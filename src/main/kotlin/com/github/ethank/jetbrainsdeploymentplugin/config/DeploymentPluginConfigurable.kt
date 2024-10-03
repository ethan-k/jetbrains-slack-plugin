package com.github.ethank.jetbrainsdeploymentplugin.config

import com.github.ethank.jetbrainsdeploymentplugin.services.SettingsService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class DeploymentPluginConfigurable(project: Project) : Configurable {
    private lateinit var apiTokenField: JTextField
    private lateinit var channelNameField: JTextField
    private lateinit var commitCountField: JTextField
    private lateinit var updatePeriodField: JTextField
    private val settingsService = project.getService(SettingsService::class.java)

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val c = GridBagConstraints()

        apiTokenField = JTextField(30)
        channelNameField = JTextField(30)
        commitCountField = JTextField(5)
        updatePeriodField = JTextField(5)

        c.fill = GridBagConstraints.HORIZONTAL
        c.anchor = GridBagConstraints.WEST
        c.insets = Insets(5, 5, 5, 5)
        c.weightx = 1.0

        addLabelAndField(panel, c, "Slack API Token:", apiTokenField, 0)
        addLabelAndField(panel, c, "Slack Channel Name:", channelNameField, 1)
        addLabelAndField(panel, c, "Number of Commits:", commitCountField, 2)
        addLabelAndField(panel, c, "Update Period (ms):", updatePeriodField, 3)

        c.weighty = 1.0
        c.gridx = 0
        c.gridy = 4
        c.gridwidth = 2
        panel.add(Box.createVerticalGlue(), c)

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
        val fieldPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        fieldPanel.add(field)
        panel.add(fieldPanel, c)
    }

    override fun isModified(): Boolean {
        return apiTokenField.text != settingsService.slackApiToken ||
                channelNameField.text != settingsService.slackChannelName ||
                commitCountField.text != settingsService.commitCount.toString() ||
                updatePeriodField.text != settingsService.updatePeriod.toString()
    }

    override fun apply() {
        settingsService.slackApiToken = apiTokenField.text
        settingsService.slackChannelName = channelNameField.text
        settingsService.commitCount = commitCountField.text.toIntOrNull() ?: 10

        val newUpdatePeriod = updatePeriodField.text.toLongOrNull() ?: 5000L
        if (newUpdatePeriod != settingsService.updatePeriod) {
            settingsService.updatePeriod = newUpdatePeriod
        }
    }

    override fun reset() {
        apiTokenField.text = settingsService.slackApiToken
        channelNameField.text = settingsService.slackChannelName
        commitCountField.text = settingsService.commitCount.toString()
        updatePeriodField.text = settingsService.updatePeriod.toString()
    }

    override fun getDisplayName(): String = "Deployment Plugin Settings"
}
