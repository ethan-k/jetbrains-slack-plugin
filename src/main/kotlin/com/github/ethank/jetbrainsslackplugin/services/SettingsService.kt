package com.github.ethank.jetbrainsslackplugin.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class SettingsService(project: Project) {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance(project)

    var slackApiToken: String
        get() = properties.getValue(SLACK_API_TOKEN_KEY) ?: ""
        set(value) = properties.setValue(SLACK_API_TOKEN_KEY, value)

    var slackChannelName: String
        get() = properties.getValue(SLACK_CHANNEL_NAME_KEY) ?: ""
        set(value) = properties.setValue(SLACK_CHANNEL_NAME_KEY, value)

    var commitCount: Int
        get() = properties.getValue(COMMIT_COUNT_KEY, "10").toIntOrNull() ?: 10
        set(value) = properties.setValue(COMMIT_COUNT_KEY, value.toString())

    companion object {
        private const val SLACK_API_TOKEN_KEY = "slackApiToken"
        private const val SLACK_CHANNEL_NAME_KEY = "slackChannelName"
        private const val COMMIT_COUNT_KEY = "commitCount"
    }
}