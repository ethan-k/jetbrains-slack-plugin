package com.github.ethank.jetbrainsslackplugin.actions

import com.github.ethank.jetbrainsslackplugin.common.slack.createDeployMessage
import com.github.ethank.jetbrainsslackplugin.common.slack.sendToSlack
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class DeployCurrentBranchToStaging : AnAction("Deploy Current Branch to Staging") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val message = createDeployMessage(project, "staging", null)
        sendToSlack(project, message)
    }
}