package com.dungeonsanddeigo.web

import com.dungeonsanddeigo.repository.WasmCharacterRepository
import com.dungeonsanddeigo.repository.WasmCustomClassRepository
import com.dungeonsanddeigo.repository.WasmDndBaseStatsRepository
import com.dungeonsanddeigo.repository.WasmDndMainInfoRepository
import com.dungeonsanddeigo.repository.WasmDndFeaturesRepository
import com.dungeonsanddeigo.repository.WasmDndArmorRepository
import com.dungeonsanddeigo.repository.WasmDndAppearanceRepository
import com.dungeonsanddeigo.repository.WasmDndBackstoryRepository
import com.dungeonsanddeigo.repository.WasmDndNoteRepository
import com.dungeonsanddeigo.repository.WasmDndSpellRepository
import com.dungeonsanddeigo.repository.WasmDndConsumableRepository
import com.dungeonsanddeigo.repository.WasmDndWeaponRepository
import com.dungeonsanddeigo.repository.WasmDndMagicItemRepository
import com.dungeonsanddeigo.repository.WasmDndInventoryRepository
import com.dungeonsanddeigo.repository.WasmDndMoneyRepository
import com.dungeonsanddeigo.repository.WasmDndSkillsRepository
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

object Repos {
    val character = WasmCharacterRepository()
    val mainInfo = WasmDndMainInfoRepository()
    val baseStats = WasmDndBaseStatsRepository()
    val skills = WasmDndSkillsRepository()
    val features = WasmDndFeaturesRepository()
    val inventory = WasmDndInventoryRepository()
    val money = WasmDndMoneyRepository()
    val armor = WasmDndArmorRepository()
    val consumable = WasmDndConsumableRepository()
    val weapon = WasmDndWeaponRepository()
    val magicItem = WasmDndMagicItemRepository()
    val customClass = WasmCustomClassRepository()
    val appearance = WasmDndAppearanceRepository()
    val backstory = WasmDndBackstoryRepository()
    val note = WasmDndNoteRepository()
    val spell = WasmDndSpellRepository()
}

val app by lazy {
    val div = document.createElement("div") as HTMLDivElement
    document.body?.appendChild(div)
    div
}

fun currentTimestamp(): String = js("new Date().toLocaleString()")
