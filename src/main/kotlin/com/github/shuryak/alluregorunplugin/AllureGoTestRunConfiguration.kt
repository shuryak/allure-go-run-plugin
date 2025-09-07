package com.github.shuryak.alluregorunplugin

import com.goide.execution.testing.GoTestRunConfiguration
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project

class AllureGoTestRunConfiguration(project: Project, name: String, configurationType: ConfigurationType) :
    GoTestRunConfiguration(project, name, configurationType)
