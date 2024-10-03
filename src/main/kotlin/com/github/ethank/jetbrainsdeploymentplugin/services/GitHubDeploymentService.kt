package com.github.ethank.jetbrainsdeploymentplugin.services

import com.github.ethank.jetbrainsdeploymentplugin.common.git.getGitHubRepoInfo
import com.github.ethank.jetbrainsdeploymentplugin.services.model.DeploymentInfo
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service(Service.Level.PROJECT)
class GitHubDeploymentService(private val project: Project) : DeploymentStatus {

    private val logger = LoggerFactory.getLogger(GitHubDeploymentService::class.java)

    companion object {
        private const val GITHUB_API_URL = "https://api.github.com"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Apache) {
        install(ContentNegotiation) {
            json(json)
        }
    }


    override fun getDeploymentStatus(environmentName: String): DeploymentInfo {
        val repoInfo = getGitHubRepoInfo(project) ?: return DeploymentInfo("Repo info not found", "", "", "")

        return runBlocking {
            try {
                val deployments = getDeployments(repoInfo.owner, repoInfo.repo)
                val targetDeployment = deployments.find { it.environment.equals(environmentName, ignoreCase = true) }

                if (targetDeployment != null) {
                    val statuses = getDeploymentStatuses(targetDeployment.statuses_url)
                    val lastSuccessfulStatus = statuses.firstOrNull { it.state == "success" }

                    if (lastSuccessfulStatus != null) {
                        DeploymentInfo(
                            status = "Success",
                            sha = targetDeployment.sha.take(7),
                            url = lastSuccessfulStatus.target_url ?: targetDeployment.url,
                            deployedAt = formatDate(lastSuccessfulStatus.updated_at)
                        )
                    } else {
                        DeploymentInfo("No successful deployment", "", "", "")
                    }
                } else {
                    DeploymentInfo("No deployment found", "", "", "")
                }
            } catch (e: Exception) {
                DeploymentInfo("Error: ${e.message}", "", "", "")
            }
        }
    }

    private fun formatDate(dateString: String): String {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    fun openDeploymentUrl(url: String) {
        BrowserUtil.browse(url)
    }

    private suspend inline fun <reified T> getAndParseResponse(url: String): T {
        val response = client.get(url) {
            header("Accept", "application/vnd.github.v3+json")
            header("X-GitHub-Api-Version", "2022-11-28")
            withAuthentication()
        }
        val responseBody = response.body<String>()
        logger.debug("Raw API response: $responseBody")
        return json.decodeFromString(responseBody)
    }

    private suspend fun getDeployments(owner: String, repo: String): List<Deployment> {
        val url = "$GITHUB_API_URL/repos/$owner/$repo/deployments"
        return getAndParseResponse(url)
    }

    private suspend fun getDeploymentStatuses(statusesUrl: String): List<DeploymentStatusResponse> {
        return getAndParseResponse(statusesUrl)
    }

    private fun HttpRequestBuilder.withAuthentication() {
        val token = project.getService(SettingsService::class.java).githubApiToken
        if (token.isNotEmpty()) {
            header("Authorization", "Bearer $token")
        }
    }

    @Serializable
    data class Deployment(
        val id: Long,
        val sha: String,
        val ref: String,
        val task: String,
        val environment: String,
        val description: String?,
        val created_at: String,
        val updated_at: String,
        val statuses_url: String,
        val url: String,
        val repository_url: String
    )

    @Serializable
    data class DeploymentStatusResponse(
        val state: String,
        val description: String?,
        val created_at: String,
        val updated_at: String,
        val deployment_url: String,
        val repository_url: String,
        val target_url: String?
    )
}

