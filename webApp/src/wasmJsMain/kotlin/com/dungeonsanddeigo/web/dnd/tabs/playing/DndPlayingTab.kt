package com.dungeonsanddeigo.web.dnd.tabs.playing

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.*
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.changeWeapon.DndChangeWeaponModal
import com.dungeonsanddeigo.web.dnd.modals.changeArmor.DndChangeArmorModal
import com.dungeonsanddeigo.web.dnd.modals.addConsumableLoot.DndAddConsumableLootModal
import com.dungeonsanddeigo.web.dnd.modals.preparedSpells.DndPreparedSpellsModal
import com.dungeonsanddeigo.web.dnd.modals.money.DndMoneyModal
import com.dungeonsanddeigo.web.dnd.modals.addStatus.DndAddStatusModal
import com.dungeonsanddeigo.web.dnd.modals.hitDice.DndHitDiceModal
import com.dungeonsanddeigo.web.dnd.modals.armor.DndArmorModal
import com.dungeonsanddeigo.web.dnd.modals.weapon.DndWeaponModal
import com.dungeonsanddeigo.web.dnd.modals.magicItem.DndMagicItemModal
import com.dungeonsanddeigo.web.dnd.modals.inventoryItem.DndInventoryItemModal
import com.dungeonsanddeigo.web.dnd.modals.note.DndNoteModal
import com.dungeonsanddeigo.web.dnd.components.search.DndSearch
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.*

