package com.github.ethank.jetbrainsslackplugin.actions

import com.github.ethank.jetbrainsslackplugin.common.git.getRecentCommits
import com.github.ethank.jetbrainsslackplugin.common.git.model.CommitInfo
import com.github.ethank.jetbrainsslackplugin.common.slack.createDeployMessage
import com.github.ethank.jetbrainsslackplugin.common.slack.sendToSlack
import com.github.ethank.jetbrainsslackplugin.services.SettingsService
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class SelectCommitDeployment() : ActionGroup("Deployment", true) {

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val actions = mutableListOf<AnAction>()
        actions.add(object : ActionGroup("Deploy Specific Commit", true) {
            override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                return arrayOf(
                    createCommitSelectionAction("Staging"),
                    createCommitSelectionAction("Production")
                )
            }
        })

        return actions.toTypedArray()
    }

    private fun createCommitSelectionAction(environment: String): AnAction {
        return object : AnAction(environment) {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project ?: return
                val commit = selectCommit(project) ?: return
                val message = createDeployMessage(project, environment.toLowerCase(), commit)
                sendToSlack(project, message)
            }
        }
    }
}


fun selectCommit(project: Project): CommitInfo? {
    val commitCount = project.getService(SettingsService::class.java).commitCount
    val commits = getRecentCommits(project, commitCount)

    val commitOptions = commits.map { "${it.id} - ${it.shortMessage}" }.toTypedArray()
    val selection = Messages.showChooseDialog(
        project,
        "Select a commit to deploy",
        "Deploy Commit",
        null,
        commitOptions,
        commitOptions.firstOrNull()
    )

    return if (selection >= 0) commits[selection] else null
}

