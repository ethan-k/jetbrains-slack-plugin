package com.github.ethank.jetbrainsdeploymentplugin.ui.toolwindow

import com.github.ethank.jetbrainsdeploymentplugin.common.constant.*
import com.github.ethank.jetbrainsdeploymentplugin.services.SettingsService
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class DeploymentPluginToolWindow(private val project: Project) {
    private val apiTokenField = JTextField(20)
    private val channelNameField = JTextField(20)
    private val commitCountField = JTextField(5)
    private val updatePeriodField = JTextField(5)
    private val githubApiTokenField = JTextField(20)
    private val settingsService = project.getService(SettingsService::class.java)

    fun getContent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val c = GridBagConstraints()

        c.fill = GridBagConstraints.HORIZONTAL
        c.anchor = GridBagConstraints.WEST
        c.insets = JBUI.insets(5)
        c.weightx = 1.0

        addLabelAndField(panel, c, SLACK_API_TOKEN, apiTokenField, 0)
        addLabelAndField(panel, c, SLACK_DEPLOYMENT_CHANNEL_NAME, channelNameField, 1)
        addLabelAndField(panel, c, GITHUB_API_TOKEN, githubApiTokenField, 2)
        addLabelAndField(panel, c, NUMBER_OF_COMMITS, commitCountField, 3)
        addLabelAndField(panel, c, UPDATE_PERIOD, updatePeriodField, 4)

        val saveButton = JButton("Save")
        saveButton.addActionListener { saveSettings() }
        c.gridy = 5
        c.gridx = 0
        c.gridwidth = 2
        panel.add(saveButton, c)

        c.weighty = 1.0
        c.gridy = 6
        panel.add(Box.createVerticalGlue(), c)

        loadSettings()

        return panel
    }

    private fun addLabelAndField(
        panel: JPanel,
        c: GridBagConstraints,
        labelText: String,
        field: JTextField,
        gridy: Int
    ) {
        c.gridy = gridy
        c.gridx = 0
        c.gridwidth = 1
        panel.add(JLabel(labelText), c)

        c.gridx = 1
        c.gridwidth = GridBagConstraints.REMAINDER
        panel.add(field, c)
    }

    private fun loadSettings() {
        apiTokenField.text = settingsService.slackApiToken
        channelNameField.text = settingsService.slackChannelName
        commitCountField.text = settingsService.commitCount.toString()
        updatePeriodField.text = settingsService.updatePeriod.toString()
        githubApiTokenField.text = settingsService.githubApiToken
    }

    private fun saveSettings() {
        settingsService.slackApiToken = apiTokenField.text
        settingsService.slackChannelName = channelNameField.text
        settingsService.commitCount = commitCountField.text.toIntOrNull() ?: 10
        settingsService.githubApiToken = githubApiTokenField.text

        val newUpdatePeriod = updatePeriodField.text.toLongOrNull() ?: 300000L // 5 minutes
        if (newUpdatePeriod != settingsService.updatePeriod) {
            settingsService.updatePeriod = newUpdatePeriod
        }
        JOptionPane.showMessageDialog(null, "Settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE)
    }
}