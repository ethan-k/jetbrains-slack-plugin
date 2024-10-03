package com.github.ethank.jetbrainsdeploymentplugin.ui.statuswidget

import com.github.ethank.jetbrainsdeploymentplugin.actions.DeployCurrentBranchToProduction
import com.github.ethank.jetbrainsdeploymentplugin.actions.DeployCurrentBranchToStaging
import com.github.ethank.jetbrainsdeploymentplugin.actions.SelectCommitDeployment
import com.github.ethank.jetbrainsdeploymentplugin.services.GitHubDeploymentService
import com.github.ethank.jetbrainsdeploymentplugin.services.SettingsService
import com.github.ethank.jetbrainsdeploymentplugin.services.model.DeploymentInfo
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.Consumer
import java.awt.Point
import java.awt.event.MouseEvent
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class DeploymentStatusWidget(private val project: Project) : StatusBarWidget,
    StatusBarWidget.TextPresentation,
    SettingsService.SettingsChangeListener {

    companion object {
        const val ID = "DeploymentStatus"
    }

    private var statusBar: StatusBar? = null
    private var stagingStatus: DeploymentInfo = DeploymentInfo("Loading...", "", "", "", "")
    private var productionStatus: DeploymentInfo = DeploymentInfo("Loading...", "", "", "", "")
    private var lastSuccessfulStagingStatus: DeploymentInfo = DeploymentInfo("No successful deployment", "", "", "", "")
    private var lastSuccessfulProductionStatus: DeploymentInfo =
        DeploymentInfo("No successful deployment", "", "", "", "")

    private val settingsService: SettingsService = project.getService(SettingsService::class.java)
    private val gitHubDeploymentService: GitHubDeploymentService =
        project.getService(GitHubDeploymentService::class.java)
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var updateTask: ScheduledFuture<*>? = null

    init {
        updateStatus()
        startUpdateTimer()
    }

    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        settingsService.addListener(this)
    }


    override fun getText(): String {
        return "S: ${getStatusIcon(stagingStatus)} ${stagingStatus.sha} | P: ${getStatusIcon(productionStatus)} ${productionStatus.sha}"
    }

    override fun getTooltipText(): String = "Click to see deployment options"


    override fun getAlignment(): Float = 0f

    override fun onUpdatePeriodChanged(newPeriod: Long) {
        startUpdateTimer()
    }

    override fun dispose() {
        settingsService.removeListener(this)
        cancelUpdateTimer()
        scheduler.shutdown()
        statusBar = null
    }

    override fun getClickConsumer(): Consumer<MouseEvent>? = Consumer { event ->
        val dataContext = DataManager.getInstance().getDataContext(event.component)
        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            "Deployment Details",
            createDetailedActionGroup(),
            dataContext,
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            true
        )
        val dimension = popup.content.preferredSize
        val at = Point(event.x - dimension.width / 2, event.y - dimension.height)
        popup.show(RelativePoint(event.component, at))
    }

    private fun createDetailedActionGroup(): DefaultActionGroup {
        return DefaultActionGroup().apply {
            addSeparator("Staging")
            add(createDeploymentAction("Last Deployment", stagingStatus))
            add(createDeploymentAction("Last Successful Deployment", lastSuccessfulStagingStatus))

            addSeparator("Production")
            add(createDeploymentAction("Last Deployment", productionStatus))
            add(createDeploymentAction("Last Successful Deployment", lastSuccessfulProductionStatus))
        }
    }

    private fun createDeploymentAction(environment: String, deploymentInfo: DeploymentInfo): AnAction {
        return object :
            AnAction("$environment: ${deploymentInfo.status} (${deploymentInfo.sha}) - Deployed: ${deploymentInfo.deployedAt}") {
            override fun actionPerformed(e: AnActionEvent) {
                gitHubDeploymentService.openDeploymentUrl(deploymentInfo.url)
            }
        }
    }

    private fun getStatusIcon(status: DeploymentInfo): String {
        return when {
            status.status == "Success" -> "✅"
            status.status.startsWith("Error") -> "❌"
            else -> "ℹ️"
        }
    }

    private fun updateStatus() {
        val newStagingStatus = gitHubDeploymentService.getDeploymentStatus("staging")
        val newProductionStatus = gitHubDeploymentService.getDeploymentStatus("production")

        if (newStagingStatus != stagingStatus || newProductionStatus != productionStatus) {
            stagingStatus = newStagingStatus
            productionStatus = newProductionStatus

            // Update last successful deployment for staging if applicable
            if (newStagingStatus.status == "Success") {
                lastSuccessfulStagingStatus = newStagingStatus
            }

            // Update last successful deployment for production if applicable
            if (newProductionStatus.status == "Success") {
                lastSuccessfulProductionStatus = newProductionStatus
            }

            ApplicationManager.getApplication().invokeLater {
                statusBar?.updateWidget(ID)
            }
        }
    }

    private fun startUpdateTimer() {
        cancelUpdateTimer()
        val updatePeriod = settingsService.updatePeriod
        updateTask = scheduler.scheduleAtFixedRate({
            if (!project.isDisposed) {
                updateStatus()
            }
        }, updatePeriod, updatePeriod, TimeUnit.MILLISECONDS)
    }


    private fun cancelUpdateTimer() {
        updateTask?.cancel(true)
        updateTask = null
    }
}

