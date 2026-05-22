package com.dungeonsanddeigo.i18n

enum class Locale { EN, PT_BR }

object I18n {
    var current: Locale = Locale.EN

    fun t(key: String): String = when (current) {
        Locale.EN -> EnStrings.get(key)
        Locale.PT_BR -> PtBrStrings.get(key)
    }
}

fun t(key: String): String = I18n.t(key)
