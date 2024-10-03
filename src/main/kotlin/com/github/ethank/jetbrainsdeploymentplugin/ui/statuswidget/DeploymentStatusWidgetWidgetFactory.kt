package com.github.ethank.jetbrainsdeploymentplugin.ui.statuswidget

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class DeploymentStatusWidgetWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = DeploymentStatusWidget.ID

    override fun getDisplayName(): String = "Deployment Status"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = DeploymentStatusWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}