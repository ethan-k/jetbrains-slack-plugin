package com.github.ethank.jetbrainsdeploymentplugin.common.deployment

import com.github.ethank.jetbrainsdeploymentplugin.common.git.model.CommitInfo
import com.intellij.openapi.project.Project

interface Deployment {
    fun deploy(project: Project, environment: String, commit: CommitInfo?)
}



