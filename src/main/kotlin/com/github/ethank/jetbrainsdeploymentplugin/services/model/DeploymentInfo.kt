package com.github.ethank.jetbrainsdeploymentplugin.services.model

data class DeploymentInfo(
    val status: String,
    val sha: String,
    val url: String,
    val deployedAt: String,
    val ref: String
) {
    fun getRef(): String {
        return if (ref == "none") {
            sha
        } else {
            ref
        }
    }
}