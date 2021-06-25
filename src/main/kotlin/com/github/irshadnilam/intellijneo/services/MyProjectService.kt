package com.github.irshadnilam.intellijneo.services

import com.github.irshadnilam.intellijneo.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