fun renderDndPlayingTab(character: Character, container: HTMLDivElement) {
    val stats = Repos.baseStats.getByCharacterId(character.id) ?: DndBaseStats(characterId = character.id)
    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
    val maxLife = stats.maxLife ?: 0

    val lifeKey = "dnd_playing_life_${character.id}"
    val tempLifeKey = "dnd_playing_templife_${character.id}"
    val deathSaveKey = "dnd_playing_deathsave_${character.id}"
    val deathSuccessKey = "dnd_playing_deathsuccess_${character.id}"
    val deathFailKey = "dnd_playing_deathfail_${character.id}"
    var currentLife = localStorage.getItem(lifeKey)?.toIntOrNull() ?: maxLife
    var currentTempLife = localStorage.getItem(tempLifeKey)?.toIntOrNull() ?: 0
    val isInDeathSaves = localStorage.getItem(deathSaveKey) == "true"
    var deathSuccesses = localStorage.getItem(deathSuccessKey)?.toIntOrNull() ?: 0
    var deathFails = localStorage.getItem(deathFailKey)?.toIntOrNull() ?: 0

    fun saveLife() { localStorage.setItem(lifeKey, currentLife.toString()) }
    fun saveTempLife() { localStorage.setItem(tempLifeKey, currentTempLife.toString()) }
    fun saveDeathState() {
        localStorage.setItem(deathSaveKey, isInDeathSaves.toString())
        localStorage.setItem(deathSuccessKey, deathSuccesses.toString())
        localStorage.setItem(deathFailKey, deathFails.toString())
    }

    // Layout: 3/4 left + 1/4 right
    val layout = document.createElement("div") as HTMLDivElement
    layout.style.display = "flex"
    layout.style.setProperty("gap", "16px")

    val leftPanel = document.createElement("div") as HTMLDivElement
    leftPanel.style.setProperty("flex", "3")

    val rightPanel = document.createElement("div") as HTMLDivElement
    rightPanel.style.setProperty("flex", "1")

    // === LEFT PANEL ===

    // Life Tracker box
    val lifeBox = document.createElement("div") as HTMLDivElement
    lifeBox.style.border = "1px solid #ccc"
    lifeBox.style.borderRadius = "8px"
    lifeBox.style.padding = "12px"
    lifeBox.style.marginBottom = "16px"

    val lifeBoxTitle = document.createElement("h4") as HTMLHeadingElement
    lifeBoxTitle.textContent = t("playing.lifeTracker")
    lifeBoxTitle.style.margin = "0 0 10px 0"
    lifeBox.appendChild(lifeBoxTitle)

    // Life Points section
    val lifeSection = document.createElement("div") as HTMLDivElement
    lifeSection.style.display = "flex"
    lifeSection.style.setProperty("gap", "24px")
    lifeSection.style.alignItems = "center"
    lifeSection.style.marginBottom = "12px"

    val lifeCol = document.createElement("div") as HTMLDivElement
    lifeCol.style.textAlign = "center"
    val lifeLbl = document.createElement("div") as HTMLDivElement
    lifeLbl.textContent = t("playing.lifePoints")
    lifeLbl.style.fontWeight = "bold"; lifeLbl.style.fontSize = "12px"; lifeLbl.style.marginBottom = "4px"
    lifeCol.appendChild(lifeLbl)
    val lifeVal = document.createElement("span") as HTMLSpanElement
    lifeVal.textContent = currentLife.toString()
    lifeVal.style.fontSize = "28px"; lifeVal.style.fontWeight = "bold"
    lifeCol.appendChild(lifeVal)
    lifeSection.appendChild(lifeCol)

    val sep = document.createElement("span") as HTMLSpanElement
    sep.textContent = "/"; sep.style.fontSize = "24px"; sep.style.color = "#666"
    lifeSection.appendChild(sep)

    val maxCol = document.createElement("div") as HTMLDivElement
    maxCol.style.textAlign = "center"
    val maxLbl = document.createElement("div") as HTMLDivElement
    maxLbl.textContent = t("playing.max")
    maxLbl.style.fontWeight = "bold"; maxLbl.style.fontSize = "12px"; maxLbl.style.marginBottom = "4px"
    maxCol.appendChild(maxLbl)
    val maxValSpan = document.createElement("span") as HTMLSpanElement
    maxValSpan.textContent = maxLife.toString()
    maxValSpan.style.fontSize = "28px"; maxValSpan.style.fontWeight = "bold"
    maxCol.appendChild(maxValSpan)
    lifeSection.appendChild(maxCol)

    val sep2 = document.createElement("span") as HTMLSpanElement
    sep2.textContent = "+"; sep2.style.fontSize = "24px"; sep2.style.color = "#666"
    lifeSection.appendChild(sep2)

    val tempCol = document.createElement("div") as HTMLDivElement
    tempCol.style.textAlign = "center"
    val tempLbl = document.createElement("div") as HTMLDivElement
    tempLbl.textContent = t("playing.tempHP")
    tempLbl.style.fontWeight = "bold"; tempLbl.style.fontSize = "12px"; tempLbl.style.marginBottom = "4px"
    tempCol.appendChild(tempLbl)
    val tempVal = document.createElement("span") as HTMLSpanElement
    tempVal.textContent = currentTempLife.toString()
    tempVal.style.fontSize = "28px"; tempVal.style.fontWeight = "bold"
    tempCol.appendChild(tempVal)
    lifeSection.appendChild(tempCol)

    lifeBox.appendChild(lifeSection)

    var deathBtnRef: HTMLButtonElement? = null

    fun updateDisplay() {
        lifeVal.textContent = currentLife.toString()
        tempVal.textContent = currentTempLife.toString()
        deathBtnRef?.disabled = currentLife > 0
    }

    fun showValueModal(title: String, onConfirm: (Int) -> Unit) {
        val overlay = document.createElement("div") as HTMLDivElement
        overlay.style.position = "fixed"
        overlay.style.top = "0"; overlay.style.left = "0"
        overlay.style.width = "100%"; overlay.style.height = "100%"
        overlay.style.backgroundColor = "rgba(0,0,0,0.5)"
        overlay.style.display = "flex"
        overlay.style.justifyContent = "center"; overlay.style.alignItems = "center"
        overlay.style.setProperty("z-index", "1000")

        val modal = document.createElement("div") as HTMLDivElement
        modal.style.backgroundColor = "white"
        modal.style.borderRadius = "8px"; modal.style.padding = "24px"
        modal.style.maxWidth = "300px"; modal.style.width = "90%"

        val titleEl = document.createElement("h3") as HTMLHeadingElement
        titleEl.textContent = title
        modal.appendChild(titleEl)

        val input = document.createElement("input") as HTMLInputElement
        input.type = "number"; input.min = "0"; input.value = "0"
        input.style.width = "100%"; input.style.padding = "8px"
        input.style.fontSize = "18px"; input.style.textAlign = "center"
        modal.appendChild(input)

        val btnRow = document.createElement("div") as HTMLDivElement
        btnRow.style.display = "flex"; btnRow.style.setProperty("gap", "8px")
        btnRow.style.marginTop = "16px"; btnRow.style.justifyContent = "flex-end"

        val cancelBtn = document.createElement("button") as HTMLButtonElement
        cancelBtn.textContent = t("btn.cancel")
        cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
        btnRow.appendChild(cancelBtn)

        val confirmBtn = document.createElement("button") as HTMLButtonElement
        confirmBtn.textContent = t("playing.confirm")
        confirmBtn.addEventListener("click", {
            val value = input.value.toIntOrNull() ?: 0
            if (value > 0) onConfirm(value)
            document.body?.removeChild(overlay)
        })
        btnRow.appendChild(confirmBtn)

        modal.appendChild(btnRow)
        overlay.appendChild(modal)
        document.body?.appendChild(overlay)
        input.focus()
    }

    // Buttons
    val btnSection = document.createElement("div") as HTMLDivElement
    btnSection.style.display = "flex"
    btnSection.style.setProperty("gap", "8px")
    btnSection.style.marginBottom = "8px"

    val btnSection2 = document.createElement("div") as HTMLDivElement
    btnSection2.style.display = "flex"
    btnSection2.style.setProperty("gap", "8px")
    btnSection2.style.marginBottom = "16px"

    if (isInDeathSaves) {
        // Death Saving Throws mode
        lifeSection.style.display = "none"

        // Auto-stabilize on 3 successes
        if (deathSuccesses >= 3) {
            localStorage.setItem(deathSaveKey, "false")
            localStorage.setItem(deathSuccessKey, "0")
            localStorage.setItem(deathFailKey, "0")
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
            return
        }

        val deathSection = document.createElement("div") as HTMLDivElement
        deathSection.style.marginBottom = "12px"

        val deathTitle = document.createElement("div") as HTMLDivElement
        deathTitle.textContent = "\u2620\uFE0F Death Saving Throws"
        deathTitle.style.fontWeight = "bold"
        deathTitle.style.marginBottom = "10px"
        deathSection.appendChild(deathTitle)

        // Grid for aligned rows
        val deathGrid = document.createElement("div") as HTMLDivElement
        deathGrid.style.setProperty("display", "grid")
        deathGrid.style.setProperty("grid-template-columns", "100px 1fr")
        deathGrid.style.setProperty("gap", "6px 12px")
        deathGrid.style.alignItems = "center"
        deathGrid.style.marginBottom = "8px"

        // Success row
        val successLabel = document.createElement("span") as HTMLSpanElement
        successLabel.textContent = t("playing.successes")
        successLabel.style.fontSize = "14px"
        successLabel.style.fontWeight = "bold"
        deathGrid.appendChild(successLabel)
        val successIcons = document.createElement("span") as HTMLSpanElement
        successIcons.style.fontSize = "20px"
        successIcons.textContent = (1..3).joinToString(" ") { i -> if (i <= deathSuccesses) "\u2764\uFE0F" else "\u2B1C" }
        deathGrid.appendChild(successIcons)

        // Fail row
        val failLabel = document.createElement("span") as HTMLSpanElement
        failLabel.textContent = t("playing.failures")
        failLabel.style.fontSize = "14px"
        failLabel.style.fontWeight = "bold"
        deathGrid.appendChild(failLabel)
        val failIcons = document.createElement("span") as HTMLSpanElement
        failIcons.style.fontSize = "20px"
        failIcons.textContent = (1..3).joinToString(" ") { i -> if (i <= deathFails) "\u2620\uFE0F" else "\u2B1C" }
        deathGrid.appendChild(failIcons)

        deathSection.appendChild(deathGrid)
        lifeBox.insertBefore(deathSection, lifeBox.children[1])

        // Mark Fail button
        val dmgBtn = document.createElement("button") as HTMLButtonElement
        dmgBtn.textContent = "\u2620\uFE0F Mark Fail"
        dmgBtn.disabled = deathFails >= 3
        dmgBtn.addEventListener("click", {
            deathFails = minOf(deathFails + 1, 3)
            localStorage.setItem(deathFailKey, deathFails.toString())
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection.appendChild(dmgBtn)

        // Success button
        val successBtn = document.createElement("button") as HTMLButtonElement
        successBtn.textContent = "\u2764\uFE0F Success"
        successBtn.disabled = deathSuccesses >= 3
        successBtn.addEventListener("click", {
            deathSuccesses = minOf(deathSuccesses + 1, 3)
            localStorage.setItem(deathSuccessKey, deathSuccesses.toString())
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection.appendChild(successBtn)

        // Stable Creature button
        val stableBtn = document.createElement("button") as HTMLButtonElement
        stableBtn.textContent = "\uD83D\uDC9A Stable"
        stableBtn.addEventListener("click", {
            localStorage.setItem(deathSaveKey, "false")
            localStorage.setItem(deathSuccessKey, "0")
            localStorage.setItem(deathFailKey, "0")
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection.appendChild(stableBtn)
    } else {
        // Normal mode
        val cureBtn = document.createElement("button") as HTMLButtonElement
        cureBtn.textContent = "\u2764\uFE0F " + t("playing.cure")
        cureBtn.addEventListener("click", {
            showValueModal(t("playing.cure")) { value ->
                currentLife = minOf(currentLife + value, maxLife)
                saveLife(); updateDisplay()
            }
        })
        btnSection.appendChild(cureBtn)

        val dmgBtn = document.createElement("button") as HTMLButtonElement
        dmgBtn.textContent = "\u2694\uFE0F " + t("playing.dmg")
        dmgBtn.addEventListener("click", {
            showValueModal(t("playing.dmg")) { value ->
                var remaining = value
                if (currentTempLife > 0) {
                    val absorbed = minOf(remaining, currentTempLife)
                    currentTempLife -= absorbed
                    remaining -= absorbed
                    saveTempLife()
                }
                if (remaining > 0) {
                    currentLife = maxOf(currentLife - remaining, 0)
                    saveLife()
                }
                updateDisplay()
            }
        })
        btnSection.appendChild(dmgBtn)

        val tempBtn = document.createElement("button") as HTMLButtonElement
        tempBtn.textContent = "\uD83D\uDEE1\uFE0F " + t("playing.tempHP")
        tempBtn.addEventListener("click", {
            showValueModal(t("playing.tempHP")) { value ->
                currentTempLife += value
                saveTempLife(); updateDisplay()
            }
        })
        btnSection.appendChild(tempBtn)

        // Second row: Reset + Death Saves
        val resetBtn = document.createElement("button") as HTMLButtonElement
        resetBtn.textContent = "\u21BA " + t("playing.reset")
        resetBtn.addEventListener("click", {
            currentLife = maxLife
            currentTempLife = 0
            saveLife(); saveTempLife(); updateDisplay()
        })
        btnSection2.appendChild(resetBtn)

        val deathBtn = document.createElement("button") as HTMLButtonElement
        deathBtn.textContent = "\u2620\uFE0F"
        deathBtn.disabled = currentLife > 0
        deathBtn.addEventListener("click", {
            localStorage.setItem(deathSaveKey, "true")
            localStorage.setItem(deathSuccessKey, "0")
            localStorage.setItem(deathFailKey, "0")
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        })
        btnSection2.appendChild(deathBtn)
        deathBtnRef = deathBtn
    }

    lifeBox.appendChild(btnSection)
    if (!isInDeathSaves) {
        lifeBox.appendChild(btnSection2)
    }

    // Hit Dice section
    val hitDiceSection = document.createElement("div") as HTMLDivElement
    hitDiceSection.style.borderTop = "1px solid #ccc"
    hitDiceSection.style.marginTop = "12px"
    hitDiceSection.style.paddingTop = "12px"

    val hitDiceTitle = document.createElement("h5") as HTMLHeadingElement
    hitDiceTitle.textContent = t("playing.hitDice")
    hitDiceTitle.style.margin = "0 0 8px 0"
    hitDiceSection.appendChild(hitDiceTitle)

    // Build hit dice info per class
    data class HitDiceInfo(val className: String, val die: String, val maxDice: Int, val storageKey: String)

    val hitDiceInfos = mutableListOf<HitDiceInfo>()
    val mainClass = mainInfo?.mainClass
    val mainLevel = mainInfo?.mainClassLevel ?: 0
    if (mainClass != null && mainLevel > 0) {
        hitDiceInfos.add(HitDiceInfo(mainClass, DungeonsAndDragons.hitDieFor(mainClass), mainLevel, "dnd_hitdice_main_${character.id}"))
    }
    val secClass = mainInfo?.secondaryClass
    val secLevel = mainInfo?.secondaryClassLevel ?: 0
    if (secClass != null && secLevel > 0) {
        hitDiceInfos.add(HitDiceInfo(secClass, DungeonsAndDragons.hitDieFor(secClass), secLevel, "dnd_hitdice_sec_${character.id}"))
    }

    if (hitDiceInfos.isEmpty()) {
        val noInfo = document.createElement("p") as HTMLParagraphElement
        noInfo.textContent = t("playing.setClassLevel")
        noInfo.style.color = "#999"; noInfo.style.fontSize = "12px"
        hitDiceSection.appendChild(noInfo)
    } else {
        hitDiceInfos.forEach { hd ->
            var usedDice = localStorage.getItem(hd.storageKey)?.toIntOrNull() ?: 0
            val remaining = hd.maxDice - usedDice

            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.alignItems = "center"
            row.style.setProperty("gap", "8px")
            row.style.marginBottom = "6px"

            val infoSpan = document.createElement("span") as HTMLSpanElement
            infoSpan.textContent = "${tDnd("class", hd.className)} (${hd.die}): $remaining / ${hd.maxDice}"
            infoSpan.style.fontSize = "13px"
            infoSpan.style.fontWeight = "bold"
            if (remaining <= 0) infoSpan.style.color = "#c00"
            row.appendChild(infoSpan)

            val useHdBtn = document.createElement("button") as HTMLButtonElement
            useHdBtn.textContent = t("playing.use")
            useHdBtn.style.fontSize = "11px"
            useHdBtn.disabled = remaining <= 0
            useHdBtn.addEventListener("click", {
                DndHitDiceModal(hd.die, hd.storageKey, usedDice, character).show {
                    container.innerHTML = ""
                    renderDndPlayingTab(character, container)
                }
            })
            row.appendChild(useHdBtn)

            val recoverBtn = document.createElement("button") as HTMLButtonElement
            recoverBtn.textContent = t("playing.recover")
            recoverBtn.style.fontSize = "11px"
            recoverBtn.disabled = usedDice <= 0
            recoverBtn.addEventListener("click", {
                usedDice -= 1
                localStorage.setItem(hd.storageKey, usedDice.toString())
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            row.appendChild(recoverBtn)

            hitDiceSection.appendChild(row)
        }
    }

    lifeBox.appendChild(hitDiceSection)

    // Top row: Life Tracker + Attacks side by side
    val topRow = document.createElement("div") as HTMLDivElement
    topRow.style.display = "flex"
    topRow.style.setProperty("gap", "16px")
    topRow.style.marginBottom = "16px"

    topRow.appendChild(lifeBox)

    // Attacks box
    val atkBox = document.createElement("div") as HTMLDivElement
    atkBox.style.border = "1px solid #ccc"
    atkBox.style.borderRadius = "8px"
    atkBox.style.padding = "12px"
    atkBox.style.setProperty("flex", "1")

    val atkBoxTitle = document.createElement("h4") as HTMLHeadingElement
    atkBoxTitle.textContent = t("playing.attacks")
    atkBoxTitle.style.margin = "0"

    val atkHeader = document.createElement("div") as HTMLDivElement
    atkHeader.style.display = "flex"
    atkHeader.style.justifyContent = "space-between"
    atkHeader.style.alignItems = "center"
    atkHeader.style.marginBottom = "10px"
    atkHeader.appendChild(atkBoxTitle)

    val changeWeaponBtn = document.createElement("button") as HTMLButtonElement
    changeWeaponBtn.textContent = t("playing.changeWeapon")
    changeWeaponBtn.style.fontSize = "11px"
    changeWeaponBtn.addEventListener("click", {
        DndChangeWeaponModal(character).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    atkHeader.appendChild(changeWeaponBtn)
    atkBox.appendChild(atkHeader)

    // Gather weapon proficiencies
    val features = Repos.features.getByCharacterId(character.id)
    val weaponCatProfs = mutableSetOf<String>()
    val weaponSpecificProfs = mutableSetOf<String>()
    features.filter { it.type == "Weapon/Armor Proficiency" }.forEach { f ->
        f.description.split(",").map { it.trim() }.forEach { entry ->
            when {
                entry.startsWith("weapon_cat:") -> weaponCatProfs.add(entry.removePrefix("weapon_cat:"))
                entry.startsWith("weapon:") -> weaponSpecificProfs.add(entry.removePrefix("weapon:"))
            }
        }
    }

    val weapons = Repos.weapon.getByCharacterId(character.id)
    val equippedWeapon = weapons.firstOrNull { it.isEquipped }

    if (equippedWeapon != null) {
        val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
        val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
        val profBonus = calcProficiency(totalLevel)
        val strMod = stats.strValue?.let { calcModifier(it) } ?: 0
        val dexMod = stats.dexValue?.let { calcModifier(it) } ?: 0

        val hasProfEq = equippedWeapon.category in weaponCatProfs
                || equippedWeapon.weaponType in weaponSpecificProfs
        val isRanged = equippedWeapon.range && !equippedWeapon.thrown

        val atkTable = document.createElement("table") as HTMLTableElement
        atkTable.style.width = "100%"
        atkTable.style.fontSize = "12px"
        atkTable.style.borderCollapse = "collapse"

        val thead = document.createElement("thead")
        val headerRow = document.createElement("tr") as HTMLTableRowElement
        listOf(t("inv.atkTable.name"), t("inv.atkTable.range"), t("inv.atkTable.test"), t("inv.atkTable.damage"), t("inv.atkTable.notes")).forEach { h ->
            val th = document.createElement("th") as HTMLTableCellElement
            th.textContent = h
            th.style.textAlign = "left"
            th.style.padding = "4px"
            th.style.borderBottom = "1px solid #ccc"
            th.style.fontSize = "11px"
            th.style.color = "#666"
            headerRow.appendChild(th)
        }
        thead.appendChild(headerRow)
        atkTable.appendChild(thead)

        val tbody = document.createElement("tbody")

        fun addAtkRow(useDex: Boolean, twoHanded: Boolean = false, thrown: Boolean = false) {
            val mod = if (useDex) dexMod else strMod
            val atkMod = mod + (if (hasProfEq) profBonus else 0)

            val tr = document.createElement("tr") as HTMLTableRowElement
            tr.classList.add("weapon-atk-row")

            val tdName = document.createElement("td") as HTMLTableCellElement
            tdName.style.padding = "3px 4px"; tdName.style.fontWeight = "bold"
            val statLabel = if (equippedWeapon.finesse) " (${if (useDex) tStat("Dex") else tStat("Str")})" else ""
            val handLabel = if (twoHanded) " [${t("inv.twoHandedShort")}]" else ""
            val thrownLabel = if (thrown) " [${t("inv.thrownShort")}]" else ""
            tdName.textContent = "${equippedWeapon.name}$statLabel$handLabel$thrownLabel"
            tr.appendChild(tdName)

            val tdRange = document.createElement("td") as HTMLTableCellElement
            tdRange.style.padding = "3px 4px"
            tdRange.textContent = if (isRanged || thrown) "${equippedWeapon.rangeDistance}/${equippedWeapon.rangeLongDistance}m" else t("inv.melee")
            tr.appendChild(tdRange)

            val tdTest = document.createElement("td") as HTMLTableCellElement
            tdTest.style.padding = "3px 4px"
            val testSign = if (atkMod >= 0) "+" else ""
            tdTest.textContent = "$testSign$atkMod"
            tr.appendChild(tdTest)

            val tdDmg = document.createElement("td") as HTMLTableCellElement
            tdDmg.style.padding = "3px 4px"
            val dice = if (twoHanded) equippedWeapon.versatileDice else equippedWeapon.damageDice
            val dmgStr = if (isRanged || thrown) {
                "$dice ${when(equippedWeapon.damageType) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> equippedWeapon.damageType }}"
            } else {
                val modSign = if (mod >= 0) "+" else ""
                "$dice$modSign$mod ${when(equippedWeapon.damageType) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> equippedWeapon.damageType }}"
            }
            tdDmg.textContent = dmgStr
            tr.appendChild(tdDmg)

            val tdNotes = document.createElement("td") as HTMLTableCellElement
            tdNotes.style.padding = "3px 4px"; tdNotes.style.color = "#888"
            val icons = mutableListOf<String>()
            if (equippedWeapon.silver) icons.add("\uD83E\uDD48")
            if (equippedWeapon.heavy) icons.add("\u2693")
            if (equippedWeapon.light) icons.add("\uD83E\uDEB6")
            if (equippedWeapon.reach) icons.add("\uD83D\uDCAB")
            if (equippedWeapon.ammunition) icons.add("\uD83C\uDFF9")
            if (equippedWeapon.loading) icons.add("\u23F3")
            val extras = mutableListOf<String>()
            if (icons.isNotEmpty()) extras.add(icons.joinToString(""))
            if (equippedWeapon.additionalFeatures.isNotEmpty()) {
                val maxLen = 50 - (extras.firstOrNull()?.length ?: 0)
                val feat = if (equippedWeapon.additionalFeatures.length > maxLen)
                    equippedWeapon.additionalFeatures.take(maxLen) + "..." else equippedWeapon.additionalFeatures
                extras.add(feat)
            }
            tdNotes.textContent = extras.joinToString(" ")
            tr.appendChild(tdNotes)

            tbody.appendChild(tr)
        }

        if (isRanged) {
            addAtkRow(useDex = true)
        } else if (equippedWeapon.finesse) {
            addAtkRow(useDex = false)
            if (equippedWeapon.versatile) addAtkRow(useDex = false, twoHanded = true)
            addAtkRow(useDex = true)
            if (equippedWeapon.versatile) addAtkRow(useDex = true, twoHanded = true)
            if (equippedWeapon.thrown) {
                addAtkRow(useDex = false, thrown = true)
                addAtkRow(useDex = true, thrown = true)
            }
        } else {
            addAtkRow(useDex = false)
            if (equippedWeapon.versatile) addAtkRow(useDex = false, twoHanded = true)
            if (equippedWeapon.thrown) addAtkRow(useDex = false, thrown = true)
        }

        // Disarmed Attack row
        val disarmedTr = document.createElement("tr") as HTMLTableRowElement
        val disName = document.createElement("td") as HTMLTableCellElement
        disName.style.padding = "3px 4px"; disName.style.fontWeight = "bold"
        disName.textContent = t("combat.disarmedAttack")
        disarmedTr.appendChild(disName)
        val disRange = document.createElement("td") as HTMLTableCellElement
        disRange.style.padding = "3px 4px"; disRange.textContent = t("inv.melee")
        disarmedTr.appendChild(disRange)
        val disTest = document.createElement("td") as HTMLTableCellElement
        disTest.style.padding = "3px 4px"
        val disAtkMod = strMod + profBonus
        val disTestSign = if (disAtkMod >= 0) "+" else ""
        disTest.textContent = "$disTestSign$disAtkMod"
        disarmedTr.appendChild(disTest)
        val disDmg = document.createElement("td") as HTMLTableCellElement
        disDmg.style.padding = "3px 4px"
        val disDmgStr = if (stats.disarmedDice == "Normal") {
            "${maxOf(1, 1 + strMod)} ${t("inv.dmg.bludgeoning")}"
        } else {
            val modSign = if (strMod >= 0) "+" else ""
            "${stats.disarmedDice}$modSign$strMod ${t("inv.dmg.bludgeoning")}"
        }
        disDmg.textContent = disDmgStr
        disarmedTr.appendChild(disDmg)
        val disNotes = document.createElement("td") as HTMLTableCellElement
        disNotes.style.padding = "3px 4px"
        disarmedTr.appendChild(disNotes)
        tbody.appendChild(disarmedTr)

        // Add spell attack rows to the same tbody
        val attackSpells = Repos.spell.getByCharacterId(character.id).filter { it.isAttack && (it.circle == "Cantrip" || it.isPrepared || it.originClass !in DungeonsAndDragons.preparedCasters) }
        attackSpells.forEach { spell ->
            val tr = document.createElement("tr") as HTMLTableRowElement
            tr.classList.add("magic-atk-row")
            tr.setAttribute("data-spell-id", spell.id.toString())
            val tdName = document.createElement("td") as HTMLTableCellElement
            tdName.style.padding = "3px 4px"; tdName.style.fontWeight = "bold"
            val circleStr = if (spell.circle == "Cantrip") "" else " (${tCircle(spell.circle)})"
            tdName.textContent = "\u2728 ${spell.name}$circleStr"
            tr.appendChild(tdName)
            val tdRange = document.createElement("td") as HTMLTableCellElement
            tdRange.style.padding = "3px 4px"
            tdRange.textContent = if (spell.range.isNotEmpty()) "${spell.range}m" else "\u2014"
            tr.appendChild(tdRange)
            val tdTest = document.createElement("td") as HTMLTableCellElement
            tdTest.style.padding = "3px 4px"
            if (spell.needsSavingThrow) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val tl = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
                val dc = abMod + calcProficiency(tl) + 8
                tdTest.textContent = "${tStat(spell.savingThrowAbility)} ${t("magic.saveAbility")} (${t("playing.dc")} $dc)"
            } else if (spell.isAttack) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val tl = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
                val atkMod = abMod + calcProficiency(tl)
                tdTest.textContent = if (atkMod >= 0) "+$atkMod" else "$atkMod"
            }
            tr.appendChild(tdTest)
            val tdDmg = document.createElement("td") as HTMLTableCellElement
            tdDmg.style.padding = "3px 4px"
            tdDmg.textContent = if (spell.attackDamageDice.isNotEmpty()) "${spell.attackDamageDice} ${tSpellDamageType(spell.attackDamageType)}" else "\u2014"
            tr.appendChild(tdDmg)
            val tdNotes = document.createElement("td") as HTMLTableCellElement
            tdNotes.style.padding = "3px 4px"; tdNotes.style.fontSize = "10px"; tdNotes.style.color = "#666"
            val notes = mutableListOf<String>()
            val components = mutableListOf<String>()
            if (spell.hasVerbal) components.add("V")
            if (spell.hasSomatic) components.add("S")
            if (spell.hasMaterial) components.add("M")
            if (components.isNotEmpty()) notes.add(components.joinToString(""))
            if (spell.hasMaterial && spell.materialComponents.isNotEmpty()) notes.add(spell.materialComponents)
            if (spell.needsConcentration) notes.add(t("magic.concShort"))
            if (spell.canBeRitual) notes.add(t("magic.ritualShort"))
            if (spell.higherCircles.isNotEmpty()) notes.add("⬆️ ${t("magic.higherShort")}")
            tdNotes.textContent = notes.joinToString(", ")
            tr.appendChild(tdNotes)
            tbody.appendChild(tr)
        }

        atkTable.appendChild(tbody)
        atkBox.appendChild(atkTable)
    } else {
        // No weapon equipped - still show disarmed attack
        val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
        val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
        val profBonus = calcProficiency(totalLevel)
        val strMod = stats.strValue?.let { calcModifier(it) } ?: 0

        val atkTable = document.createElement("table") as HTMLTableElement
        atkTable.style.width = "100%"
        atkTable.style.fontSize = "12px"
        atkTable.style.borderCollapse = "collapse"

        val thead = document.createElement("thead")
        val headerRow = document.createElement("tr") as HTMLTableRowElement
        listOf(t("inv.atkTable.name"), t("inv.atkTable.range"), t("inv.atkTable.test"), t("inv.atkTable.damage"), t("inv.atkTable.notes")).forEach { h ->
            val th = document.createElement("th") as HTMLTableCellElement
            th.textContent = h
            th.style.textAlign = "left"
            th.style.padding = "4px"
            th.style.borderBottom = "1px solid #ccc"
            th.style.fontSize = "11px"
            th.style.color = "#666"
            headerRow.appendChild(th)
        }
        thead.appendChild(headerRow)
        atkTable.appendChild(thead)

        val tbody = document.createElement("tbody")
        val disarmedTr = document.createElement("tr") as HTMLTableRowElement
        val disName = document.createElement("td") as HTMLTableCellElement
        disName.style.padding = "3px 4px"; disName.style.fontWeight = "bold"
        disName.textContent = t("combat.disarmedAttack")
        disarmedTr.appendChild(disName)
        val disRange = document.createElement("td") as HTMLTableCellElement
        disRange.style.padding = "3px 4px"; disRange.textContent = t("inv.melee")
        disarmedTr.appendChild(disRange)
        val disTest = document.createElement("td") as HTMLTableCellElement
        disTest.style.padding = "3px 4px"
        val disAtkMod = strMod + profBonus
        val disTestSign = if (disAtkMod >= 0) "+" else ""
        disTest.textContent = "$disTestSign$disAtkMod"
        disarmedTr.appendChild(disTest)
        val disDmg = document.createElement("td") as HTMLTableCellElement
        disDmg.style.padding = "3px 4px"
        val disDmgStr2 = if (stats.disarmedDice == "Normal") {
            "${maxOf(1, 1 + strMod)} ${t("inv.dmg.bludgeoning")}"
        } else {
            val modSign = if (strMod >= 0) "+" else ""
            "${stats.disarmedDice}$modSign$strMod ${t("inv.dmg.bludgeoning")}"
        }
        disDmg.textContent = disDmgStr2
        disarmedTr.appendChild(disDmg)
        val disNotes = document.createElement("td") as HTMLTableCellElement
        disNotes.style.padding = "3px 4px"
        disarmedTr.appendChild(disNotes)
        tbody.appendChild(disarmedTr)

        // Add spell attack rows to the same tbody
        val attackSpells2 = Repos.spell.getByCharacterId(character.id).filter { it.isAttack && (it.circle == "Cantrip" || it.isPrepared || it.originClass !in DungeonsAndDragons.preparedCasters) }
        attackSpells2.forEach { spell ->
            val tr = document.createElement("tr") as HTMLTableRowElement
            tr.classList.add("magic-atk-row")
            tr.setAttribute("data-spell-id", spell.id.toString())
            val tdName = document.createElement("td") as HTMLTableCellElement
            tdName.style.padding = "3px 4px"; tdName.style.fontWeight = "bold"
            val circleStr = if (spell.circle == "Cantrip") "" else " (${tCircle(spell.circle)})"
            tdName.textContent = "\u2728 ${spell.name}$circleStr"
            tr.appendChild(tdName)
            val tdRange = document.createElement("td") as HTMLTableCellElement
            tdRange.style.padding = "3px 4px"
            tdRange.textContent = if (spell.range.isNotEmpty()) "${spell.range}m" else "\u2014"
            tr.appendChild(tdRange)
            val tdTest = document.createElement("td") as HTMLTableCellElement
            tdTest.style.padding = "3px 4px"
            if (spell.needsSavingThrow) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val dc = abMod + profBonus + 8
                tdTest.textContent = "${tStat(spell.savingThrowAbility)} ${t("magic.saveAbility")} (${t("playing.dc")} $dc)"
            } else if (spell.isAttack) {
                val magicAb = DungeonsAndDragons.spellcastingAbilityFor(mainInfo?.mainClass, mainInfo?.mainSubClass)
                    ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_${mainInfo?.mainClass}")?.takeIf { it != "__none__" }
                val abMod = when (magicAb) {
                    "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
                    "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
                    "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
                    else -> 0
                }
                val atkMod = abMod + profBonus
                tdTest.textContent = if (atkMod >= 0) "+$atkMod" else "$atkMod"
            }
            tr.appendChild(tdTest)
            val tdDmg = document.createElement("td") as HTMLTableCellElement
            tdDmg.style.padding = "3px 4px"
            tdDmg.textContent = if (spell.attackDamageDice.isNotEmpty()) "${spell.attackDamageDice} ${tSpellDamageType(spell.attackDamageType)}" else "\u2014"
            tr.appendChild(tdDmg)
            val tdNotes = document.createElement("td") as HTMLTableCellElement
            tdNotes.style.padding = "3px 4px"; tdNotes.style.fontSize = "10px"; tdNotes.style.color = "#666"
            val notes = mutableListOf<String>()
            val components = mutableListOf<String>()
            if (spell.hasVerbal) components.add("V")
            if (spell.hasSomatic) components.add("S")
            if (spell.hasMaterial) components.add("M")
            if (components.isNotEmpty()) notes.add(components.joinToString(""))
            if (spell.hasMaterial && spell.materialComponents.isNotEmpty()) notes.add(spell.materialComponents)
            if (spell.needsConcentration) notes.add(t("magic.concShort"))
            if (spell.canBeRitual) notes.add(t("magic.ritualShort"))
            if (spell.higherCircles.isNotEmpty()) notes.add("⬆️ ${t("magic.higherShort")}")
            tdNotes.textContent = notes.joinToString(", ")
            tr.appendChild(tdNotes)
            tbody.appendChild(tr)
        }

        atkTable.appendChild(tbody)
        atkBox.appendChild(atkTable)
    }

    // Ammunition section
    val ammoItems = Repos.consumable.getByCharacterId(character.id).filter { it.type == "Ammunition" }
    if (ammoItems.isNotEmpty()) {
        val ammoSection = document.createElement("div") as HTMLDivElement
        ammoSection.style.borderTop = "1px solid #eee"
        ammoSection.style.marginTop = "10px"
        ammoSection.style.paddingTop = "8px"

        val ammoTitle = document.createElement("div") as HTMLDivElement
        ammoTitle.textContent = t("playing.ammunition")
        ammoTitle.style.fontWeight = "bold"
        ammoTitle.style.fontSize = "12px"
        ammoTitle.style.marginBottom = "6px"
        ammoSection.appendChild(ammoTitle)

        ammoItems.forEach { ammo ->
            val row = document.createElement("div") as HTMLDivElement
            row.style.display = "flex"
            row.style.alignItems = "center"
            row.style.setProperty("gap", "8px")
            row.style.marginBottom = "4px"

            val nameSpan = document.createElement("span") as HTMLSpanElement
            nameSpan.textContent = ammo.name
            nameSpan.style.fontSize = "12px"
            nameSpan.style.setProperty("flex", "1")
            row.appendChild(nameSpan)

            val minusBtn = document.createElement("button") as HTMLButtonElement
            minusBtn.textContent = "\u2212"
            minusBtn.style.fontSize = "11px"
            minusBtn.style.width = "24px"
            minusBtn.disabled = ammo.quantity <= 0
            minusBtn.addEventListener("click", {
                Repos.consumable.save(ammo.copy(quantity = maxOf(ammo.quantity - 1, 0)))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            row.appendChild(minusBtn)

            val qtySpan = document.createElement("span") as HTMLSpanElement
            qtySpan.textContent = ammo.quantity.toString()
            qtySpan.style.fontSize = "13px"
            qtySpan.style.fontWeight = "bold"
            qtySpan.style.minWidth = "20px"
            qtySpan.style.textAlign = "center"
            row.appendChild(qtySpan)

            val plusBtn = document.createElement("button") as HTMLButtonElement
            plusBtn.textContent = "+"
            plusBtn.style.fontSize = "11px"
            plusBtn.style.width = "24px"
            plusBtn.addEventListener("click", {
                Repos.consumable.save(ammo.copy(quantity = ammo.quantity + 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            row.appendChild(plusBtn)

            ammoSection.appendChild(row)
        }
        atkBox.appendChild(ammoSection)
    }

    topRow.appendChild(atkBox)

    // Special Actions box (right of Attacks)
    val specialBox = document.createElement("div") as HTMLDivElement
    specialBox.style.border = "1px solid #ccc"
    specialBox.style.borderRadius = "8px"
    specialBox.style.padding = "12px"
    specialBox.style.setProperty("flex", "0.4")

    val specialTitle = document.createElement("h4") as HTMLHeadingElement
    specialTitle.textContent = t("playing.specialActions")
    specialTitle.style.margin = "0 0 10px 0"
    specialBox.appendChild(specialTitle)

    val rechargeables = features.filter { it.type == "Rechargable Feature" }
    if (rechargeables.isEmpty()) {
        val placeholder = document.createElement("p") as HTMLParagraphElement
        placeholder.textContent = t("playing.noRechargeable")
        placeholder.style.color = "#999"
        placeholder.style.fontSize = "13px"
        specialBox.appendChild(placeholder)
    } else {
        rechargeables.forEach { feat ->
            val row = document.createElement("div") as HTMLDivElement
            row.setAttribute("data-feature-id", feat.id.toString())
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "6px 0"
            row.style.borderBottom = "1px solid #eee"

            val infoDiv = document.createElement("div") as HTMLDivElement
            val nameSpan = document.createElement("span") as HTMLSpanElement
            nameSpan.textContent = feat.name
            nameSpan.style.fontWeight = "bold"
            nameSpan.style.fontSize = "13px"
            infoDiv.appendChild(nameSpan)

            val usageSpan = document.createElement("span") as HTMLSpanElement
            val remaining = (feat.maxQuantity ?: 0) - feat.currentUsages
            usageSpan.textContent = " ($remaining / ${feat.maxQuantity ?: 0})"
            usageSpan.style.fontSize = "12px"
            usageSpan.style.color = if (remaining <= 0) "#c00" else "#666"
            infoDiv.appendChild(usageSpan)

            row.appendChild(infoDiv)

            val btnsDiv = document.createElement("div") as HTMLDivElement
            btnsDiv.style.display = "flex"
            btnsDiv.style.setProperty("gap", "4px")

            val useBtn = document.createElement("button") as HTMLButtonElement
            useBtn.textContent = "\u25BC"
            useBtn.style.fontSize = "11px"
            useBtn.disabled = remaining <= 0
            useBtn.addEventListener("click", {
                val updated = feat.copy(currentUsages = feat.currentUsages + 1)
                Repos.features.save(updated)
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(useBtn)

            val rechargeBtn = document.createElement("button") as HTMLButtonElement
            rechargeBtn.textContent = "\u21BA"
            rechargeBtn.style.fontSize = "11px"
            rechargeBtn.disabled = feat.currentUsages <= 0
            rechargeBtn.addEventListener("click", {
                val updated = feat.copy(currentUsages = 0)
                Repos.features.save(updated)
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(rechargeBtn)

            row.appendChild(btnsDiv)

            specialBox.appendChild(row)
        }
    }

    // Healing & Magic Potions section
    val allPotions = Repos.consumable.getByCharacterId(character.id).filter { it.type == "Healing Potion" || it.type == "Magic Potion" }
    if (allPotions.isNotEmpty()) {
        allPotions.forEach { potion ->
            val row = document.createElement("div") as HTMLDivElement
            row.setAttribute("data-consumable-id", potion.id.toString())
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "4px 0"
            row.style.borderBottom = "1px solid #f5f5f5"

            val infoDiv = document.createElement("div") as HTMLDivElement
            val nameSpan = document.createElement("span") as HTMLSpanElement
            val displayText = if (potion.type == "Healing Potion" && potion.effect.isNotEmpty()) {
                "${t("playing.drink")} ${potion.name} (${potion.effect})"
            } else {
                "${t("playing.drink")} ${potion.name}"
            }
            nameSpan.textContent = displayText
            nameSpan.style.fontSize = "12px"
            infoDiv.appendChild(nameSpan)
            val qtySpan = document.createElement("span") as HTMLSpanElement
            qtySpan.textContent = " x${potion.quantity}"
            qtySpan.style.fontSize = "11px"
            qtySpan.style.color = "#666"
            infoDiv.appendChild(qtySpan)
            row.appendChild(infoDiv)

            val btnsDiv = document.createElement("div") as HTMLDivElement
            btnsDiv.style.display = "flex"
            btnsDiv.style.setProperty("gap", "4px")

            val drinkBtn = document.createElement("button") as HTMLButtonElement
            drinkBtn.textContent = "\uD83E\uDDEA"
            drinkBtn.title = "Drink 1"
            drinkBtn.style.fontSize = "11px"
            drinkBtn.disabled = potion.quantity <= 0
            drinkBtn.addEventListener("click", {
                Repos.consumable.save(potion.copy(quantity = potion.quantity - 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(drinkBtn)

            val addBtn = document.createElement("button") as HTMLButtonElement
            addBtn.textContent = "\u2795"
            addBtn.title = "Add 1 to stock"
            addBtn.style.fontSize = "11px"
            addBtn.addEventListener("click", {
                Repos.consumable.save(potion.copy(quantity = potion.quantity + 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(addBtn)

            row.appendChild(btnsDiv)
            specialBox.appendChild(row)
        }
    }

    // Other consumable items
    val otherItems = Repos.consumable.getByCharacterId(character.id).filter { it.type == "Other" }
    if (otherItems.isNotEmpty()) {
        otherItems.forEach { item ->
            val row = document.createElement("div") as HTMLDivElement
            row.setAttribute("data-consumable-id", item.id.toString())
            row.style.display = "flex"
            row.style.justifyContent = "space-between"
            row.style.alignItems = "center"
            row.style.padding = "6px 0"
            row.style.borderBottom = "1px solid #eee"

            val infoDiv = document.createElement("div") as HTMLDivElement
            val nameSpan = document.createElement("span") as HTMLSpanElement
            nameSpan.textContent = "${t("playing.use")} ${item.name}"
            nameSpan.style.fontSize = "12px"
            infoDiv.appendChild(nameSpan)
            val qtySpan = document.createElement("span") as HTMLSpanElement
            qtySpan.textContent = " x${item.quantity}"
            qtySpan.style.fontSize = "11px"
            qtySpan.style.color = "#666"
            infoDiv.appendChild(qtySpan)
            row.appendChild(infoDiv)

            val btnsDiv = document.createElement("div") as HTMLDivElement
            btnsDiv.style.display = "flex"
            btnsDiv.style.setProperty("gap", "4px")

            val useBtn = document.createElement("button") as HTMLButtonElement
            useBtn.textContent = "\u25BC"
            useBtn.title = "Use 1"
            useBtn.style.fontSize = "11px"
            useBtn.disabled = item.quantity <= 0
            useBtn.addEventListener("click", {
                Repos.consumable.save(item.copy(quantity = item.quantity - 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(useBtn)

            val addBtn = document.createElement("button") as HTMLButtonElement
            addBtn.textContent = "\u2795"
            addBtn.title = "Add 1 to stock"
            addBtn.style.fontSize = "11px"
            addBtn.addEventListener("click", {
                Repos.consumable.save(item.copy(quantity = item.quantity + 1))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            btnsDiv.appendChild(addBtn)

            row.appendChild(btnsDiv)
            specialBox.appendChild(row)
        }
    }

    // Non-attack spells (prepared or always available)
    val nonAttackSpells = Repos.spell.getByCharacterId(character.id).filter {
        !it.isAttack && (it.circle == "Cantrip" || it.isPrepared || it.originClass !in DungeonsAndDragons.preparedCasters)
    }
    nonAttackSpells.forEach { spell ->
        val row = document.createElement("div") as HTMLDivElement
        row.setAttribute("data-spell-id", spell.id.toString())
        row.style.padding = "4px 0"
        row.style.borderBottom = "1px solid #f5f5f5"
        row.style.fontSize = "12px"

        val text = if (spell.circle == "Cantrip") {
            "\u2728 ${t("playing.cast")} ${spell.name}"
        } else {
            val higherStr = if (spell.higherCircles.isNotEmpty()) " \uD83C\uDD99" else ""
            val ritualStr = if (spell.canBeRitual) "/Ritual" else ""
            "\u2728 ${t("playing.cast")} ${spell.name} (${tCircle(spell.circle)}$higherStr$ritualStr)"
        }
        row.textContent = text
        specialBox.appendChild(row)
    }

    topRow.appendChild(specialBox)
    leftPanel.appendChild(topRow)

    // === DEFENSE AND STATUS SECTION ===
    val defenseBox = document.createElement("div") as HTMLDivElement
    defenseBox.style.border = "1px solid #ccc"
    defenseBox.style.borderRadius = "8px"
    defenseBox.style.padding = "12px"
    defenseBox.style.setProperty("flex", "1")

    // Header
    val defHeader = document.createElement("div") as HTMLDivElement
    defHeader.style.display = "flex"
    defHeader.style.justifyContent = "space-between"
    defHeader.style.alignItems = "center"
    defHeader.style.marginBottom = "10px"
    val defTitle = document.createElement("h4") as HTMLHeadingElement
    defTitle.textContent = t("playing.defenseStatus"); defTitle.style.margin = "0"
    defHeader.appendChild(defTitle)
    val changeArmorBtn = document.createElement("button") as HTMLButtonElement
    changeArmorBtn.textContent = t("playing.changeArmor")
    changeArmorBtn.style.fontSize = "11px"
    changeArmorBtn.addEventListener("click", {
        DndChangeArmorModal(character).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    defHeader.appendChild(changeArmorBtn)
    defenseBox.appendChild(defHeader)

    // Numbers row: AC + Initiative
    val armors = Repos.armor.getByCharacterId(character.id)
    val equippedArmor = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
    val equippedShield = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
    val dexMod = stats.dexValue?.let { calcModifier(it) } ?: 0
    val noArmorAC = stats.emptyArmorClass ?: 10
    val shieldAC = equippedShield?.baseAC ?: 0

    val totalAC = if (equippedArmor == null) {
        noArmorAC + shieldAC
    } else {
        val modBonus = when (equippedArmor.acModifier) {
            "Dex" -> dexMod
            "Dex (Max: 2)" -> minOf(dexMod, 2)
            else -> 0
        }
        equippedArmor.baseAC + modBonus + shieldAC
    }

    val numbersRow = document.createElement("div") as HTMLDivElement
    numbersRow.style.display = "flex"
    numbersRow.style.setProperty("gap", "24px")
    numbersRow.style.marginBottom = "12px"

    // AC
    val acCol = document.createElement("div") as HTMLDivElement
    acCol.style.textAlign = "center"
    val acNum = document.createElement("div") as HTMLDivElement
    acNum.textContent = totalAC.toString()
    acNum.style.fontSize = "32px"; acNum.style.fontWeight = "bold"
    acCol.appendChild(acNum)
    val acIcons = document.createElement("div") as HTMLDivElement
    acIcons.style.fontSize = "11px"; acIcons.style.color = "#666"
    val armorIcon = if (equippedArmor != null) "\uD83E\uDE96" else "\uD83D\uDC55"
    val shieldIcon = if (equippedShield != null) " \uD83D\uDEE1\uFE0F" else ""
    acIcons.textContent = "CA $armorIcon$shieldIcon"
    acCol.appendChild(acIcons)
    numbersRow.appendChild(acCol)

    // Initiative
    val initCol = document.createElement("div") as HTMLDivElement
    initCol.style.textAlign = "center"
    val initNum = document.createElement("div") as HTMLDivElement
    val initVal = stats.dexValue?.let { calcModifier(it) } ?: 0
    initNum.textContent = if (initVal >= 0) "+$initVal" else "$initVal"
    initNum.style.fontSize = "32px"; initNum.style.fontWeight = "bold"
    initCol.appendChild(initNum)
    val initLabel = document.createElement("div") as HTMLDivElement
    initLabel.textContent = t("stats.initiative")
    initLabel.style.fontSize = "11px"; initLabel.style.color = "#666"
    initCol.appendChild(initLabel)
    numbersRow.appendChild(initCol)

    defenseBox.appendChild(numbersRow)

    // Status section
    val statusHeader = document.createElement("div") as HTMLDivElement
    statusHeader.style.display = "flex"
    statusHeader.style.justifyContent = "space-between"
    statusHeader.style.alignItems = "center"
    statusHeader.style.marginBottom = "6px"
    statusHeader.style.borderTop = "1px solid #eee"
    statusHeader.style.paddingTop = "8px"
    val statusTitle = document.createElement("span") as HTMLSpanElement
    statusTitle.textContent = "Status"; statusTitle.style.fontWeight = "bold"; statusTitle.style.fontSize = "13px"
    statusHeader.appendChild(statusTitle)
    val addStatusBtn = document.createElement("button") as HTMLButtonElement
    addStatusBtn.textContent = t("inv.add")
    addStatusBtn.style.fontSize = "11px"
    addStatusBtn.addEventListener("click", {
        DndAddStatusModal(character).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    statusHeader.appendChild(addStatusBtn)
    defenseBox.appendChild(statusHeader)

    val statusKey = "dnd_playing_status_${character.id}"
    val statuses = localStorage.getItem(statusKey)?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    if (statuses.isEmpty()) {
        val noStatus = document.createElement("span") as HTMLSpanElement
        noStatus.textContent = t("playing.noActiveStatus")
        noStatus.style.color = "#999"; noStatus.style.fontSize = "12px"
        defenseBox.appendChild(noStatus)
    } else {
        val statusList = document.createElement("div") as HTMLDivElement
        statusList.style.display = "flex"
        statusList.style.setProperty("flex-wrap", "wrap")
        statusList.style.setProperty("gap", "4px")
        statuses.forEach { status ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "${tStatus(status)} \u00D7"
            badge.style.backgroundColor = "#ffe0e0"
            badge.style.padding = "2px 8px"
            badge.style.borderRadius = "4px"
            badge.style.fontSize = "12px"
            badge.style.cursor = "pointer"
            badge.addEventListener("click", {
                val updated = statuses.filter { it != status }
                localStorage.setItem(statusKey, updated.joinToString(","))
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            })
            statusList.appendChild(badge)
        }
        defenseBox.appendChild(statusList)
    }

    // Bottom row: Defense + Magics side by side
    val bottomRow = document.createElement("div") as HTMLDivElement
    bottomRow.style.display = "flex"
    bottomRow.style.setProperty("gap", "16px")
    bottomRow.style.marginBottom = "16px"

    bottomRow.appendChild(defenseBox)

    // Magics box
    val magicsBox = document.createElement("div") as HTMLDivElement
    magicsBox.style.border = "1px solid #ccc"
    magicsBox.style.borderRadius = "8px"
    magicsBox.style.padding = "12px"
    magicsBox.style.setProperty("flex", "1")

    val magicsTitle = document.createElement("h4") as HTMLHeadingElement
    magicsTitle.textContent = t("playing.magics")
    magicsTitle.style.margin = "0 0 10px 0"
    magicsBox.appendChild(magicsTitle)

    // Get spellcasting info
    val magicMainClass = mainInfo?.mainClass
    val magicMainSub = mainInfo?.mainSubClass
    val magicAbility = DungeonsAndDragons.spellcastingAbilityFor(magicMainClass, magicMainSub)
        ?: localStorage.getItem("dnd_custom_spell_ability_${character.id}_$magicMainClass")?.takeIf { it != "__none__" }

    if (magicAbility == null) {
        val noMagic = document.createElement("p") as HTMLParagraphElement
        noMagic.textContent = t("playing.noMagic")
        noMagic.style.color = "#999"; noMagic.style.fontSize = "13px"
        magicsBox.appendChild(noMagic)
    } else {
        val abilityMod = when (magicAbility) {
            "Cha" -> stats.chaValue?.let { calcModifier(it) } ?: 0
            "Int" -> stats.intValue?.let { calcModifier(it) } ?: 0
            "Wis" -> stats.wisValue?.let { calcModifier(it) } ?: 0
            else -> 0
        }
        val spellMod = abilityMod + calcProficiency((mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0))
        val spellDC = spellMod + 8
        val abilityName = when (magicAbility) { "Cha" -> "Cha"; "Int" -> "Int"; "Wis" -> "Wis"; else -> magicAbility }

        // Stats row
        val magicStats = document.createElement("div") as HTMLDivElement
        magicStats.style.display = "flex"
        magicStats.style.setProperty("gap", "16px")
        magicStats.style.marginBottom = "8px"
        magicStats.style.fontSize = "12px"

        val abSpan = document.createElement("span") as HTMLSpanElement
        abSpan.textContent = "\u2728 ${tStat(abilityName)}"; abSpan.style.fontWeight = "bold"
        magicStats.appendChild(abSpan)
        val modSpan = document.createElement("span") as HTMLSpanElement
        val modStr = if (spellMod >= 0) "+$spellMod" else "$spellMod"
        modSpan.textContent = "${t("playing.mod")}: $modStr"
        magicStats.appendChild(modSpan)
        val dcSpan = document.createElement("span") as HTMLSpanElement
        dcSpan.textContent = "${t("playing.dc")}: $spellDC"
        magicStats.appendChild(dcSpan)
        magicsBox.appendChild(magicStats)

        // Spell slots
        val mainLevel = mainInfo?.mainClassLevel ?: 1
        val slots = DungeonsAndDragons.spellSlotsFor(magicMainClass, magicMainSub, mainLevel)

        if (slots.isNotEmpty()) {
            val slotsDiv = document.createElement("div") as HTMLDivElement
            slotsDiv.style.marginBottom = "8px"

            slots.forEachIndexed { idx, baseSlots ->
                val circleNum = idx + 1
                val usedKey = "dnd_spell_slots_${character.id}_${magicMainClass}_$circleNum"
                val tempKey = "dnd_spell_slots_temp_${character.id}_${magicMainClass}_$circleNum"
                val usedSlots = localStorage.getItem(usedKey)?.toIntOrNull() ?: 0
                val tempSlots = localStorage.getItem(tempKey)?.toIntOrNull() ?: 0
                val availableSlots = baseSlots + tempSlots

                val slotRow = document.createElement("div") as HTMLDivElement
                slotRow.style.display = "flex"
                slotRow.style.alignItems = "center"
                slotRow.style.setProperty("gap", "4px")
                slotRow.style.marginBottom = "3px"
                slotRow.style.fontSize = "11px"

                val label = document.createElement("span") as HTMLSpanElement
                label.textContent = "${circleNum}\u00BA"
                label.style.fontWeight = "bold"
                label.style.width = "22px"
                slotRow.appendChild(label)

                val badge = document.createElement("span") as HTMLSpanElement
                val tempStr = if (tempSlots > 0) " (+$tempSlots)" else ""
                badge.textContent = "$usedSlots / $availableSlots$tempStr"
                badge.style.padding = "1px 4px"
                badge.style.borderRadius = "3px"
                badge.style.backgroundColor = if (usedSlots >= availableSlots) "#ffe0e0" else "#e0f0e0"
                badge.style.minWidth = "50px"
                badge.style.textAlign = "center"
                slotRow.appendChild(badge)

                // Increase Used
                val useBtn = document.createElement("button") as HTMLButtonElement
                useBtn.textContent = "\u25B2"; useBtn.title = "Use Slot"; useBtn.style.fontSize = "9px"
                useBtn.disabled = usedSlots >= availableSlots
                useBtn.addEventListener("click", {
                    localStorage.setItem(usedKey, (usedSlots + 1).toString())
                    container.innerHTML = ""; renderDndPlayingTab(character, container)
                })
                slotRow.appendChild(useBtn)

                // Decrease Used (or remove temp if used is 0)
                val restoreBtn = document.createElement("button") as HTMLButtonElement
                restoreBtn.textContent = "\u25BC"; restoreBtn.title = "Restore Slot"; restoreBtn.style.fontSize = "9px"
                restoreBtn.disabled = usedSlots <= 0 && tempSlots <= 0
                restoreBtn.addEventListener("click", {
                    if (usedSlots > 0) {
                        localStorage.setItem(usedKey, (usedSlots - 1).toString())
                    } else if (tempSlots > 0) {
                        localStorage.setItem(tempKey, (tempSlots - 1).toString())
                    }
                    container.innerHTML = ""; renderDndPlayingTab(character, container)
                })
                slotRow.appendChild(restoreBtn)

                // Add Temporary Slot
                val tempBtn = document.createElement("button") as HTMLButtonElement
                tempBtn.textContent = "\u2795"; tempBtn.title = "Add Temp Slot"; tempBtn.style.fontSize = "9px"
                tempBtn.addEventListener("click", {
                    localStorage.setItem(tempKey, (tempSlots + 1).toString())
                    container.innerHTML = ""; renderDndPlayingTab(character, container)
                })
                slotRow.appendChild(tempBtn)

                slotsDiv.appendChild(slotRow)
            }
            magicsBox.appendChild(slotsDiv)
        }

        // Prepared Spells button
        val magicBtns = document.createElement("div") as HTMLDivElement
        magicBtns.style.display = "flex"
        magicBtns.style.setProperty("gap", "4px")

        // Change Prepared Spells
        val prepBtn = document.createElement("button") as HTMLButtonElement
        prepBtn.textContent = "\uD83D\uDCCB " + t("magic.prepared")
        prepBtn.title = "Change Prepared Spells"
        prepBtn.style.fontSize = "11px"
        val classNeedsPrepared = (magicMainClass ?: "") in DungeonsAndDragons.preparedCasters
        prepBtn.disabled = !classNeedsPrepared
        prepBtn.addEventListener("click", {
            DndPreparedSpellsModal(character).show {
                container.innerHTML = ""
                renderDndPlayingTab(character, container)
            }
        })
        magicBtns.appendChild(prepBtn)

        // Reset all slots
        val resetSlotsBtn = document.createElement("button") as HTMLButtonElement
        resetSlotsBtn.textContent = "\u21BA"
        resetSlotsBtn.title = t("playing.resetSlots")
        resetSlotsBtn.style.fontSize = "11px"
        resetSlotsBtn.addEventListener("click", {
            slots.forEachIndexed { idx, _ ->
                val circleNum = idx + 1
                localStorage.removeItem("dnd_spell_slots_${character.id}_${magicMainClass}_$circleNum")
                localStorage.removeItem("dnd_spell_slots_temp_${character.id}_${magicMainClass}_$circleNum")
            }
            container.innerHTML = ""; renderDndPlayingTab(character, container)
        })
        magicBtns.appendChild(resetSlotsBtn)

        magicsBox.appendChild(magicBtns)
    }

    bottomRow.appendChild(magicsBox)

    // Loot and Notes box
    val lootBox = document.createElement("div") as HTMLDivElement
    lootBox.style.border = "1px solid #ccc"
    lootBox.style.borderRadius = "8px"
    lootBox.style.padding = "12px"
    lootBox.style.setProperty("flex", "1")

    val lootTitle = document.createElement("h4") as HTMLHeadingElement
    lootTitle.textContent = t("playing.lootNotes")
    lootTitle.style.margin = "0 0 10px 0"
    lootBox.appendChild(lootTitle)

    // Money display
    val money = Repos.money.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)
    val moneyGrid = document.createElement("div") as HTMLDivElement
    moneyGrid.style.setProperty("display", "grid")
    moneyGrid.style.setProperty("grid-template-columns", "repeat(5, 1fr)")
    moneyGrid.style.setProperty("gap", "8px")
    moneyGrid.style.textAlign = "center"
    moneyGrid.style.marginBottom = "12px"

    data class CoinDisplay(val label: String, val abbr: String, val value: Int)
    val coins = listOf(
        CoinDisplay(t("coin.copper"), t("coin.copper.abbr"), money.copper),
        CoinDisplay(t("coin.silver"), t("coin.silver.abbr"), money.silver),
        CoinDisplay(t("coin.electrum"), t("coin.electrum.abbr"), money.electrum),
        CoinDisplay(t("coin.gold"), t("coin.gold.abbr"), money.gold),
        CoinDisplay(t("coin.platinum"), t("coin.platinum.abbr"), money.platinum)
    )

    coins.forEach { coin ->
        val col = document.createElement("div") as HTMLDivElement
        val numSpan = document.createElement("div") as HTMLDivElement
        numSpan.textContent = coin.value.toString()
        numSpan.style.fontSize = "18px"
        numSpan.style.fontWeight = "bold"
        col.appendChild(numSpan)
        val abbrSpan = document.createElement("div") as HTMLDivElement
        abbrSpan.textContent = coin.abbr
        abbrSpan.style.fontSize = "10px"
        abbrSpan.style.color = "#666"
        col.appendChild(abbrSpan)
        moneyGrid.appendChild(col)
    }
    lootBox.appendChild(moneyGrid)

    // Add / Subtract money buttons
    val moneyBtns = document.createElement("div") as HTMLDivElement
    moneyBtns.style.display = "flex"
    moneyBtns.style.setProperty("gap", "8px")
    moneyBtns.style.justifyContent = "center"

    val addMoneyBtn = document.createElement("button") as HTMLButtonElement
    addMoneyBtn.textContent = "\uD83D\uDCB0 " + t("btn.add")
    addMoneyBtn.style.fontSize = "11px"
    addMoneyBtn.addEventListener("click", {
        DndMoneyModal(character, t("playing.addMoney"), true).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    moneyBtns.appendChild(addMoneyBtn)

    val subMoneyBtn = document.createElement("button") as HTMLButtonElement
    subMoneyBtn.textContent = "\uD83D\uDCB8 " + t("playing.spend")
    subMoneyBtn.style.fontSize = "11px"
    subMoneyBtn.addEventListener("click", {
        DndMoneyModal(character, t("playing.spendMoney"), false).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    moneyBtns.appendChild(subMoneyBtn)

    lootBox.appendChild(moneyBtns)

    // Loot buttons
    val lootBtnSection = document.createElement("div") as HTMLDivElement
    lootBtnSection.style.borderTop = "1px solid #eee"
    lootBtnSection.style.marginTop = "10px"
    lootBtnSection.style.paddingTop = "8px"

    val lootBtnTitle = document.createElement("div") as HTMLDivElement
    lootBtnTitle.textContent = t("playing.addLoot")
    lootBtnTitle.style.fontWeight = "bold"
    lootBtnTitle.style.fontSize = "12px"
    lootBtnTitle.style.marginBottom = "6px"
    lootBtnSection.appendChild(lootBtnTitle)

    val lootBtnRow = document.createElement("div") as HTMLDivElement
    lootBtnRow.style.display = "flex"
    lootBtnRow.style.setProperty("flex-wrap", "wrap")
    lootBtnRow.style.setProperty("gap", "4px")

    fun addLootBtn(label: String, onClick: () -> Unit) {
        val btn = document.createElement("button") as HTMLButtonElement
        btn.textContent = label
        btn.style.fontSize = "10px"
        btn.addEventListener("click", { onClick() })
        lootBtnRow.appendChild(btn)
    }

    addLootBtn("\uD83E\uDE96 " + t("inventory.armor")) {
        DndArmorModal(character, null).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    addLootBtn("\u2694\uFE0F " + t("inventory.weapons")) {
        DndWeaponModal(character, null).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    addLootBtn("\u2728 " + t("inventory.magicItems")) {
        DndMagicItemModal(character, null).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    addLootBtn("\uD83E\uDDEA " + t("inv.addConsumable")) {
        DndAddConsumableLootModal(character, onItemAdded = {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }).show()
    }

    addLootBtn("\uD83D\uDCE6 " + t("inventory.other")) {
        DndInventoryItemModal(character, "Key Items, Loot and others", null).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    }

    lootBtnSection.appendChild(lootBtnRow)

    // Add Note button
    val addNoteBtn = document.createElement("button") as HTMLButtonElement
    addNoteBtn.textContent = "\uD83D\uDCDD " + t("playing.addNote")
    addNoteBtn.style.fontSize = "10px"
    addNoteBtn.style.marginTop = "8px"
    addNoteBtn.addEventListener("click", {
        DndNoteModal(character, null).show {
            container.innerHTML = ""
            renderDndPlayingTab(character, container)
        }
    })
    lootBtnSection.appendChild(addNoteBtn)

    lootBox.appendChild(lootBtnSection)
    bottomRow.appendChild(lootBox)
    leftPanel.appendChild(bottomRow)

    // === RIGHT PANEL (Search) ===
    DndSearch(character, rightPanel)

    layout.appendChild(leftPanel)
    layout.appendChild(rightPanel)
    container.appendChild(layout)
}

