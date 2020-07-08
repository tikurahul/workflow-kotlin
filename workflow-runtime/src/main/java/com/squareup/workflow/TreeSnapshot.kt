/*
 * Copyright 2020 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.workflow

import com.squareup.workflow.TreeSnapshot.Companion.forRootOnly
import com.squareup.workflow.TreeSnapshot.Companion.parse
import com.squareup.workflow.internal.WorkflowNodeId
import okio.Buffer
import okio.ByteString

/**
 * Aggregate of all the snapshots of a tree of workflows.
 *
 * Can be serialized with [toByteString] and deserialized with [parse].
 *
 * For tests, you can get a [TreeSnapshot] from a [RenderingAndSnapshot] or by creating one for
 * your root workflow only by calling [forRootOnly].
 *
 * @constructor
 * @param childTreeSnapshots A function that will lazily parse the child snapshots for this tree
 * when invoked. It will be cached in the [childTreeSnapshots] property.
 */
class TreeSnapshot internal constructor(
  workflowSnapshot: Snapshot?,
  childTreeSnapshots: () -> Map<WorkflowNodeId, TreeSnapshot>
) {
  /**
   * The [Snapshot] for the root workflow, or null if that snapshot was empty or unspecified.
   * Computed lazily to avoid serializing the snapshot until necessary.
   */
  internal val workflowSnapshot: Snapshot? by lazy(NONE) {
    workflowSnapshot?.takeUnless { it.bytes.size == 0 }
  }

  /**
   * The map of child snapshots by child [WorkflowNodeId]. Computed lazily so the entire snapshot
   * tree isn't parsed upfront.
   */
  internal val childTreeSnapshots: Map<WorkflowNodeId, TreeSnapshot>
      by lazy(NONE, childTreeSnapshots)

  /**
   * Writes this [Snapshot] and all its children into a [ByteString]. The snapshot can be restored
   * with [parse].
   *
   * Any children snapshots for workflows whose [WorkflowIdentifier]s are
   * [unsnapshottable][unsnapshottableIdentifier] will not be serialized.
   */
  fun toByteString(): ByteString = Buffer().let { sink ->
    sink.writeByteStringWithLength(workflowSnapshot?.bytes ?: ByteString.EMPTY)
    val childBytes: List<Pair<ByteString, ByteString>> =
      childTreeSnapshots.mapNotNull { (childId, childSnapshot) ->
        val childIdBytes = childId.toByteStringOrNull() ?: return@mapNotNull null
        val childSnapshotBytes = childSnapshot.toByteString()
            .takeUnless { it.size == 0 }
            ?: return@mapNotNull null
        return@mapNotNull Pair(childIdBytes, childSnapshotBytes)
      }
    sink.writeInt(childBytes.size)
    childBytes.forEach { (childIdBytes, childSnapshotBytes) ->
      sink.writeByteStringWithLength(childIdBytes)
      sink.writeByteStringWithLength(childSnapshotBytes)
    }
    sink.readByteString()
  }

  override fun equals(other: Any?): Boolean = when {
    other === this -> true
    other !is TreeSnapshot -> false
    else -> other.workflowSnapshot == workflowSnapshot &&
        other.childTreeSnapshots == childTreeSnapshots
  }

  override fun hashCode(): Int {
    var result = workflowSnapshot.hashCode()
    result = 31 * result + childTreeSnapshots.hashCode()
    return result
  }

  companion object {
    /**
     * A [TreeSnapshot] that has a null [workflowSnapshot] and no [childTreeSnapshots].
     */
    val NONE = TreeSnapshot(null, ::emptyMap)

    /**
     * Returns a [TreeSnapshot] that only contains a [Snapshot] for the root workflow, and no child
     * snapshots.
     */
    fun forRootOnly(rootSnapshot: Snapshot?): TreeSnapshot = TreeSnapshot(rootSnapshot, ::emptyMap)

    /**
     * Parses a "root" snapshot and the list of child snapshots with associated [WorkflowNodeId]s
     * from a [ByteString] returned by [toByteString].
     *
     * Never returns an empty root snapshot: if the root snapshot is empty it will be null.
     * Child snapshots, however, are always returned as-is. They must be recursively passed to this
     * function to continue parsing the tree.
     *
     * Note that this method is mostly lazy. It will parse the list of child [TreeSnapshot]s, but
     * will not recursively parse each of those.
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun parse(bytes: ByteString): TreeSnapshot = bytes.parse { source ->
      val rootSnapshot = source.readByteStringWithLength()
      return TreeSnapshot(Snapshot.of(rootSnapshot)) {
        val childSnapshotCount = source.readInt()
        buildMap(childSnapshotCount) {
          for (i in 0 until childSnapshotCount) {
            val idBytes = source.readByteStringWithLength()
            val id = WorkflowNodeId.parse(idBytes)
            val childSnapshot = source.readByteStringWithLength()
            this[id] = parse(childSnapshot)
          }
        }
      }
    }
  }
}
