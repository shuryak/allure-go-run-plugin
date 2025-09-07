package com.github.shuryak.alluregorunplugin

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class AllureGoTestRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String {
        return type.id
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        AllureGoTestRunConfiguration(project, "AllureGoTestRunConfiguration", type)
}
