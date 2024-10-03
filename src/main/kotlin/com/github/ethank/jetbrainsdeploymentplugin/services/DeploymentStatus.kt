package com.github.ethank.jetbrainsdeploymentplugin.services

import com.github.ethank.jetbrainsdeploymentplugin.services.model.DeploymentInfo

interface DeploymentStatus {
    fun getDeploymentStatus(environmentName: String): DeploymentInfo
}