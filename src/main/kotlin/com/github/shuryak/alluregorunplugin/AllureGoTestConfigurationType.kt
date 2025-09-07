package com.github.shuryak.alluregorunplugin

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.icons.AllIcons
import javax.swing.Icon

class AllureGoTestConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Allure Go Test"

    override fun getConfigurationTypeDescription(): String = "Allure Go test configuration"

    override fun getIcon(): Icon = AllIcons.Language.GO

    override fun getId(): String = "ALLURE_GO_TEST_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(AllureGoTestRunConfigurationFactory(this))

    companion object {
        val instance: AllureGoTestConfigurationType
            get() = ConfigurationTypeUtil.findConfigurationType(AllureGoTestConfigurationType::class.java)
    }
}
