package com.github.ethank.jetbrainsdeploymentplugin.services

import com.intellij.openapi.components.Service
import com.intellij.util.io.HttpRequests
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Service(Service.Level.PROJECT)
class GitHubDeploymentService : DeploymentStatus {
    companion object {
        private const val GITHUB_API_URL = "https://api.github.com"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun getDeploymentStatus(owner: String, repo: String, environmentName: String): String {
        val url = "$GITHUB_API_URL/repos/$owner/$repo/deployments"

        return try {
            val response = HttpRequests.request(url)
                .accept("application/vnd.github.v3+json")
                .readString()

            val deployments = json.decodeFromString<List<Deployment>>(response)
            val targetDeployment = deployments.find { it.environment == environmentName }

            if (targetDeployment != null) {
                val statusResponse = HttpRequests.request(targetDeployment.statuses_url)
                    .accept("application/vnd.github.v3+json")
                    .readString()

                val statuses = json.decodeFromString<List<DeploymentStatus>>(statusResponse)
                statuses.firstOrNull()?.state ?: "No status found"
            } else {
                "No deployment found"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    @Serializable
    data class Deployment(
        val environment: String,
        val statuses_url: String
    )

    @Serializable
    data class DeploymentStatus(
        val state: String
    )
}