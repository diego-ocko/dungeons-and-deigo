package com.dungeonsanddeigo.search

import com.dungeonsanddeigo.model.*

data class SearchResults(
    val stats: List<StatResult> = emptyList(),
    val skills: List<SkillResult> = emptyList(),
    val features: List<FeatureResult> = emptyList(),
    val combinedProficiencies: List<CombinedProficiency> = emptyList(),
    val weapons: List<WeaponResult> = emptyList(),
    val armors: List<ArmorResult> = emptyList(),
    val magicItems: List<MagicItemResult> = emptyList(),
    val consumables: List<ConsumableResult> = emptyList(),
    val keyItems: List<KeyItemResult> = emptyList(),
    val spells: List<SpellResult> = emptyList(),
    val highlightFeatureIds: Set<Long> = emptySet(),
    val highlightConsumableIds: Set<Long> = emptySet(),
    val highlightSpellIds: Set<Long> = emptySet(),
    val highlightNonAttackSpellIds: Set<Long> = emptySet(),
    val highlightWeaponAttacks: Boolean = false
)

data class StatResult(val name: String, val abbr: String, val modifier: Int, val saveValue: Int, val children: List<Any> = emptyList())
data class SkillResult(val name: String, val trained: Boolean, val totalValue: Int, val children: List<Any> = emptyList())
data class FeatureResult(val feature: DndFeature, val indent: Boolean = false)
data class CombinedProficiency(val type: String, val translatedType: String, val description: String)
data class WeaponResult(val weapon: DndWeapon, val indent: Boolean = false)
data class ArmorResult(val armor: DndArmor, val indent: Boolean = false)
data class MagicItemResult(val item: DndMagicItem, val indent: Boolean = false)
data class ConsumableResult(val item: DndConsumable, val indent: Boolean = false)
data class KeyItemResult(val item: DndInventoryItem, val indent: Boolean = false)
data class SpellResult(val spell: DndSpell, val indent: Boolean = false)

data class NoteSearchResults(
    val appearanceFields: List<FieldResult> = emptyList(),
    val backstoryFields: List<FieldResult> = emptyList(),
    val notes: List<DndNote> = emptyList()
)

data class FieldResult(val label: String, val value: String, val icon: String)

object SearchEngine {

    fun matchesTag(tag: String, tags: List<String>, description: String = "", plainSearch: Boolean = false): Boolean {
        val t = tag.lowercase()
        return tags.any { it.lowercase() == t } || (plainSearch && description.lowercase().contains(t))
    }

