package com.dungeonsanddeigo.repository

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dungeonsanddeigo.db.AppDatabase
import com.dungeonsanddeigo.model.DndFeature

class AndroidDndFeaturesRepository(context: Context) : DndFeaturesRepository {
    private val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "characters.db")
    private val database = AppDatabase(driver)
    private val queries = database.dndFeaturesQueries

    override fun getByCharacterId(characterId: Long): List<DndFeature> {
        return queries.selectByCharacterId(characterId).executeAsList().map { r ->
            val tags = queries.selectTagsByFeatureId(r.id).executeAsList().map { it.tag }
            DndFeature(
                id = r.id, characterId = r.characterId,
                name = r.name, source = r.source, sourceClass = r.sourceClass,
                sourceClassLevel = r.sourceClassLevel?.toInt(),
                sourceOrigin = r.sourceOrigin, sourceRace = r.sourceRace,
                sourceSubRace = r.sourceSubRace, sourceCustom = r.sourceCustom,
                type = r.type, description = r.description,
                maxQuantity = r.maxQuantity?.toInt(), reloadRule = r.reloadRule,
                currentUsages = r.currentUsages.toInt(),
                tags = tags
            )
        }
    }

    override fun save(feature: DndFeature): Long {
        val featureId: Long
        if (feature.id == 0L) {
            queries.insert(
                feature.characterId, feature.name, feature.source, feature.sourceClass,
                feature.sourceClassLevel?.toLong(), feature.sourceOrigin,
                feature.sourceRace, feature.sourceSubRace, feature.sourceCustom,
                feature.type, feature.description,
                feature.maxQuantity?.toLong(), feature.reloadRule,
                feature.currentUsages.toLong()
            )
            featureId = queries.lastInsertId().executeAsOne()
        } else {
            queries.update(
                feature.name, feature.source, feature.sourceClass, feature.sourceClassLevel?.toLong(),
                feature.sourceOrigin, feature.sourceRace, feature.sourceSubRace,
                feature.sourceCustom, feature.type, feature.description,
                feature.maxQuantity?.toLong(), feature.reloadRule,
                feature.currentUsages.toLong(), feature.id
            )
            featureId = feature.id
        }
        queries.deleteTagsByFeatureId(featureId)
        feature.tags.forEach { queries.insertTag(featureId, it) }
        return featureId
    }

    override fun delete(featureId: Long) {
        queries.deleteTagsByFeatureId(featureId)
        queries.deleteById(featureId)
    }
}
