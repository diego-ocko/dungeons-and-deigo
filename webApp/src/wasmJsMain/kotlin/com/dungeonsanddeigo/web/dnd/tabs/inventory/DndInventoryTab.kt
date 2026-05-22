package com.dungeonsanddeigo.web.dnd.tabs.inventory

import com.dungeonsanddeigo.dnd.rules.calcModifier
import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.*
import com.dungeonsanddeigo.web.Repos
import com.dungeonsanddeigo.web.dnd.modals.armor.showArmorModal
import com.dungeonsanddeigo.web.dnd.modals.weapon.showWeaponModal
import com.dungeonsanddeigo.web.dnd.modals.magicItem.showMagicItemModal
import com.dungeonsanddeigo.web.dnd.modals.consumable.showConsumableModal
import com.dungeonsanddeigo.web.dnd.modals.inventoryItem.showInventoryItemModal
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*

fun renderDndInventoryTab(character: Character, container: HTMLDivElement) {
    val sections = listOf("Armor", "Weapons", "Magic Items", "Money", "Potions Ammo and Ration", "Key Items, Loot and others")

    fun refresh() {
        container.innerHTML = ""
        val grid = document.createElement("div") as HTMLDivElement
        grid.style.setProperty("display", "grid")
        grid.style.setProperty("grid-template-columns", "1fr 1fr 1fr")
        grid.style.setProperty("gap", "16px")

        sections.forEach { section ->
            val box = document.createElement("div") as HTMLDivElement
            box.style.border = "1px solid #ccc"
            box.style.borderRadius = "8px"
            box.style.padding = "12px"
            box.style.minHeight = "150px"

            // Header with title and Add button
            val header = document.createElement("div") as HTMLDivElement
            header.style.display = "flex"
            header.style.justifyContent = "space-between"
            header.style.alignItems = "center"
            header.style.marginBottom = "8px"

            val title = document.createElement("h4") as HTMLHeadingElement
            title.textContent = tSection(section)
            title.style.margin = "0"
            header.appendChild(title)

            if (section != "Money") {
                val addBtn = document.createElement("button") as HTMLButtonElement
                addBtn.textContent = t("inv.add")
                addBtn.style.fontSize = "12px"
                addBtn.addEventListener("click", {
                    when (section) {
                        "Armor" -> showArmorModal(character, null) { refresh() }
                        "Weapons" -> showWeaponModal(character, null) { refresh() }
                        "Magic Items" -> showMagicItemModal(character, null) { refresh() }
                        "Potions Ammo and Ration" -> showConsumableModal(character, null) { refresh() }
                        else -> showInventoryItemModal(character, section, null) { refresh() }
                    }
                })
                header.appendChild(addBtn)
            }

            box.appendChild(header)

            if (section == "Money") {
                // Money section: editable fields with auto-save
                val money = Repos.money.getByCharacterId(character.id) ?: DndMoney(characterId = character.id)

                val coinsGrid = document.createElement("div") as HTMLDivElement
                coinsGrid.style.setProperty("display", "grid")
                coinsGrid.style.setProperty("grid-template-columns", "1fr 1fr")
                coinsGrid.style.setProperty("gap", "6px")
                coinsGrid.style.fontSize = "13px"

                fun addCoinField(label: String, abbr: String, value: Int): HTMLInputElement {
                    val lbl = document.createElement("label") as HTMLLabelElement
                    lbl.textContent = label
                    lbl.style.fontWeight = "bold"
                    coinsGrid.appendChild(lbl)
                    val wrapper = document.createElement("div") as HTMLDivElement
                    wrapper.style.display = "flex"
                    wrapper.style.alignItems = "center"
                    wrapper.style.setProperty("gap", "4px")
                    val input = document.createElement("input") as HTMLInputElement
                    input.type = "number"; input.min = "0"
                    input.value = value.toString()
                    input.style.width = "100%"; input.style.padding = "2px"
                    wrapper.appendChild(input)
                    val abbrSpan = document.createElement("span") as HTMLSpanElement
                    abbrSpan.textContent = abbr
                    abbrSpan.style.fontSize = "12px"
                    abbrSpan.style.color = "#666"
                    wrapper.appendChild(abbrSpan)
                    coinsGrid.appendChild(wrapper)
                    return input
                }

                val cpInput = addCoinField(t("coin.copper"), t("coin.copper.abbr"), money.copper)
                val spInput = addCoinField(t("coin.silver"), t("coin.silver.abbr"), money.silver)
                val epInput = addCoinField(t("coin.electrum"), t("coin.electrum.abbr"), money.electrum)
                val gpInput = addCoinField(t("coin.gold"), t("coin.gold.abbr"), money.gold)
                val ppInput = addCoinField(t("coin.platinum"), t("coin.platinum.abbr"), money.platinum)

                // Lifestyle
                val lifeLbl = document.createElement("label") as HTMLLabelElement
                lifeLbl.textContent = t("inv.lifestyle"); lifeLbl.style.fontWeight = "bold"
                coinsGrid.appendChild(lifeLbl)
                val lifeSelect = document.createElement("select") as HTMLSelectElement
                lifeSelect.style.width = "100%"
                val emptyOpt = document.createElement("option") as HTMLOptionElement
                emptyOpt.value = ""; emptyOpt.textContent = "--"
                lifeSelect.appendChild(emptyOpt)
                DndMoney.lifestyles.forEach { ls ->
                    val opt = document.createElement("option") as HTMLOptionElement
                    opt.value = ls; opt.textContent = tLifestyle(ls)
                    lifeSelect.appendChild(opt)
                }
                lifeSelect.value = money.lifestyle
                coinsGrid.appendChild(lifeSelect)

                // Cost per day label
                val costLabel = document.createElement("span") as HTMLSpanElement
                costLabel.style.setProperty("grid-column", "1 / -1")
                costLabel.style.fontSize = "12px"
                costLabel.style.color = "#555"
                costLabel.style.marginTop = "4px"

                fun lifestyleCost(ls: String): String = when (ls) {
                    "Wretched" -> t("inv.noCost")
                    "Squalid" -> "1 ${t("coin.silver.abbr")} / ${t("inv.perDay")}"
                    "Poor" -> "2 ${t("coin.silver.abbr")} / ${t("inv.perDay")}"
                    "Modest" -> "1 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    "Comfortable" -> "2 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    "Wealthy" -> "4 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    "Aristocratic" -> "10 ${t("coin.gold.abbr")} / ${t("inv.perDay")}"
                    else -> ""
                }

                fun updateCostLabel() {
                    val cost = lifestyleCost(lifeSelect.value)
                    costLabel.textContent = if (cost.isNotEmpty()) "${t("inv.costPerDay")}: $cost" else ""
                }
                updateCostLabel()
                lifeSelect.addEventListener("change", { updateCostLabel() })
                coinsGrid.appendChild(costLabel)

                box.appendChild(coinsGrid)

                // Auto-save for money
                val moneyStatus = document.createElement("span") as HTMLSpanElement
                moneyStatus.style.fontSize = "11px"
                moneyStatus.style.display = "block"
                moneyStatus.style.marginTop = "6px"
                box.appendChild(moneyStatus)

                var moneySaveTimeout = 0
                fun autoSaveMoney() {
                    moneyStatus.textContent = "Saving..."
                    moneyStatus.style.color = "gray"
                    if (moneySaveTimeout != 0) window.clearTimeout(moneySaveTimeout)
                    moneySaveTimeout = window.setTimeout({
                        Repos.money.save(DndMoney(
                            characterId = character.id,
                            copper = cpInput.value.toIntOrNull() ?: 0,
                            silver = spInput.value.toIntOrNull() ?: 0,
                            electrum = epInput.value.toIntOrNull() ?: 0,
                            gold = gpInput.value.toIntOrNull() ?: 0,
                            platinum = ppInput.value.toIntOrNull() ?: 0,
                            lifestyle = lifeSelect.value
                        ))
                        moneyStatus.textContent = "\u2713 Saved"
                        moneyStatus.style.color = "green"
                        null
                    }, 500)
                }
                coinsGrid.addEventListener("input", { autoSaveMoney() })
                coinsGrid.addEventListener("change", { autoSaveMoney() })
            } else if (section == "Armor") {
                // Armor section
                val armorTypeOrder = mapOf("Light Armor" to 0, "Medium Armor" to 1, "Heavy Armor" to 2, "Shield" to 3, "Clothes" to 4)
                val armors = Repos.armor.getByCharacterId(character.id)
                    .sortedWith(compareByDescending<DndArmor> { it.isEquipped }
                        .thenBy { armorTypeOrder[it.type] ?: 5 }
                        .thenBy { it.name })

                // Calculate AC
                val stats = Repos.baseStats.getByCharacterId(character.id)
                val dexMod = stats?.dexValue?.let { calcModifier(it) } ?: 0
                val noArmorAC = stats?.emptyArmorClass ?: 10
                val equippedArmor = armors.firstOrNull { it.isEquipped && it.type != "Shield" && it.type != "Clothes" }
                val equippedShield = armors.firstOrNull { it.isEquipped && it.type == "Shield" }
                val shieldAC = equippedShield?.baseAC ?: 0

                val totalAC = if (equippedArmor == null) {
                    noArmorAC + shieldAC
                } else {
                    val armorBase = equippedArmor.baseAC
                    val modBonus = when (equippedArmor.acModifier) {
                        "Dex" -> dexMod
                        "Dex (Max: 2)" -> minOf(dexMod, 2)
                        else -> 0
                    }
                    armorBase + modBonus + shieldAC
                }

                // Display AC
                // Check proficiencies and stats for warnings
                val features = Repos.features.getByCharacterId(character.id)
                val weaponArmorFeatures = features.filter { it.type == "Weapon/Armor Proficiency" || it.type == t("stats.proficiency") }
                val armorProficiencies = mutableSetOf<String>()
                weaponArmorFeatures.forEach { f ->
                    f.description.split(",").map { it.trim() }.forEach { entry ->
                        if (entry.startsWith("armor:")) armorProficiencies.add(entry.removePrefix("armor:").trim())
                    }
                }

                // Check if equipped armor/shield lacks proficiency
                val equippedArmorProfKey = when (equippedArmor?.type) {
                    "Light Armor" -> "Light"
                    "Medium Armor" -> "Medium"
                    "Heavy Armor" -> "Heavy"
                    else -> null
                }
                val equippedShieldProfKey = if (equippedShield != null) "Shields" else null
                val lacksArmorProf = equippedArmor != null && equippedArmorProfKey != null && equippedArmorProfKey !in armorProficiencies
                val lacksShieldProf = equippedShield != null && equippedShieldProfKey != null && equippedShieldProfKey !in armorProficiencies
                val lacksAnyProf = lacksArmorProf || lacksShieldProf

                // Display AC with optional warning
                val acRow = document.createElement("div") as HTMLDivElement
                acRow.style.display = "flex"
                acRow.style.alignItems = "center"
                acRow.style.setProperty("gap", "12px")
                acRow.style.marginBottom = "12px"

                val acDisplay = document.createElement("div") as HTMLDivElement
                acDisplay.style.textAlign = "center"
                val acNumber = document.createElement("span") as HTMLSpanElement
                acNumber.textContent = totalAC.toString()
                acNumber.style.fontSize = "36px"
                acNumber.style.fontWeight = "bold"
                acDisplay.appendChild(acNumber)
                val acLabel = document.createElement("div") as HTMLDivElement
                acLabel.textContent = t("stats.ac")
                acLabel.style.fontSize = "12px"
                acLabel.style.color = "#666"
                acDisplay.appendChild(acLabel)
                acRow.appendChild(acDisplay)

                // Strength warning check
                val charStrength = stats?.strValue ?: 0
                val charSpeed = stats?.speed ?: 0
                val lacksStrength = equippedArmor != null && equippedArmor.minimumStrength > 0 && charStrength < equippedArmor.minimumStrength

                // Warnings column
                val warningsDiv = document.createElement("div") as HTMLDivElement
                warningsDiv.style.setProperty("flex", "1")

                if (lacksAnyProf) {
                    val warning = document.createElement("div") as HTMLDivElement
                    warning.textContent = "\u26A0\uFE0F " + t("inv.lackProf")
                    warning.style.color = "#c00"
                    warning.style.fontSize = "12px"
                    warning.style.marginBottom = "4px"
                    warningsDiv.appendChild(warning)
                }

                if (equippedArmor?.hasSneakDisadvantage == true) {
                    val sneakWarn = document.createElement("div") as HTMLDivElement
                    sneakWarn.textContent = "\u26A0\uFE0F " + t("inv.sneakDisadvWarn")
                    sneakWarn.style.color = "#c00"
                    sneakWarn.style.fontSize = "12px"
                    sneakWarn.style.marginBottom = "4px"
                    warningsDiv.appendChild(sneakWarn)
                }

                if (lacksStrength) {
                    val strWarn = document.createElement("div") as HTMLDivElement
                    strWarn.textContent = "\u26A0\uFE0F " + t("inv.needStrength")
                    strWarn.style.color = "#c00"
                    strWarn.style.fontSize = "12px"
                    strWarn.style.marginBottom = "4px"
                    warningsDiv.appendChild(strWarn)
                }

                acRow.appendChild(warningsDiv)

                // Speed display
                val speedDisplay = document.createElement("div") as HTMLDivElement
                speedDisplay.style.textAlign = "center"
                val effectiveSpeed = if (lacksStrength) charSpeed - 3 else charSpeed
                val speedNumber = document.createElement("span") as HTMLSpanElement
                speedNumber.textContent = "${effectiveSpeed}m"
                speedNumber.style.fontSize = "36px"
                speedNumber.style.fontWeight = "bold"
                if (lacksStrength) speedNumber.style.color = "#c00"
                speedDisplay.appendChild(speedNumber)
                val speedLabel = document.createElement("div") as HTMLDivElement
                speedLabel.textContent = t("stats.speed")
                speedLabel.style.fontSize = "12px"
                speedLabel.style.color = "#666"
                speedDisplay.appendChild(speedLabel)
                acRow.appendChild(speedDisplay)

                box.appendChild(acRow)

                if (armors.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noArmor")
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    armors.forEach { armor ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val eqIcon = if (armor.isEquipped) "\u2705 " else ""
                        nameSpan.textContent = "$eqIcon${armor.name}"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showArmorModal(character, armor) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { Repos.armor.delete(armor.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        // Check warnings
                        val profKey = when (armor.type) {
                            "Light Armor" -> "Light"
                            "Medium Armor" -> "Medium"
                            "Heavy Armor" -> "Heavy"
                            "Shield" -> "Shields"
                            else -> ""
                        }
                        val hasProficiency = armor.type == "Clothes" || profKey in armorProficiencies
                        val hasStrWarning = armor.minimumStrength > 0 && charStrength < armor.minimumStrength

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val warnings = StringBuilder()
                        if (!hasProficiency) warnings.append("\u26A0\uFE0F ")
                        if (hasStrWarning) warnings.append("\u26A0\uFE0F ")
                        val sneakStr = if (armor.hasSneakDisadvantage) " | " + t("inv.sneakDisadvShort") else ""
                        val strStr = if (armor.minimumStrength > 0) " | ${t("inv.minStr")}: ${armor.minimumStrength}" else ""
                        if (armor.type == "Clothes") {
                            val featText = if (armor.additionalFeatures.isNotEmpty()) {
                                val truncated = if (armor.additionalFeatures.length > 50) armor.additionalFeatures.take(50) + "..." else armor.additionalFeatures
                                " | $truncated"
                            } else ""
                            line2.textContent = "${tArmorType(armor.type)} | ${armor.weight}kg | ${armor.price} ${tCurrency(armor.priceCurrency)}$featText"
                        } else {
                            line2.textContent = "$warnings${tArmorType(armor.type)} | ${t("inv.baseAC")}: ${armor.baseAC} (${tAcModifier(armor.acModifier)})$strStr$sneakStr | ${armor.weight}kg | ${armor.price} ${tCurrency(armor.priceCurrency)}"
                        }
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Weapons") {
                val weapons = Repos.weapon.getByCharacterId(character.id)

                // Gather weapon proficiencies from features
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

                // Attacks section (equipped weapons)
                val equippedWeapon = weapons.firstOrNull { it.isEquipped }
                if (equippedWeapon != null) {
                    val stats = Repos.baseStats.getByCharacterId(character.id)
                    val mainInfo = Repos.mainInfo.getByCharacterId(character.id)
                    val totalLevel = (mainInfo?.mainClassLevel ?: 0) + (mainInfo?.secondaryClassLevel ?: 0)
                    val profBonus = calcProficiency(totalLevel)
                    val strMod = stats?.strValue?.let { calcModifier(it) } ?: 0
                    val dexMod = stats?.dexValue?.let { calcModifier(it) } ?: 0

                    val hasProfEq = equippedWeapon.category in weaponCatProfs
                            || equippedWeapon.weaponType in weaponSpecificProfs
                    val isRanged = equippedWeapon.range && !equippedWeapon.thrown

                    val atkTable = document.createElement("table") as HTMLTableElement
                    atkTable.style.width = "100%"
                    atkTable.style.fontSize = "12px"
                    atkTable.style.borderCollapse = "collapse"
                    atkTable.style.marginBottom = "10px"

                    // Header
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

                    fun addAttackRow(useDex: Boolean, twoHanded: Boolean = false, thrown: Boolean = false) {
                        val mod = if (useDex) dexMod else strMod
                        val atkMod = mod + (if (hasProfEq) profBonus else 0)

                        val tr = document.createElement("tr") as HTMLTableRowElement

                        // Name
                        val tdName = document.createElement("td") as HTMLTableCellElement
                        tdName.style.padding = "3px 4px"
                        tdName.style.fontWeight = "bold"
                        val statLabel = if (equippedWeapon.finesse) " (${if (useDex) tStat("Dex") else tStat("Str")})" else ""
                        val handLabel = if (twoHanded) " [${t("inv.twoHandedShort")}]" else ""
                        val thrownLabel = if (thrown) " [${t("inv.thrownShort")}]" else ""
                        tdName.textContent = "${equippedWeapon.name}$statLabel$handLabel$thrownLabel"
                        tr.appendChild(tdName)

                        // Range
                        val tdRange = document.createElement("td") as HTMLTableCellElement
                        tdRange.style.padding = "3px 4px"
                        tdRange.textContent = if (isRanged || thrown) "${equippedWeapon.rangeDistance}/${equippedWeapon.rangeLongDistance}m" else t("inv.melee")
                        tr.appendChild(tdRange)

                        // Test
                        val tdTest = document.createElement("td") as HTMLTableCellElement
                        tdTest.style.padding = "3px 4px"
                        val testSign = if (atkMod >= 0) "+" else ""
                        tdTest.textContent = "$testSign$atkMod"
                        tr.appendChild(tdTest)

                        // Damage
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

                        // Notes (icons + additional features)
                        val tdNotes = document.createElement("td") as HTMLTableCellElement
                        tdNotes.style.padding = "3px 4px"
                        tdNotes.style.color = "#888"
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
                        addAttackRow(useDex = true)
                    } else if (equippedWeapon.finesse) {
                        addAttackRow(useDex = false)
                        if (equippedWeapon.versatile) addAttackRow(useDex = false, twoHanded = true)
                        addAttackRow(useDex = true)
                        if (equippedWeapon.versatile) addAttackRow(useDex = true, twoHanded = true)
                        if (equippedWeapon.thrown) {
                            addAttackRow(useDex = false, thrown = true)
                            addAttackRow(useDex = true, thrown = true)
                        }
                    } else {
                        addAttackRow(useDex = false)
                        if (equippedWeapon.versatile) addAttackRow(useDex = false, twoHanded = true)
                        if (equippedWeapon.thrown) addAttackRow(useDex = false, thrown = true)
                    }

                    atkTable.appendChild(tbody)
                    box.appendChild(atkTable)
                }

                if (weapons.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noWeapons")
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    weapons.sortedByDescending { it.isEquipped }.forEach { weapon ->
                        val hasProficiency = weapon.category in weaponCatProfs
                                || weapon.weaponType in weaponSpecificProfs

                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val eqIcon = if (weapon.isEquipped) "\u2705 " else ""
                        val warnIcon = if (!hasProficiency) "\u26A0\uFE0F " else ""
                        nameSpan.textContent = "$warnIcon$eqIcon${weapon.name}"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showWeaponModal(character, weapon) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { Repos.weapon.delete(weapon.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val props = mutableListOf<String>()
                        props.add(when(weapon.category) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); else -> weapon.category })
                        props.add("${weapon.damageDice} ${when(weapon.damageType) { "Bludgeoning" -> t("inv.dmg.bludgeoning"); "Piercing" -> t("inv.dmg.piercing"); "Slashing" -> t("inv.dmg.slashing"); else -> weapon.damageType }}")
                        if (weapon.versatile) props.add("${t("inv.versatile")} (${weapon.versatileDice})")
                        if (weapon.range) props.add("${t("inv.range")} ${weapon.rangeDistance}/${weapon.rangeLongDistance}m")
                        props.add("${weapon.weight}kg")
                        props.add("${weapon.price} ${tCurrency(weapon.priceCurrency)}")
                        // Property icons
                        val icons = mutableListOf<String>()
                        if (weapon.silver) icons.add("\uD83E\uDD48") // silver medal
                        if (weapon.heavy) icons.add("\u2693") // anchor = heavy
                        if (weapon.light) icons.add("\uD83E\uDEB6") // feather = light
                        if (weapon.reach) icons.add("\uD83D\uDCAB") // reach
                        if (weapon.ammunition) icons.add("\uD83C\uDFF9") // bow = ammo
                        if (weapon.loading) icons.add("\u23F3") // hourglass = loading
                        if (icons.isNotEmpty()) props.add(icons.joinToString(""))
                        // Features text
                        val featuresText = listOf(weapon.specialDescription, weapon.additionalFeatures)
                            .filter { it.isNotEmpty() }.joinToString(" | ")
                        if (featuresText.isNotEmpty()) {
                            val truncated = if (featuresText.length > 50) featuresText.take(50) + "..." else featuresText
                            props.add(truncated)
                        }
                        line2.textContent = props.joinToString(" | ")
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Magic Items") {
                val magicItems = Repos.magicItem.getByCharacterId(character.id)
                    .sortedWith(compareBy<DndMagicItem> {
                        when {
                            it.needSynch && it.isSynched -> 0
                            !it.needSynch -> 1
                            else -> 2
                        }
                    }.thenBy { it.name })

                // Synch counter
                val synchedCount = magicItems.count { it.needSynch && it.isSynched }
                val synchInfo = document.createElement("div") as HTMLDivElement
                synchInfo.style.fontSize = "12px"
                synchInfo.style.marginBottom = "8px"
                synchInfo.style.color = if (synchedCount > 3) "#c00" else "#555"
                synchInfo.textContent = "${t("inv.synchedItems")}: $synchedCount / ${t("inv.max")}: 3"
                box.appendChild(synchInfo)

                if (magicItems.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noMagicItems")
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    magicItems.forEach { item ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val synchIcon = if (!item.needSynch) "\uD83D\uDD35 "
                            else if (item.isSynched) "\u2705 " else ""
                        nameSpan.textContent = "$synchIcon${item.name}"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showMagicItemModal(character, item) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { Repos.magicItem.delete(item.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val effectStr = if (item.effect.isNotEmpty()) {
                            val truncated = if (item.effect.length > 80) item.effect.take(80) + "..." else item.effect
                            "$truncated | "
                        } else ""
                        line2.textContent = "$effectStr${item.weight}kg | ${item.price} ${tCurrency(item.priceCurrency)}"
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else if (section == "Potions Ammo and Ration") {
                val consumables = Repos.consumable.getByCharacterId(character.id)
                if (consumables.isEmpty()) {
                    val placeholder = document.createElement("p") as HTMLParagraphElement
                    placeholder.textContent = t("inv.noItems")
                    placeholder.style.color = "#999"
                    placeholder.style.fontSize = "13px"
                    box.appendChild(placeholder)
                } else {
                    consumables.forEach { item ->
                        val row = document.createElement("div") as HTMLDivElement
                        row.style.borderBottom = "1px solid #eee"
                        row.style.padding = "6px 0"
                        row.style.fontSize = "12px"

                        val line1 = document.createElement("div") as HTMLDivElement
                        line1.style.display = "flex"
                        line1.style.justifyContent = "space-between"
                        line1.style.alignItems = "center"

                        val nameSpan = document.createElement("span") as HTMLSpanElement
                        val qtyStr = if (item.quantity > 1) " x${item.quantity}" else ""
                        nameSpan.textContent = "${item.name}$qtyStr"
                        nameSpan.style.fontWeight = "bold"
                        line1.appendChild(nameSpan)

                        val actions = document.createElement("div") as HTMLDivElement
                        actions.style.display = "flex"
                        actions.style.setProperty("gap", "4px")
                        val editBtn = document.createElement("button") as HTMLButtonElement
                        editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                        editBtn.addEventListener("click", { showConsumableModal(character, item) { refresh() } })
                        actions.appendChild(editBtn)
                        val delBtn = document.createElement("button") as HTMLButtonElement
                        delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                        delBtn.addEventListener("click", { Repos.consumable.delete(item.id); refresh() })
                        actions.appendChild(delBtn)
                        line1.appendChild(actions)
                        row.appendChild(line1)

                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val effectStr = if (item.effect.isNotEmpty()) {
                            val truncated = if (item.effect.length > 50) item.effect.take(50) + "..." else item.effect
                            " | $truncated"
                        } else ""
                        line2.textContent = "${when(item.type) { "Healing Potion" -> t("inv.consumable.healingPotion"); "Magic Potion" -> t("inv.consumable.magicPotion"); "Food" -> t("inv.consumable.food"); "Ammunition" -> t("inv.consumable.ammunition"); "Other" -> t("inv.consumable.other"); else -> item.type }}$effectStr | ${item.weight}kg | ${item.price} ${tCurrency(item.priceCurrency)}"
                        row.appendChild(line2)

                        box.appendChild(row)
                    }
                }
            } else {
            // Key Items, Loot and others
            val items = Repos.inventory.getByCategory(character.id, section).sortedBy { it.name }
            if (items.isEmpty()) {
                val placeholder = document.createElement("p") as HTMLParagraphElement
                placeholder.textContent = t("inv.noItems")
                placeholder.style.color = "#999"
                placeholder.style.fontSize = "13px"
                box.appendChild(placeholder)
            } else {
                items.forEach { item ->
                    val row = document.createElement("div") as HTMLDivElement
                    row.style.borderBottom = "1px solid #eee"
                    row.style.padding = "6px 0"
                    row.style.fontSize = "12px"

                    val line1 = document.createElement("div") as HTMLDivElement
                    line1.style.display = "flex"
                    line1.style.justifyContent = "space-between"
                    line1.style.alignItems = "center"

                    val nameSpan = document.createElement("span") as HTMLSpanElement
                    nameSpan.textContent = item.name
                    nameSpan.style.fontWeight = "bold"
                    line1.appendChild(nameSpan)

                    val actions = document.createElement("div") as HTMLDivElement
                    actions.style.display = "flex"
                    actions.style.setProperty("gap", "4px")
                    val editBtn = document.createElement("button") as HTMLButtonElement
                    editBtn.textContent = "\u270E"; editBtn.style.fontSize = "11px"
                    editBtn.addEventListener("click", {
                        showInventoryItemModal(character, section, item) { refresh() }
                    })
                    actions.appendChild(editBtn)
                    val delBtn = document.createElement("button") as HTMLButtonElement
                    delBtn.textContent = "\u2716"; delBtn.style.fontSize = "11px"; delBtn.style.color = "red"
                    delBtn.addEventListener("click", { Repos.inventory.delete(item.id); refresh() })
                    actions.appendChild(delBtn)
                    line1.appendChild(actions)
                    row.appendChild(line1)

                    if (item.description.isNotEmpty()) {
                        val line2 = document.createElement("div") as HTMLDivElement
                        line2.style.color = "#666"
                        val truncated = if (item.description.length > 80) item.description.take(80) + "..." else item.description
                        line2.textContent = truncated
                        row.appendChild(line2)
                    }

                    box.appendChild(row)
                }
            }
            } // end else (non-Money)

            grid.appendChild(box)
        }

        container.appendChild(grid)
    }

    refresh()
}

