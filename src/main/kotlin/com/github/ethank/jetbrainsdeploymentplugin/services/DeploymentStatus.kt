package com.github.ethank.jetbrainsdeploymentplugin.services

interface DeploymentStatus {
    fun getDeploymentStatus(owner: String, repo: String, environmentName: String): String
}