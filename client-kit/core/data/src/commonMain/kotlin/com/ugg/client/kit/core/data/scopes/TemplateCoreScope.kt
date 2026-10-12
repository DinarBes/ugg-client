package com.ugg.client.kit.core.data.scopes

import com.ugg.client.infrastructure.api.core.TemplateCore
import org.kodein.di.bindings.Scope
import org.kodein.di.bindings.ScopeRegistry
import org.kodein.di.bindings.StandardScopeRegistry

object TemplateCoreScope : Scope<TemplateCore> {
    private val registries = mutableMapOf<TemplateCore, ScopeRegistry>()

    override fun getRegistry(context: TemplateCore): ScopeRegistry =
        registries.getOrPut(context) { StandardScopeRegistry() }

    fun close(core: TemplateCore) = registries.remove(core)?.clear()
}