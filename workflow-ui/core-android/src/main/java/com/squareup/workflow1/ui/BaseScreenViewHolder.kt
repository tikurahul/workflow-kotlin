package com.squareup.workflow1.ui

import android.view.View

@WorkflowUiExperimentalApi
internal class BaseScreenViewHolder<ScreenT: Screen>(
  private val initialRendering: ScreenT,
  private val initialViewEnvironment: ViewEnvironment,
  override val view: View,
  private val updater: ScreenViewUpdater<ScreenT>
) : ScreenViewHolder<ScreenT> {
  lateinit var currentRendering: ScreenT
  lateinit var currentEnvironment: ViewEnvironment

  override val screen: ScreenT
    get() = currentRendering
  override val environment: ViewEnvironment
    get() = currentEnvironment

  override fun start() {
    showScreen(initialRendering, initialViewEnvironment)
  }

  override fun showScreen(screen: ScreenT, environment: ViewEnvironment) {
    currentRendering = screen
    currentEnvironment = environment
    updater.showRendering(screen, environment)
  }
}