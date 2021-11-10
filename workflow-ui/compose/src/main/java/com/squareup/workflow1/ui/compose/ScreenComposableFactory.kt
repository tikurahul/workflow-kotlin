package com.squareup.workflow1.ui.compose

import androidx.compose.runtime.Composable
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.ViewRegistry
import com.squareup.workflow1.ui.WorkflowUiExperimentalApi

@WorkflowUiExperimentalApi
public interface ScreenComposableFactory<RenderingT : Screen> : ViewRegistry.Entry<RenderingT> {
  @Suppress("FunctionName")
  @Composable public fun Content(
    renderingT: RenderingT,
    viewEnvironment: ViewEnvironment
  )
}
