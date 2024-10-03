package com.github.ethank.jetbrainsdeploymentplugin.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class SlackPluginToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = SlackPluginToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(toolWindowContent.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class SlackPluginToolWindowContent(private val project: Project) {
    private val apiTokenField = JTextField(20)
    private val channelNameField = JTextField(20)
    private val commitCountField = JTextField(5)

    fun getContent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val c = GridBagConstraints()

        c.fill = GridBagConstraints.HORIZONTAL
        c.anchor = GridBagConstraints.WEST
        c.insets = Insets(5, 5, 5, 5)
        c.weightx = 1.0

        addLabelAndField(panel, c, "Slack API Token:", apiTokenField, 0)
        addLabelAndField(panel, c, "Slack Channel Name:", channelNameField, 1)
        addLabelAndField(panel, c, "Number of Commits:", commitCountField, 2)

        val saveButton = JButton("Save")
        saveButton.addActionListener { saveSettings() }
        c.gridy = 3
        c.gridx = 0
        c.gridwidth = 2
        panel.add(saveButton, c)

        c.weighty = 1.0
        c.gridy = 4
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
        val properties = PropertiesComponent.getInstance(project)
        apiTokenField.text = properties.getValue("slackApiToken") ?: ""
        channelNameField.text = properties.getValue("slackChannelName") ?: ""
        commitCountField.text = properties.getValue("commitCount", "10")
    }

    private fun saveSettings() {
        val properties = PropertiesComponent.getInstance(project)
        properties.setValue("slackApiToken", apiTokenField.text)
        properties.setValue("slackChannelName", channelNameField.text)
        properties.setValue("commitCount", commitCountField.text)
        JOptionPane.showMessageDialog(null, "Settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE)
    }
}