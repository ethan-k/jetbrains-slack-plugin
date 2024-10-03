package com.github.ethank.jetbrainsdeploymentplugin.common.slack

import com.github.ethank.jetbrainsdeploymentplugin.common.git.model.CommitInfo
import com.github.ethank.jetbrainsdeploymentplugin.common.git.getBranch
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.slack.api.Slack
import com.slack.api.methods.MethodsClient

private const val DEPLOY_MESSAGE_FORMAT = "Deploying to %s:\n%s"

fun sendToSlack(project: Project, message: String) {
    val properties = PropertiesComponent.getInstance(project)
    val apiToken = properties.getValue("slackApiToken")
    val channelName = properties.getValue("slackChannelName")

    if (apiToken.isNullOrEmpty() || channelName.isNullOrEmpty()) {
        Messages.showErrorDialog(
            project,
            "Please set your Slack API token and channel name in the plugin settings.",
            "Configuration Error"
        )
        return
    }

    try {
        val client: MethodsClient = Slack.getInstance().methods(apiToken)
        val result = client.chatPostMessage { req ->
            req.channel(channelName)
                .text(message)
        }

        if (result.isOk) {
            Messages.showInfoMessage(project, "Message sent successfully!", "Success")
        } else {
            Messages.showErrorDialog(project, "Failed to send message: ${result.error}", "Error")
        }
    } catch (e: Exception) {
        Messages.showErrorDialog(project, "An error occurred: ${e.message}", "Error")
    }
}

fun createDeployMessage(project: Project, environment: String, commit: CommitInfo?): String {
    val info = if (commit != null) {
        "Commit: ${commit.id} - ${commit.shortMessage}"
    } else {
        "Branch: ${getBranch(project)}"
    }
    return DEPLOY_MESSAGE_FORMAT.format(environment, info)
}