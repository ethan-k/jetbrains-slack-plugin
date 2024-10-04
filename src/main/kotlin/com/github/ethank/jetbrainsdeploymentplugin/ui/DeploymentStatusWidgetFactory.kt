package com.github.ethank.jetbrainsdeploymentplugin.ui

import com.github.ethank.jetbrainsdeploymentplugin.services.GitHubDeploymentService
import com.github.ethank.jetbrainsdeploymentplugin.services.SettingsService
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.fixedRateTimer

class GitHubDeploymentStatusWidget(project: Project) : StatusBarWidget,
    StatusBarWidget.MultipleTextValuesPresentation,
    SettingsService.SettingsChangeListener {

    companion object {
        const val ID = "DeploymentStatus"
    }

    private val currentStatus = AtomicReference("Fetching deployment status...")
    private var statusBar: StatusBar? = null
    private var fixedRateTimer: Timer? = null
    private var currentPeriod: Long
    private val settingsService: SettingsService = project.getService(SettingsService::class.java)
    private val gitHubDeploymentService: GitHubDeploymentService = project.getService(GitHubDeploymentService::class.java)

    init {
        currentPeriod = settingsService.updatePeriod
    }

    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        settingsService.addListener(this)
        startTimer()
    }

    override fun dispose() {
        stopTimer()
        settingsService.removeListener(this)
        statusBar = null
    }

    private fun startTimer() {
        stopTimer()
        fixedRateTimer("StatusUpdater", daemon = true, period = currentPeriod) {
            val newStatus = if (currentStatus.get().contains("Ready")) "Deploying..." else "Status: Ready"
            println("${java.time.LocalDateTime.now()} - $newStatus")
            updateStatus(newStatus)
        }
    }

    private fun stopTimer() {
        fixedRateTimer?.cancel()
        fixedRateTimer = null
    }

    override fun onUpdatePeriodChanged(newPeriod: Long) {
        updateTimerPeriod(newPeriod)
    }

    private fun updateTimerPeriod(newPeriod: Long) {
        if (newPeriod != currentPeriod) {
            currentPeriod = newPeriod
            startTimer()
        }
    }

    override fun getPopupStep(): ListPopup {
        val group = DefaultActionGroup()
        group.add(object : AnAction(currentStatus.get()) {
            override fun actionPerformed(e: AnActionEvent) {}

            override fun update(e: AnActionEvent) {
                e.presentation.text = currentStatus.get()
                e.presentation.isEnabled = false
                e.presentation.icon = if (currentStatus.get().contains("Ready"))
                    AllIcons.General.InspectionsOK
                else
                    AllIcons.General.BalloonInformation
            }
        })

        group.addSeparator()

        group.add(object : AnAction("Refresh Status") {
            override fun actionPerformed(e: AnActionEvent) {
                updateStatus("Status: Refreshed at ${System.currentTimeMillis() % 1000}")
            }
        })

        return JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Deployment Status",
                group,
                DataManager.getInstance().getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )
    }

    override fun getTooltipText(): String? = "GitHub Deployment Status"

    override fun getClickConsumer(): Consumer<MouseEvent>? = null

    override fun getSelectedValue(): String = currentStatus.get()

    private fun updateStatus(status: String) {
        currentStatus.set(status)
        statusBar?.updateWidget(ID)
    }
}

class GitHubDeploymentStatusWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = GitHubDeploymentStatusWidget.ID

    override fun getDisplayName(): String = "Deployment Status"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = GitHubDeploymentStatusWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}