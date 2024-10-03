package com.github.ethank.jetbrainsdeploymentplugin.common.deployment

import com.github.ethank.jetbrainsdeploymentplugin.common.git.model.CommitInfo
import com.github.ethank.jetbrainsdeploymentplugin.common.slack.createDeployMessage
import com.github.ethank.jetbrainsdeploymentplugin.common.slack.sendToSlack
import com.intellij.openapi.project.Project

class SlackDeployment : Deployment {
    override fun deploy(project: Project, environment: String, commit: CommitInfo?) {
        sendToSlack(project, createDeployMessage(project, environment, commit))
    }
}