package com.github.shuryak.alluregorunplugin.psi

import com.goide.psi.GoFile
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoParameterDeclaration
import com.goide.psi.GoTopLevelDeclaration
import com.goide.psi.GoType
import com.goide.psi.GoTypeReferenceExpression
import com.goide.psi.impl.GoPackage
import com.goide.psi.impl.GoPointerTypeImpl
import com.goide.psi.impl.GoTypeImpl

object GoPsiExtension {
    fun GoParameterDeclaration.isAllureFrameworkProviderT(): Boolean {
        val type = this.type ?: return false
        val typeName: String

        when (type) {
            is GoTypeReferenceExpression -> typeName = type.text
            is GoTypeImpl -> typeName = type.text.toString()
            else -> return false
        }

        val file = this.containingFile as? GoFile ?: return false

        val frameworkProviderImportSpec = file.imports.find { importSpec ->
            importSpec.path == "github.com/ozontech/allure-go/pkg/framework/provider"
        }

        return frameworkProviderImportSpec != null && typeName == "${frameworkProviderImportSpec.alias ?: "provider"}.T"
    }

    fun GoMethodDeclaration.getReceiverGoType(): GoType? {
        val receiverType = this.receiverType ?: return null
        return when (receiverType) {
            is GoPointerTypeImpl -> receiverType.type
            is GoTypeImpl -> receiverType
            else -> return null
        }
    }

    fun GoTopLevelDeclaration.getGoPackage(): GoPackage? {
        return GoPackage.of(this.containingFile as GoFile)
    }

    fun GoPackage.collectGoFiles(): Collection<GoFile> {
        return this.files()?.filterIsInstance<GoFile>() ?: emptyList()
    }
}
