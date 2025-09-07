package com.github.shuryak.alluregorunplugin.psi

import com.goide.execution.testing.GoTestFinder.isTestFunction
import com.goide.psi.*
import com.goide.psi.impl.GoPackage
import com.goide.util.GoUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType

object GoPsiExtension {
    fun GoParameterDeclaration.isAllureFrameworkProviderT(): Boolean {
        val typeSpec = (this.type?.typeReferenceExpression?.resolve() ?: return false) as GoTypeSpec

        val expectedImport = "github.com/ozontech/allure-go/pkg/framework/provider"

        return typeSpec.containingFile.getImportPath(true) == expectedImport && typeSpec.name == "T"
    }

    fun GoType.unwrapPointerIfNeeded(): GoType? {
        return when (this) {
            is GoPointerType -> this.type
            else -> this
        }
    }

    fun GoMethodDeclaration.isAllureTestCandidate(): Boolean {
        GoUtil.checkPrefix(this.name, "Test").let { if (!it) return false }
        val params = this.signature?.parameters?.parameterDeclarationList ?: return false
        return !(params.size != 1 || !params[0].isAllureFrameworkProviderT())
    }

    fun GoMethodDeclaration.findAllureSuiteRunner(): GoFunctionOrMethodDeclaration? {
        val receiverType = this.receiverType?.unwrapPointerIfNeeded() ?: return null
        val receiverContainingFile = receiverType.typeReferenceExpression?.resolve()?.containingFile

        findAllureSuiteRunner(receiverContainingFile as GoFile, receiverType)?.let { return it }

        this.getGoPackage()?.collectGoFiles()?.forEach { file ->
            findAllureSuiteRunner(file, receiverType)?.let { return it }
        }

        return null
    }

    fun GoExpression.isStructPointerCreationNamed(expected: String): Boolean {
        var typeSpec: GoTypeSpec

        when (this) {
            is GoUnaryExpr -> {
                if (this.operator?.text != "&") return false
                val lit = (this.expression as? GoCompositeLit) ?: return false
                typeSpec = (lit.typeReferenceExpression?.resolve() ?: return false) as? GoTypeSpec ?: return false
            }
            is GoBuiltinCallExpr -> {
                if (this.expression.text != "new") return false
                val type = this.builtinArgumentList.type ?: return false
                typeSpec = (type.typeReferenceExpression?.resolve() ?: return false) as? GoTypeSpec ?: return false
            }
            else -> return false
        }

        return typeSpec.name == expected
    }

    fun GoCallExpr.isAllureSuiteRun(): Boolean {
        val resolved = (this.expression as? GoReferenceExpression)?.resolve() ?: return false
        val func = resolved as? GoFunctionDeclaration ?: return false

        if (func.name != "RunSuite" && func.name != "RunNamedSuite") return false

        val expectedImport = "github.com/ozontech/allure-go/pkg/framework/suite"
        return func.containingFile.getImportPath(true) == expectedImport
    }

    private fun findAllureSuiteRunner(goFile: GoFile, suite: GoType): GoFunctionOrMethodDeclaration? {
        val calls = PsiTreeUtil.findChildrenOfType(goFile, GoCallExpr::class.java)

        calls.forEach { call ->
            if (call.isAllureSuiteRun()) {
                call.argumentList.expressionList.forEach { arg ->
                    if (arg.isStructPointerCreationNamed(suite.text)) {
                        val parent = call.parentOfType<GoFunctionOrMethodDeclaration>()

                        if (parent != null && isTestFunction(parent)) {
                            return parent
                        }
                    }
                }
            }
        }

        return null
    }

    fun GoTopLevelDeclaration.getGoPackage(): GoPackage? {
        return GoPackage.of(this.containingFile as GoFile)
    }

    fun GoPackage.collectGoFiles(): Collection<GoFile> {
        return this.files()?.filterIsInstance<GoFile>() ?: emptyList()
    }
}
