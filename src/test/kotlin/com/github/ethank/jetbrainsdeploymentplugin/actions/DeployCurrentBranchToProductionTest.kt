package com.github.ethank.jetbrainsdeploymentplugin.actions

import com.github.ethank.jetbrainsdeploymentplugin.common.git.getBranch
import com.github.ethank.jetbrainsdeploymentplugin.common.slack.sendToSlack
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.actionSystem.AnActionEvent
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DeployCurrentBranchToProductionTest {

    private lateinit var action: DeployCurrentBranchToProduction
    private val project: Project = mockk()
    private val e: AnActionEvent = mockk()

    @BeforeEach
    fun setUp() {
        every { e.project } returns project
        mockkStatic(::getBranch)
        mockkStatic(::sendToSlack)
        mockkStatic(Messages::class)
        action = spyk(DeployCurrentBranchToProduction())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `actionPerformed should send Slack message on confirmation`() {
        // Given
        val branchName = "feature/test-deployment"
        every { getBranch(project) } returns branchName
        every { Messages.showYesNoDialog(
            project,
            "Are you sure you want to deploy the current branch '$branchName' to production?",
            "Confirm Production Deployment",
            "Deploy",
            "Cancel",
            Messages.getWarningIcon()
        ) } returns Messages.YES
        mockkStatic("com.github.ethank.jetbrainsdeploymentplugin.common.slack.SlackUtilKt")
        every { sendToSlack(any(), any()) } just Runs

        // Whe
        action.actionPerformed(e)

        // The
        verify(exactly = 1) { sendToSlack(any(), any()) }
    }

    @Test
    fun `actionPerformed should not send Slack message on cancellation`() {
        // Arrange
        val branchName = "feature/test-deployment"
        every { getBranch(project) } returns branchName
        every { Messages.showYesNoDialog(
            project,
            "Are you sure you want to deploy the current branch '$branchName' to production?",
            "Confirm Production Deployment",
            "Deploy",
            "Cancel",
            Messages.getWarningIcon()
        ) } returns Messages.NO
        mockkStatic("com.github.ethank.jetbrainsdeploymentplugin.common.slack.SlackUtilKt")

        // Act
        action = DeployCurrentBranchToProduction()
        action.actionPerformed(e)

        // Assert
        verify(exactly = 0) { sendToSlack(any(), any()) }
    }
}
