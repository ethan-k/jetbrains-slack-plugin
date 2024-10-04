package com.github.ethank.jetbrainsslackplugin.common.deployment

import com.github.ethank.jetbrainsslackplugin.common.git.model.CommitInfo
import com.intellij.openapi.project.Project

interface Deployment {
    fun deploy(project: Project, environment: String, commit: CommitInfo?)
}



