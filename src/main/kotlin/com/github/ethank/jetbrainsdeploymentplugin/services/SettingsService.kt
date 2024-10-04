package com.github.ethank.jetbrainsdeploymentplugin.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.concurrent.CopyOnWriteArrayList

@Service(Service.Level.PROJECT)
class SettingsService(project: Project) {
    companion object {
        private const val SLACK_API_TOKEN_KEY = "slackApiToken"
        private const val SLACK_CHANNEL_NAME_KEY = "slackChannelName"
        private const val COMMIT_COUNT_KEY = "commitCount"
        private const val UPDATE_PERIOD_KEY = "updatePeriod"
    }

    private val properties: PropertiesComponent = PropertiesComponent.getInstance(project)
    private val listeners = CopyOnWriteArrayList<SettingsChangeListener>()

    var slackApiToken: String
        get() = properties.getValue(SLACK_API_TOKEN_KEY) ?: ""
        set(value) = properties.setValue(SLACK_API_TOKEN_KEY, value)

    var slackChannelName: String
        get() = properties.getValue(SLACK_CHANNEL_NAME_KEY) ?: ""
        set(value) = properties.setValue(SLACK_CHANNEL_NAME_KEY, value)

    var commitCount: Int
        get() = properties.getValue(COMMIT_COUNT_KEY, "10").toIntOrNull() ?: 10
        set(value) = properties.setValue(COMMIT_COUNT_KEY, value.toString())

    var updatePeriod: Long
        get() = properties.getValue(UPDATE_PERIOD_KEY, "5000").toLongOrNull() ?: 5000L
        set(value) {
            properties.setValue(UPDATE_PERIOD_KEY, value.toString())
            notifyUpdatePeriodChanged(value)
        }

    fun addListener(listener: SettingsChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SettingsChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyUpdatePeriodChanged(newPeriod: Long) {
        listeners.forEach { it.onUpdatePeriodChanged(newPeriod) }
    }

    interface SettingsChangeListener {
        fun onUpdatePeriodChanged(newPeriod: Long)
    }
}

