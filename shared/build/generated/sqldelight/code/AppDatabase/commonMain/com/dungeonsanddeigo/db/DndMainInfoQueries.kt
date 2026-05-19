package com.dungeonsanddeigo.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class DndMainInfoQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    mainClass: String?,
    mainSubClass: String?,
    mainClassLevel: Long?,
    secondaryClass: String?,
    secondarySubClass: String?,
    secondaryClassLevel: Long?,
    race: String?,
    subRace: String?,
    origin: String?,
    alignment: String?,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getLong(4),
      cursor.getString(5),
      cursor.getString(6),
      cursor.getLong(7),
      cursor.getString(8),
      cursor.getString(9),
      cursor.getString(10),
      cursor.getString(11)
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndMainInfo> = selectByCharacterId(characterId, ::DndMainInfo)

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    mainClass: String?,
    mainSubClass: String?,
    mainClassLevel: Long?,
    secondaryClass: String?,
    secondarySubClass: String?,
    secondaryClassLevel: Long?,
    race: String?,
    subRace: String?,
    origin: String?,
    alignment: String?,
  ): QueryResult<Long> {
    val result = driver.execute(-1_766_995_045, """
        |INSERT INTO DndMainInfo(characterId, mainClass, mainSubClass, mainClassLevel, secondaryClass, secondarySubClass, secondaryClassLevel, race, subRace, origin, alignment)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 11) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, mainClass)
          bindString(parameterIndex++, mainSubClass)
          bindLong(parameterIndex++, mainClassLevel)
          bindString(parameterIndex++, secondaryClass)
          bindString(parameterIndex++, secondarySubClass)
          bindLong(parameterIndex++, secondaryClassLevel)
          bindString(parameterIndex++, race)
          bindString(parameterIndex++, subRace)
          bindString(parameterIndex++, origin)
          bindString(parameterIndex++, alignment)
        }
    notifyQueries(-1_766_995_045) { emit ->
      emit("DndMainInfo")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    mainClass: String?,
    mainSubClass: String?,
    mainClassLevel: Long?,
    secondaryClass: String?,
    secondarySubClass: String?,
    secondaryClassLevel: Long?,
    race: String?,
    subRace: String?,
    origin: String?,
    alignment: String?,
    characterId: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-1_422_048_853, """
        |UPDATE DndMainInfo SET mainClass = ?, mainSubClass = ?, mainClassLevel = ?, secondaryClass = ?, secondarySubClass = ?, secondaryClassLevel = ?, race = ?, subRace = ?, origin = ?, alignment = ?
        |WHERE characterId = ?
        """.trimMargin(), 11) {
          var parameterIndex = 0
          bindString(parameterIndex++, mainClass)
          bindString(parameterIndex++, mainSubClass)
          bindLong(parameterIndex++, mainClassLevel)
          bindString(parameterIndex++, secondaryClass)
          bindString(parameterIndex++, secondarySubClass)
          bindLong(parameterIndex++, secondaryClassLevel)
          bindString(parameterIndex++, race)
          bindString(parameterIndex++, subRace)
          bindString(parameterIndex++, origin)
          bindString(parameterIndex++, alignment)
          bindLong(parameterIndex++, characterId)
        }
    notifyQueries(-1_422_048_853) { emit ->
      emit("DndMainInfo")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndMainInfo", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndMainInfo", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(856_021_007, """SELECT DndMainInfo.id, DndMainInfo.characterId, DndMainInfo.mainClass, DndMainInfo.mainSubClass, DndMainInfo.mainClassLevel, DndMainInfo.secondaryClass, DndMainInfo.secondarySubClass, DndMainInfo.secondaryClassLevel, DndMainInfo.race, DndMainInfo.subRace, DndMainInfo.origin, DndMainInfo.alignment FROM DndMainInfo WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndMainInfo.sq:selectByCharacterId"
  }
}
