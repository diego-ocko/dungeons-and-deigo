package com.dungeonsanddeigo.db

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class DndFeaturesQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    name: String,
    source: String,
    sourceClass: String?,
    sourceClassLevel: Long?,
    sourceOrigin: String?,
    sourceRace: String?,
    sourceSubRace: String?,
    sourceCustom: String?,
    type: String,
    description: String,
    maxQuantity: Long?,
    reloadRule: String?,
    currentUsages: Long,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getLong(5),
      cursor.getString(6),
      cursor.getString(7),
      cursor.getString(8),
      cursor.getString(9),
      cursor.getString(10)!!,
      cursor.getString(11)!!,
      cursor.getLong(12),
      cursor.getString(13),
      cursor.getLong(14)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndFeatures> = selectByCharacterId(characterId, ::DndFeatures)

  public fun <T : Any> selectById(id: Long, mapper: (
    id: Long,
    characterId: Long,
    name: String,
    source: String,
    sourceClass: String?,
    sourceClassLevel: Long?,
    sourceOrigin: String?,
    sourceRace: String?,
    sourceSubRace: String?,
    sourceCustom: String?,
    type: String,
    description: String,
    maxQuantity: Long?,
    reloadRule: String?,
    currentUsages: Long,
  ) -> T): Query<T> = SelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getLong(5),
      cursor.getString(6),
      cursor.getString(7),
      cursor.getString(8),
      cursor.getString(9),
      cursor.getString(10)!!,
      cursor.getString(11)!!,
      cursor.getLong(12),
      cursor.getString(13),
      cursor.getLong(14)!!
    )
  }

  public fun selectById(id: Long): Query<DndFeatures> = selectById(id, ::DndFeatures)

  public fun lastInsertId(): ExecutableQuery<Long> = Query(-1_156_990_974, driver, "DndFeatures.sq", "lastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> selectTagsByFeatureId(featureId: Long, mapper: (
    id: Long,
    featureId: Long,
    tag: String,
  ) -> T): Query<T> = SelectTagsByFeatureIdQuery(featureId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!
    )
  }

  public fun selectTagsByFeatureId(featureId: Long): Query<DndFeatureTags> = selectTagsByFeatureId(featureId, ::DndFeatureTags)

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    name: String,
    source: String,
    sourceClass: String?,
    sourceClassLevel: Long?,
    sourceOrigin: String?,
    sourceRace: String?,
    sourceSubRace: String?,
    sourceCustom: String?,
    type: String,
    description: String,
    maxQuantity: Long?,
    reloadRule: String?,
    currentUsages: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_610_189_393, """
        |INSERT INTO DndFeatures(characterId, name, source, sourceClass, sourceClassLevel, sourceOrigin, sourceRace, sourceSubRace, sourceCustom, type, description, maxQuantity, reloadRule, currentUsages)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 14) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, source)
          bindString(parameterIndex++, sourceClass)
          bindLong(parameterIndex++, sourceClassLevel)
          bindString(parameterIndex++, sourceOrigin)
          bindString(parameterIndex++, sourceRace)
          bindString(parameterIndex++, sourceSubRace)
          bindString(parameterIndex++, sourceCustom)
          bindString(parameterIndex++, type)
          bindString(parameterIndex++, description)
          bindLong(parameterIndex++, maxQuantity)
          bindString(parameterIndex++, reloadRule)
          bindLong(parameterIndex++, currentUsages)
        }
    notifyQueries(1_610_189_393) { emit ->
      emit("DndFeatures")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    name: String,
    source: String,
    sourceClass: String?,
    sourceClassLevel: Long?,
    sourceOrigin: String?,
    sourceRace: String?,
    sourceSubRace: String?,
    sourceCustom: String?,
    type: String,
    description: String,
    maxQuantity: Long?,
    reloadRule: String?,
    currentUsages: Long,
    id: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_955_135_585, """
        |UPDATE DndFeatures SET name = ?, source = ?, sourceClass = ?, sourceClassLevel = ?, sourceOrigin = ?, sourceRace = ?, sourceSubRace = ?, sourceCustom = ?, type = ?, description = ?, maxQuantity = ?, reloadRule = ?, currentUsages = ?
        |WHERE id = ?
        """.trimMargin(), 14) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, source)
          bindString(parameterIndex++, sourceClass)
          bindLong(parameterIndex++, sourceClassLevel)
          bindString(parameterIndex++, sourceOrigin)
          bindString(parameterIndex++, sourceRace)
          bindString(parameterIndex++, sourceSubRace)
          bindString(parameterIndex++, sourceCustom)
          bindString(parameterIndex++, type)
          bindString(parameterIndex++, description)
          bindLong(parameterIndex++, maxQuantity)
          bindString(parameterIndex++, reloadRule)
          bindLong(parameterIndex++, currentUsages)
          bindLong(parameterIndex++, id)
        }
    notifyQueries(1_955_135_585) { emit ->
      emit("DndFeatures")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteById(id: Long): QueryResult<Long> {
    val result = driver.execute(-2_007_972_939, """DELETE FROM DndFeatures WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, id)
        }
    notifyQueries(-2_007_972_939) { emit ->
      emit("DndFeatures")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertTag(featureId: Long, tag: String): QueryResult<Long> {
    val result = driver.execute(-1_337_438_327, """INSERT INTO DndFeatureTags(featureId, tag) VALUES (?, ?)""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, featureId)
          bindString(parameterIndex++, tag)
        }
    notifyQueries(-1_337_438_327) { emit ->
      emit("DndFeatureTags")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteTagsByFeatureId(featureId: Long): QueryResult<Long> {
    val result = driver.execute(-1_430_939_010, """DELETE FROM DndFeatureTags WHERE featureId = ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, featureId)
        }
    notifyQueries(-1_430_939_010) { emit ->
      emit("DndFeatureTags")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndFeatures", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndFeatures", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_256_993_049, """SELECT DndFeatures.id, DndFeatures.characterId, DndFeatures.name, DndFeatures.source, DndFeatures.sourceClass, DndFeatures.sourceClassLevel, DndFeatures.sourceOrigin, DndFeatures.sourceRace, DndFeatures.sourceSubRace, DndFeatures.sourceCustom, DndFeatures.type, DndFeatures.description, DndFeatures.maxQuantity, DndFeatures.reloadRule, DndFeatures.currentUsages FROM DndFeatures WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndFeatures.sq:selectByCharacterId"
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndFeatures", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndFeatures", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_133_550_970, """SELECT DndFeatures.id, DndFeatures.characterId, DndFeatures.name, DndFeatures.source, DndFeatures.sourceClass, DndFeatures.sourceClassLevel, DndFeatures.sourceOrigin, DndFeatures.sourceRace, DndFeatures.sourceSubRace, DndFeatures.sourceCustom, DndFeatures.type, DndFeatures.description, DndFeatures.maxQuantity, DndFeatures.reloadRule, DndFeatures.currentUsages FROM DndFeatures WHERE id = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, id)
    }

    override fun toString(): String = "DndFeatures.sq:selectById"
  }

  private inner class SelectTagsByFeatureIdQuery<out T : Any>(
    public val featureId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndFeatureTags", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndFeatureTags", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(1_139_818_253, """SELECT DndFeatureTags.id, DndFeatureTags.featureId, DndFeatureTags.tag FROM DndFeatureTags WHERE featureId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, featureId)
    }

    override fun toString(): String = "DndFeatures.sq:selectTagsByFeatureId"
  }
}
