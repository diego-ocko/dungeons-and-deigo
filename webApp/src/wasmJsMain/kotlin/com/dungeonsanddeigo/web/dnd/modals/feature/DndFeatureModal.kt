package com.dungeonsanddeigo.web.dnd.modals.feature

import com.dungeonsanddeigo.dnd.rules.calcProficiency
import com.dungeonsanddeigo.i18n.*
import com.dungeonsanddeigo.model.Character
import com.dungeonsanddeigo.model.DndFeature
import com.dungeonsanddeigo.model.DndMainInfo
import com.dungeonsanddeigo.model.DungeonsAndDragons
import com.dungeonsanddeigo.web.Repos
import kotlinx.browser.document
import org.w3c.dom.*

fun showFeatureModal(
    character: Character,
    existing: DndFeature?,
    mainInfo: DndMainInfo?,
    onSave: () -> Unit
) {
    // Overlay
    val overlay = document.createElement("div") as HTMLDivElement
    overlay.className = "modal-overlay"

    val modal = document.createElement("div") as HTMLDivElement
    modal.className = "modal"

    val titleEl = document.createElement("h2") as HTMLHeadingElement
    titleEl.textContent = if (existing != null) t("features.editFeature") else t("features.addNew")
    modal.appendChild(titleEl)

    val form = document.createElement("div") as HTMLDivElement
    form.className = "modal__form"

    fun addLabel(text: String) {
        val lbl = document.createElement("label") as HTMLLabelElement
        lbl.textContent = text
        form.appendChild(lbl)
    }

    // Type (first field)
    addLabel(t("features.type"))
    val typeSelect = document.createElement("select") as HTMLSelectElement
    DndFeature.types.forEach { tp ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = tp; opt.textContent = tFeatureType(tp); typeSelect.appendChild(opt)
    }
    typeSelect.value = existing?.type ?: "Feature"
    if (existing != null) typeSelect.disabled = true
    form.appendChild(typeSelect)

    // Name input (will be placed in dynamic content area)
    val nameInput = document.createElement("input") as HTMLInputElement
    nameInput.value = existing?.name ?: ""
    nameInput.placeholder = "Feature name"

    // Source
    addLabel(t("features.source"))
    val sourceSelect = document.createElement("select") as HTMLSelectElement
    DndFeature.sources.forEach { s ->
        val opt = document.createElement("option") as HTMLOptionElement
        opt.value = s; opt.textContent = tSource(s); sourceSelect.appendChild(opt)
    }
    sourceSelect.value = existing?.source ?: "Class"
    form.appendChild(sourceSelect)

    // Source Extended container
    val sourceExtDiv = document.createElement("div") as HTMLDivElement
    sourceExtDiv.className = "modal__full-width"

    // Source extended fields (will be rebuilt on source change)
    var sourceClassSelect: HTMLSelectElement? = null
    var sourceClassLevelInput: HTMLInputElement? = null
    var sourceOriginInput: HTMLInputElement? = null
    var sourceRaceInput: HTMLInputElement? = null
    var sourceSubRaceInput: HTMLInputElement? = null
    var sourceCustomInput: HTMLInputElement? = null

    fun buildSourceExtended() {
        sourceExtDiv.innerHTML = ""
        val inner = document.createElement("div") as HTMLDivElement

        when (sourceSelect.value) {
            "Class" -> {
                val lbl1 = document.createElement("label") as HTMLLabelElement
                lbl1.textContent = t("main.class"); inner.appendChild(lbl1)

                val mainClass = mainInfo?.mainClass
                val secClass = mainInfo?.secondaryClass
                val classes = listOfNotNull(mainClass, secClass).filter { it.isNotBlank() }

                if (classes.size <= 1) {
                    // Single class — show as label
                    val classVal = classes.firstOrNull() ?: ""
                    val classSpan = document.createElement("span") as HTMLSpanElement
                    classSpan.textContent = tDnd("class", classVal)
                    inner.appendChild(classSpan)
                    val inp = document.createElement("input") as HTMLInputElement
                    inp.type = "hidden"; inp.value = classVal
                    inner.appendChild(inp)
                    sourceClassSelect = null
                    // Hack: reuse sourceCustomInput to carry the value
                    val hiddenSel = document.createElement("select") as HTMLSelectElement
                    val o = document.createElement("option") as HTMLOptionElement
                    o.value = classVal; o.textContent = tDnd("class", classVal); hiddenSel.appendChild(o)
                    hiddenSel.value = classVal
                    hiddenSel.style.display = "none"
                    inner.appendChild(hiddenSel)
                    sourceClassSelect = hiddenSel
                } else {
                    // Multiple classes — show dropdown limited to main + secondary
                    val sel = document.createElement("select") as HTMLSelectElement
                    classes.forEach { c ->
                        val o = document.createElement("option") as HTMLOptionElement
                        o.value = c; o.textContent = tDnd("class", c); sel.appendChild(o)
                    }
                    sel.value = existing?.sourceClass ?: (mainClass ?: "")
                    inner.appendChild(sel)
                    sourceClassSelect = sel
                }

                val lbl2 = document.createElement("label") as HTMLLabelElement
                lbl2.textContent = t("main.level"); inner.appendChild(lbl2)
                val lvl = document.createElement("input") as HTMLInputElement
                lvl.type = "number"; lvl.min = "1"; lvl.max = "20"
                lvl.value = existing?.sourceClassLevel?.toString() ?: "1"
                inner.appendChild(lvl)
                sourceClassLevelInput = lvl
            }
            "Origin" -> {
                val lbl = document.createElement("label") as HTMLLabelElement
                val valSpan = document.createElement("span") as HTMLSpanElement
                val originVal = existing?.sourceOrigin ?: (mainInfo?.origin ?: "")
                valSpan.textContent = originVal
                inner.appendChild(valSpan)
                // Hidden input to carry the value
                val inp = document.createElement("input") as HTMLInputElement
                inp.type = "hidden"; inp.value = originVal
                inner.appendChild(inp)
                sourceOriginInput = inp
            }
            "Race" -> {
                val lbl1 = document.createElement("label") as HTMLLabelElement
                lbl1.textContent = t("main.race"); inner.appendChild(lbl1)
                val raceVal = existing?.sourceRace ?: (mainInfo?.race ?: "")
                val raceSpan = document.createElement("span") as HTMLSpanElement
                raceSpan.textContent = tDnd("race", raceVal)
                inner.appendChild(raceSpan)
                val inp1 = document.createElement("input") as HTMLInputElement
                inp1.type = "hidden"; inp1.value = raceVal
                inner.appendChild(inp1)
                sourceRaceInput = inp1

                val lbl2 = document.createElement("label") as HTMLLabelElement
                lbl2.textContent = t("main.subrace"); inner.appendChild(lbl2)
                val subRaceVal = existing?.sourceSubRace ?: (mainInfo?.subRace ?: "")
                val subRaceSpan = document.createElement("span") as HTMLSpanElement
                subRaceSpan.textContent = tDnd("subrace", subRaceVal)
                inner.appendChild(subRaceSpan)
                val inp2 = document.createElement("input") as HTMLInputElement
                inp2.type = "hidden"; inp2.value = subRaceVal
                inner.appendChild(inp2)
                sourceSubRaceInput = inp2
            }
            "Custom" -> {
                val lbl = document.createElement("label") as HTMLLabelElement
                val inp = document.createElement("input") as HTMLInputElement
                inp.value = existing?.sourceCustom ?: ""
                inner.appendChild(inp)
                sourceCustomInput = inp
            }
        }
        sourceExtDiv.appendChild(inner)
    }
    buildSourceExtended()
    sourceSelect.addEventListener("change", { buildSourceExtended() })
    form.appendChild(sourceExtDiv)

    // Rechargable fields container
    val rechargeDiv = document.createElement("div") as HTMLDivElement
    rechargeDiv.className = "modal__full-width"

    var maxQtyInput: HTMLInputElement? = null
    var reloadSelect: HTMLSelectElement? = null

    fun buildRechargeFields() {
        rechargeDiv.innerHTML = ""
        if (typeSelect.value == "Rechargable Feature") {
            val inner = document.createElement("div") as HTMLDivElement

            val lbl1 = document.createElement("label") as HTMLLabelElement
            lbl1.textContent = t("features.maxQuantity"); inner.appendChild(lbl1)
            val qty = document.createElement("input") as HTMLInputElement
            qty.type = "number"; qty.value = existing?.maxQuantity?.toString() ?: "1"
            inner.appendChild(qty)
            maxQtyInput = qty

            val lbl2 = document.createElement("label") as HTMLLabelElement
            lbl2.textContent = t("features.reloadRule"); inner.appendChild(lbl2)
            val sel = document.createElement("select") as HTMLSelectElement
            DndFeature.reloadRules.forEach { r ->
                val o = document.createElement("option") as HTMLOptionElement
                o.value = r; o.textContent = tReloadRule(r); sel.appendChild(o)
            }
            sel.value = existing?.reloadRule ?: "Long Rest"
            inner.appendChild(sel)
            reloadSelect = sel

            rechargeDiv.appendChild(inner)
        }
    }
    buildRechargeFields()
    typeSelect.addEventListener("change", { buildRechargeFields() })
    form.appendChild(rechargeDiv)

    // Dynamic content area (changes based on type)
    val dynamicDiv = document.createElement("div") as HTMLDivElement
    dynamicDiv.className = "modal__full-width"

    val defaultIdioms = listOf("Common", "Dwarvish", "Elvish", "Giant", "Gnomish", "Goblin", "Halfling", "Orc", "Draconic")
    val defaultTools = listOf("Alchemist's Supplies", "Brewer's Supplies", "Calligrapher's Supplies", "Carpenter's Tools", "Cartographer's Tools", "Cobbler's Tools", "Cook's Utensils", "Glassblower's Tools", "Jeweler's Tools", "Leatherworker's Tools", "Mason's Tools", "Painter's Supplies", "Potter's Tools", "Smith's Tools", "Tinker's Tools", "Weaver's Tools", "Woodcarver's Tools", "Disguise Kit", "Forgery Kit", "Herbalism Kit", "Navigator's Tools", "Poisoner's Kit", "Thieves' Tools")
    val defaultWeapons = listOf("Club", "Dagger", "Greatclub", "Handaxe", "Javelin", "Light Hammer", "Mace", "Quarterstaff", "Sickle", "Spear", "Crossbow (Light)", "Dart", "Shortbow", "Sling", "Battleaxe", "Flail", "Glaive", "Greataxe", "Greatsword", "Halberd", "Lance", "Longsword", "Maul", "Morningstar", "Pike", "Rapier", "Scimitar", "Shortsword", "Trident", "War Pick", "Warhammer", "Whip", "Blowgun", "Crossbow (Hand)", "Crossbow (Heavy)", "Longbow", "Net")
    val selectedItems = mutableListOf<String>()
    val isListType = existing?.type == "Idiom" || existing?.type == "Tool Proficiency"
    if (isListType && existing?.description?.isNotBlank() == true) {
        selectedItems.addAll(existing.description.split(",").map { it.trim() }.filter { it.isNotEmpty() })
    }

    // Weapon/Armor proficiency state
    val armorChecks = mutableMapOf("Light" to false, "Medium" to false, "Heavy" to false, "Shields" to false)
    val weaponCatChecks = mutableMapOf("Simple" to false, "Martial" to false)
    var weaponCatOther = ""
    val selectedWeapons = mutableListOf<String>()
    if (existing?.type == "Weapon/Armor Proficiency" && existing.description.isNotBlank()) {
        existing.description.split(",").map { it.trim() }.forEach { entry ->
            when {
                entry.startsWith("armor:") -> armorChecks[entry.removePrefix("armor:")] = true
                entry.startsWith("weapon_cat:") -> {
                    val cat = entry.removePrefix("weapon_cat:")
                    if (cat == "Simple" || cat == "Martial") weaponCatChecks[cat] = true
                    else weaponCatOther = cat
                }
                entry.startsWith("weapon:") -> selectedWeapons.add(entry.removePrefix("weapon:"))
            }
        }
    }

    var descInput: HTMLTextAreaElement? = null

    fun buildDynamicContent() {
        dynamicDiv.innerHTML = ""
        if (typeSelect.value == "Idiom" || typeSelect.value == "Tool Proficiency") {
            val isIdiomType = typeSelect.value == "Idiom"
            val itemLabel = if (isIdiomType) t("features.languages") else t("features.tools")
            val defaultList = if (isIdiomType) defaultIdioms else defaultTools
            val placeholder = if (isIdiomType) "Custom language" else "Custom tool"
            val addPlaceholder = if (isIdiomType) "-- Add language --" else "-- Add tool --"

            val label = document.createElement("label") as HTMLLabelElement
            label.textContent = itemLabel
            dynamicDiv.appendChild(label)

            val selectedDiv = document.createElement("div") as HTMLDivElement
            selectedDiv.className = "modal__tags"

            val translateBadge: (String) -> String = if (isIdiomType) ::tIdiom else ::tTool
            fun refreshTags() {
                selectedDiv.innerHTML = ""
                selectedItems.forEach { item ->
                    val badge = document.createElement("span") as HTMLSpanElement
                    badge.textContent = "${translateBadge(item)} \u00D7"
                    badge.className = "modal__tag"
                    badge.addEventListener("click", {
                        selectedItems.remove(item)
                        refreshTags()
                    })
                    selectedDiv.appendChild(badge)
                }
            }
            refreshTags()
            dynamicDiv.appendChild(selectedDiv)

            val addRow = document.createElement("div") as HTMLDivElement
            addRow.className = "modal__tag-add-row"

            val itemSelect = document.createElement("select") as HTMLSelectElement
            val emptyOpt = document.createElement("option") as HTMLOptionElement
            emptyOpt.value = ""; emptyOpt.textContent = addPlaceholder
            itemSelect.appendChild(emptyOpt)
            val translateItem = if (isIdiomType) ::tIdiom else ::tTool
            defaultList.forEach { item ->
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = item; opt.textContent = translateItem(item)
                itemSelect.appendChild(opt)
            }
            val customOpt = document.createElement("option") as HTMLOptionElement
            customOpt.value = "__custom__"; customOpt.textContent = "Custom..."
            itemSelect.appendChild(customOpt)
            addRow.appendChild(itemSelect)

            val customInput = document.createElement("input") as HTMLInputElement
            customInput.placeholder = placeholder
            customInput.style.display = "none"
            addRow.appendChild(customInput)

            val addBtn = document.createElement("button") as HTMLButtonElement
            addBtn.textContent = "Add"
            addRow.appendChild(addBtn)

            itemSelect.addEventListener("change", {
                customInput.style.display = if (itemSelect.value == "__custom__") "inline" else "none"
            })

            addBtn.addEventListener("click", {
                val item = if (itemSelect.value == "__custom__") {
                    customInput.value.trim().also { customInput.value = "" }
                } else {
                    itemSelect.value
                }
                if (item.isNotEmpty() && item !in selectedItems) {
                    selectedItems.add(item)
                    refreshTags()
                }
                itemSelect.value = ""
                customInput.style.display = "none"
            })

            dynamicDiv.appendChild(addRow)
        } else if (typeSelect.value == "Weapon/Armor Proficiency") {
            // Armor checkboxes
            val armorLabel = document.createElement("label") as HTMLLabelElement
            armorLabel.textContent = t("features.armor")
            dynamicDiv.appendChild(armorLabel)

            val armorRow = document.createElement("div") as HTMLDivElement
            armorRow.className = "modal__checkbox-row"

            val armorCbs = mutableMapOf<String, HTMLInputElement>()
            listOf("Light", "Medium", "Heavy", "Shields").forEach { armor ->
                val armorDisplay = when (armor) { "Light" -> t("features.armor.light"); "Medium" -> t("features.armor.medium"); "Heavy" -> t("features.armor.heavy"); "Shields" -> t("features.armor.shields"); else -> armor }
                val lbl = document.createElement("label") as HTMLLabelElement
                val cb = document.createElement("input") as HTMLInputElement
                cb.type = "checkbox"
                cb.checked = armorChecks[armor] == true
                lbl.appendChild(cb)
                lbl.append(armorDisplay)
                armorRow.appendChild(lbl)
                armorCbs[armor] = cb
            }
            dynamicDiv.appendChild(armorRow)

            // Weapon category checkboxes
            val weaponCatLabel = document.createElement("label") as HTMLLabelElement
            weaponCatLabel.textContent = t("features.weaponCategories")
            dynamicDiv.appendChild(weaponCatLabel)

            val weaponCatRow = document.createElement("div") as HTMLDivElement
            weaponCatRow.className = "modal__checkbox-row"

            val weaponCatCbs = mutableMapOf<String, HTMLInputElement>()
            listOf("Simple", "Martial").forEach { cat ->
                val catDisplay = when (cat) { "Simple" -> t("features.weapon.simple"); "Martial" -> t("features.weapon.martial"); else -> cat }
                val lbl = document.createElement("label") as HTMLLabelElement
                val cb = document.createElement("input") as HTMLInputElement
                cb.type = "checkbox"
                cb.checked = weaponCatChecks[cat] == true
                lbl.appendChild(cb)
                lbl.append(catDisplay)
                weaponCatRow.appendChild(lbl)
                weaponCatCbs[cat] = cb
            }

            // Others checkbox + text
            val othersLbl = document.createElement("label") as HTMLLabelElement
            val othersCb = document.createElement("input") as HTMLInputElement
            othersCb.type = "checkbox"
            othersCb.checked = weaponCatOther.isNotEmpty()
            othersLbl.appendChild(othersCb)
            othersLbl.append(t("features.weapon.others"))
            weaponCatRow.appendChild(othersLbl)

            val othersInput = document.createElement("input") as HTMLInputElement
            othersInput.value = weaponCatOther
            othersInput.placeholder = "Specify..."
            othersInput.style.display = if (weaponCatOther.isNotEmpty()) "inline" else "none"
            weaponCatRow.appendChild(othersInput)

            othersCb.addEventListener("change", {
                othersInput.style.display = if (othersCb.checked) "inline" else "none"
                if (!othersCb.checked) othersInput.value = ""
            })

            dynamicDiv.appendChild(weaponCatRow)

            // Individual weapons list
            val weaponListLabel = document.createElement("label") as HTMLLabelElement
            weaponListLabel.textContent = t("features.individualWeapons")
            dynamicDiv.appendChild(weaponListLabel)

            val weaponSelectedDiv = document.createElement("div") as HTMLDivElement
            weaponSelectedDiv.className = "modal__tags"

            fun refreshWeaponTags() {
                weaponSelectedDiv.innerHTML = ""
                selectedWeapons.forEach { w ->
                    val badge = document.createElement("span") as HTMLSpanElement
                    badge.textContent = "${tWeapon(w)} \u00D7"
                    badge.className = "modal__tag"
                    badge.addEventListener("click", {
                        selectedWeapons.remove(w)
                        refreshWeaponTags()
                    })
                    weaponSelectedDiv.appendChild(badge)
                }
            }
            refreshWeaponTags()
            dynamicDiv.appendChild(weaponSelectedDiv)

            val weaponAddRow = document.createElement("div") as HTMLDivElement
            weaponAddRow.className = "modal__tag-add-row"

            val weaponSelect = document.createElement("select") as HTMLSelectElement
            val wEmptyOpt = document.createElement("option") as HTMLOptionElement
            wEmptyOpt.value = ""; wEmptyOpt.textContent = "-- Add weapon --"
            weaponSelect.appendChild(wEmptyOpt)
            defaultWeapons.forEach { w ->
                val opt = document.createElement("option") as HTMLOptionElement
                opt.value = w; opt.textContent = tWeapon(w)
                weaponSelect.appendChild(opt)
            }
            val wCustomOpt = document.createElement("option") as HTMLOptionElement
            wCustomOpt.value = "__custom__"; wCustomOpt.textContent = "Custom..."
            weaponSelect.appendChild(wCustomOpt)
            weaponAddRow.appendChild(weaponSelect)

            val wCustomInput = document.createElement("input") as HTMLInputElement
            wCustomInput.placeholder = "Custom weapon"
            wCustomInput.style.display = "none"
            weaponAddRow.appendChild(wCustomInput)

            val wAddBtn = document.createElement("button") as HTMLButtonElement
            wAddBtn.textContent = "Add"
            weaponAddRow.appendChild(wAddBtn)

            weaponSelect.addEventListener("change", {
                wCustomInput.style.display = if (weaponSelect.value == "__custom__") "inline" else "none"
            })

            wAddBtn.addEventListener("click", {
                val w = if (weaponSelect.value == "__custom__") {
                    wCustomInput.value.trim().also { wCustomInput.value = "" }
                } else {
                    weaponSelect.value
                }
                if (w.isNotEmpty() && w !in selectedWeapons) {
                    selectedWeapons.add(w)
                    refreshWeaponTags()
                }
                weaponSelect.value = ""
                wCustomInput.style.display = "none"
            })

            dynamicDiv.appendChild(weaponAddRow)

            // Store references for save
            armorCbs.forEach { (k, cb) -> cb.addEventListener("change", { armorChecks[k] = cb.checked }) }
            weaponCatCbs.forEach { (k, cb) -> cb.addEventListener("change", { weaponCatChecks[k] = cb.checked }) }
        } else {
            // Name field
            val nameLbl = document.createElement("label") as HTMLLabelElement
            nameLbl.textContent = t("label.name")
            dynamicDiv.appendChild(nameLbl)
            dynamicDiv.appendChild(nameInput)

            // Description textarea
            val lbl = document.createElement("label") as HTMLLabelElement
            lbl.textContent = t("label.description")
            dynamicDiv.appendChild(lbl)
            val ta = document.createElement("textarea") as HTMLTextAreaElement
            ta.value = if (existing?.type != "Idiom") (existing?.description ?: "") else ""
            ta.rows = 4
            dynamicDiv.appendChild(ta)
            descInput = ta
        }
    }
    buildDynamicContent()
    typeSelect.addEventListener("change", { buildDynamicContent() })
    form.appendChild(dynamicDiv)

    // Tags (hidden for Idiom/Tool/Weapon-Armor)
    val selectedTags = mutableListOf<String>()
    if (existing?.tags?.isNotEmpty() == true) selectedTags.addAll(existing.tags)

    val tagsContainer = document.createElement("div") as HTMLDivElement
    tagsContainer.className = "modal__full-width"

    val tagsLabel = document.createElement("label") as HTMLLabelElement
    tagsLabel.textContent = t("label.tags")
    tagsContainer.appendChild(tagsLabel)

    val tagBadgesDiv = document.createElement("div") as HTMLDivElement
    tagBadgesDiv.className = "modal__tags"

    fun refreshTagBadges() {
        tagBadgesDiv.innerHTML = ""
        selectedTags.forEach { tag ->
            val badge = document.createElement("span") as HTMLSpanElement
            badge.textContent = "$tag \u00D7"
            badge.className = "modal__tag"
            badge.addEventListener("click", {
                selectedTags.remove(tag)
                refreshTagBadges()
            })
            tagBadgesDiv.appendChild(badge)
        }
    }
    refreshTagBadges()
    tagsContainer.appendChild(tagBadgesDiv)

    val tagAddRow = document.createElement("div") as HTMLDivElement
    tagAddRow.className = "modal__tag-add-row"

    val tagInput = document.createElement("input") as HTMLInputElement
    tagInput.placeholder = "Add tag..."
    tagAddRow.appendChild(tagInput)

    val tagAddBtn = document.createElement("button") as HTMLButtonElement
    tagAddBtn.textContent = t("btn.add")
    tagAddBtn.addEventListener("click", {
        val tag = tagInput.value.trim()
        if (tag.isNotEmpty() && tag !in selectedTags) {
            selectedTags.add(tag)
            refreshTagBadges()
        }
        tagInput.value = ""
    })
    tagAddRow.appendChild(tagAddBtn)
    tagsContainer.appendChild(tagAddRow)

    form.appendChild(tagsContainer)

    fun updateTagsVisibility() {
        val hideTagsAndName = typeSelect.value == "Idiom" || typeSelect.value == "Tool Proficiency" || typeSelect.value == "Weapon/Armor Proficiency"
        tagsContainer.style.display = if (hideTagsAndName) "none" else ""
    }
    updateTagsVisibility()
    typeSelect.addEventListener("change", { updateTagsVisibility() })

    modal.appendChild(form)

    // Buttons
    val btnRow = document.createElement("div") as HTMLDivElement
    

    val cancelBtn = document.createElement("button") as HTMLButtonElement
    cancelBtn.textContent = t("btn.cancel")
    cancelBtn.addEventListener("click", { document.body?.removeChild(overlay) })
    btnRow.appendChild(cancelBtn)

    val saveBtn = document.createElement("button") as HTMLButtonElement
    saveBtn.textContent = t("btn.save")
    saveBtn.addEventListener("click", {
        val isListType = typeSelect.value == "Idiom" || typeSelect.value == "Tool Proficiency"
        val isWeaponArmor = typeSelect.value == "Weapon/Armor Proficiency"
        val tags = if (isListType || isWeaponArmor) emptyList() else selectedTags.toList()
        val descriptionValue = when {
            isListType -> selectedItems.joinToString(", ")
            isWeaponArmor -> {
                val parts = mutableListOf<String>()
                armorChecks.forEach { (k, v) -> if (v) parts.add("armor:$k") }
                weaponCatChecks.forEach { (k, v) -> if (v) parts.add("weapon_cat:$k") }
                val othersVal = (dynamicDiv.querySelector("input[placeholder=\"Specify...\"]") as? HTMLInputElement)?.value?.trim() ?: ""
                if (othersVal.isNotEmpty()) parts.add("weapon_cat:$othersVal")
                selectedWeapons.forEach { parts.add("weapon:$it") }
                parts.joinToString(", ")
            }
            else -> descInput?.value ?: ""
        }
        val feature = DndFeature(
            id = existing?.id ?: 0,
            characterId = character.id,
            name = if (isListType || isWeaponArmor) typeSelect.value else nameInput.value,
            source = sourceSelect.value,
            sourceClass = sourceClassSelect?.value,
            sourceClassLevel = sourceClassLevelInput?.value?.toIntOrNull(),
            sourceOrigin = sourceOriginInput?.value,
            sourceRace = sourceRaceInput?.value,
            sourceSubRace = sourceSubRaceInput?.value,
            sourceCustom = sourceCustomInput?.value,
            type = typeSelect.value,
            description = descriptionValue,
            maxQuantity = if (typeSelect.value == "Rechargable Feature") maxQtyInput?.value?.toIntOrNull() else null,
            reloadRule = if (typeSelect.value == "Rechargable Feature") reloadSelect?.value else null,
            tags = tags
        )
        Repos.features.save(feature)
        document.body?.removeChild(overlay)
        onSave()
    })
    btnRow.appendChild(saveBtn)

    modal.appendChild(btnRow)
    overlay.appendChild(modal)
    document.body?.appendChild(overlay)
}

