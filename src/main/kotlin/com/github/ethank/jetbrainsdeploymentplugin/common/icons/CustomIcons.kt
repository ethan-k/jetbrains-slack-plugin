package com.github.ethank.jetbrainsdeploymentplugin.common.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class CustomIcons {
    companion object {
        val LogoIcon: Icon =
            IconLoader.getIcon("/icons/logo.svg", CustomIcons::class.java)
    }
}