package com.github.ethank.jetbrainsdeploymentplugin.actions

import com.github.ethank.jetbrainsdeploymentplugin.common.git.getBranch
import com.github.ethank.jetbrainsdeploymentplugin.common.slack.createDeployMessage
import com.github.ethank.jetbrainsdeploymentplugin.common.slack.sendToSlack
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class DeployCurrentBranchToProduction : AnAction("Deploy Current Branch to Production") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val branch = getBranch(project)

        val confirmation = Messages.showYesNoDialog(
            project,
            "Are you sure you want to deploy the current branch '$branch' to production?",
            "Confirm Production Deployment",
            "Deploy",
            "Cancel",
            Messages.getWarningIcon()
        )

        if (confirmation == Messages.YES) {
            val message = createDeployMessage(project, "production", null)
            sendToSlack(project, message)
        }
    }
}

