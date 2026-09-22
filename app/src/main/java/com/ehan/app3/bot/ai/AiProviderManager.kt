package com.ehan.app3.bot.ai

class AiProviderManager {

    private val providers: List<AiProvider> =
        listOf(
            LocalAiService(),
            BackendAiProvider(
                baseUrl = "http://10.1.2.238:3000/"
            )
        )

    private val providerMap =
        providers.associateBy { it.name.lowercase() }

    private var activeProviderName = "local"

    fun getProviders(): List<AiProvider> {
        return providers
    }

    fun getActiveProvider(): AiProvider {
        return providerMap[activeProviderName]
            ?: providers.first()
    }

    fun setProvider(name: String): Boolean {
        val provider =
            providerMap[name.lowercase()]
                ?: return false

        activeProviderName =
            provider.name.lowercase()

        return true
    }

    fun getActiveProviderName(): String {
        return activeProviderName
    }

    suspend fun ask(prompt: String): String {
        return getActiveProvider().ask(prompt)
    }
}