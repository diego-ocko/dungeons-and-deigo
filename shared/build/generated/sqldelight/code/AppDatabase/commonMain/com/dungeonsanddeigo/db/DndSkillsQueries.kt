package com.dungeonsanddeigo.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class DndSkillsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    skillsJson: String,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndSkills> = selectByCharacterId(characterId, ::DndSkills)

  /**
   * @return The number of rows updated.
   */
  public fun insert(characterId: Long, skillsJson: String): QueryResult<Long> {
    val result = driver.execute(488_545_110, """INSERT INTO DndSkills(characterId, skillsJson) VALUES (?, ?)""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, skillsJson)
        }
    notifyQueries(488_545_110) { emit ->
      emit("DndSkills")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(skillsJson: String, characterId: Long): QueryResult<Long> {
    val result = driver.execute(833_491_302, """UPDATE DndSkills SET skillsJson = ? WHERE characterId = ?""", 2) {
          var parameterIndex = 0
          bindString(parameterIndex++, skillsJson)
          bindLong(parameterIndex++, characterId)
        }
    notifyQueries(833_491_302) { emit ->
      emit("DndSkills")
    }
    return result
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndSkills", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndSkills", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-174_084_044, """SELECT DndSkills.id, DndSkills.characterId, DndSkills.skillsJson FROM DndSkills WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndSkills.sq:selectByCharacterId"
  }
}
