package com.dungeonsanddeigo.web

import com.dungeonsanddeigo.i18n.I18n
import com.dungeonsanddeigo.i18n.Locale
import com.dungeonsanddeigo.web.home.showListScreen
import kotlinx.browser.localStorage

fun main() {
    val savedLocale = localStorage.getItem("app_locale")
    if (savedLocale == "PT_BR") I18n.current = Locale.PT_BR
    showListScreen()
}