    fun filterFeaturesByTag(
        tag: String,
        features: List<DndFeature>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndFeature> {
        return features.filter { feat ->
            feat.id !in excludeIds && matchesTag(tag, feat.tags, feat.description, plainSearch)
        }
    }

    fun filterWeaponsByTag(
        tag: String,
        weapons: List<DndWeapon>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndWeapon> {
        return weapons.filter { w ->
            w.id !in excludeIds && matchesTag(tag, w.tags, w.additionalFeatures, plainSearch)
        }
    }

    fun filterArmorsByTag(
        tag: String,
        armors: List<DndArmor>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndArmor> {
        return armors.filter { a ->
            a.id !in excludeIds && matchesTag(tag, a.tags, a.additionalFeatures, plainSearch)
        }
    }

    fun filterMagicItemsByTag(
        tag: String,
        items: List<DndMagicItem>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndMagicItem> {
        return items.filter { m ->
            m.id !in excludeIds && matchesTag(tag, m.tags, m.effect, plainSearch)
        }
    }

    fun filterConsumablesByTag(
        tag: String,
        items: List<DndConsumable>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndConsumable> {
        return items.filter { c ->
            c.id !in excludeIds && matchesTag(tag, c.tags, c.effect, plainSearch)
        }
    }

    fun filterKeyItemsByTag(
        tag: String,
        items: List<DndInventoryItem>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndInventoryItem> {
        return items.filter { k ->
            k.id !in excludeIds && matchesTag(tag, k.tags, k.description, plainSearch)
        }
    }

    fun filterSpellsByTag(
        tag: String,
        spells: List<DndSpell>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndSpell> {
        return spells.filter { s ->
            s.id !in excludeIds && matchesTag(tag, s.tags, s.description, plainSearch)
        }
    }

    fun filterFeaturesByName(
        query: String,
        features: List<DndFeature>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndFeature> {
        return features.filter {
            it.id !in excludeIds && (it.name.lowercase().contains(query) || (plainSearch && it.description.lowercase().contains(query)))
        }
    }

    fun filterWeaponsByName(query: String, weapons: List<DndWeapon>, excludeIds: Set<Long>, plainSearch: Boolean): List<DndWeapon> {
        return weapons.filter { it.id !in excludeIds && (it.name.lowercase().contains(query) || (plainSearch && it.additionalFeatures.lowercase().contains(query))) }
    }

    fun filterArmorsByName(query: String, armors: List<DndArmor>, excludeIds: Set<Long>, plainSearch: Boolean): List<DndArmor> {
        return armors.filter { it.id !in excludeIds && (it.name.lowercase().contains(query) || (plainSearch && it.additionalFeatures.lowercase().contains(query))) }
    }

    fun filterMagicItemsByName(query: String, items: List<DndMagicItem>, excludeIds: Set<Long>, plainSearch: Boolean): List<DndMagicItem> {
        return items.filter { it.id !in excludeIds && (it.name.lowercase().contains(query) || (plainSearch && it.effect.lowercase().contains(query))) }
    }

    fun filterConsumablesByName(query: String, items: List<DndConsumable>, excludeIds: Set<Long>, plainSearch: Boolean): List<DndConsumable> {
        return items.filter { it.id !in excludeIds && (it.name.lowercase().contains(query) || (plainSearch && it.effect.lowercase().contains(query))) }
    }

    fun filterKeyItemsByName(query: String, items: List<DndInventoryItem>, excludeIds: Set<Long>, plainSearch: Boolean): List<DndInventoryItem> {
        return items.filter { it.id !in excludeIds && (it.name.lowercase().contains(query) || (plainSearch && it.description.lowercase().contains(query))) }
    }

    fun filterSpellsByName(query: String, spells: List<DndSpell>, excludeIds: Set<Long>, plainSearch: Boolean): List<DndSpell> {
        return spells.filter { it.id !in excludeIds && (it.name.lowercase().contains(query) || (plainSearch && it.description.lowercase().contains(query))) }
    }

    fun filterByTagPartial(query: String, tags: List<String>): Boolean {
        return tags.any { it.lowercase().contains(query) }
    }

    fun findRelatedFeatures(
        feature: DndFeature,
        allFeatures: List<DndFeature>,
        excludeIds: Set<Long>,
        plainSearch: Boolean
    ): List<DndFeature> {
        return allFeatures.filter { related ->
            related.id !in excludeIds && (
                matchesTag(feature.name, related.tags, related.description, plainSearch) ||
                feature.tags.any { ft -> matchesTag(ft, related.tags, related.description, plainSearch) }
            )
        }
    }

    fun searchNotes(
        query: String,
        plainSearch: Boolean,
        appearance: DndAppearance?,
        backstory: DndBackstory?,
        notes: List<DndNote>,
        appearanceLabels: List<Pair<String, String>>,
        backstoryLabels: List<Pair<String, String>>
    ): NoteSearchResults {
        val appFields = if (appearance != null) {
            appearanceLabels.filter { (label, value) ->
                value.isNotEmpty() && (label.lowercase().contains(query) || (plainSearch && value.lowercase().contains(query)))
            }.map { (label, value) -> FieldResult(label, value, "\uD83D\uDC64") }
        } else emptyList()

        val bsFields = if (backstory != null) {
            backstoryLabels.filter { (label, value) ->
                value.isNotEmpty() && (label.lowercase().contains(query) || (plainSearch && value.lowercase().contains(query)))
            }.map { (label, value) -> FieldResult(label, value, "\uD83D\uDCDC") }
        } else emptyList()

        val matchedNotes = notes.filter {
            it.title.lowercase().contains(query) ||
            it.tags.any { tag -> tag.lowercase().contains(query) } ||
            (plainSearch && it.note.lowercase().contains(query))
        }

        return NoteSearchResults(appFields, bsFields, matchedNotes)
    }
}
