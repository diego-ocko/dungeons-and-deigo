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

public class CharacterEntityQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (
    id: Long,
    name: String,
    sheetModelName: String,
    imageBase64: String?,
  ) -> T): Query<T> = Query(-1_102_344_424, arrayOf("CharacterEntity"), driver, "CharacterEntity.sq", "selectAll", "SELECT CharacterEntity.id, CharacterEntity.name, CharacterEntity.sheetModelName, CharacterEntity.imageBase64 FROM CharacterEntity") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)
    )
  }

  public fun selectAll(): Query<CharacterEntity> = selectAll(::CharacterEntity)

  public fun <T : Any> selectById(id: Long, mapper: (
    id: Long,
    name: String,
    sheetModelName: String,
    imageBase64: String?,
  ) -> T): Query<T> = SelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)
    )
  }

  public fun selectById(id: Long): Query<CharacterEntity> = selectById(id, ::CharacterEntity)

  public fun lastInsertId(): ExecutableQuery<Long> = Query(975_663_479, driver, "CharacterEntity.sq", "lastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    name: String,
    sheetModelName: String,
    imageBase64: String?,
  ): QueryResult<Long> {
    val result = driver.execute(-1_872_183_418, """INSERT INTO CharacterEntity(name, sheetModelName, imageBase64) VALUES (?, ?, ?)""", 3) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, sheetModelName)
          bindString(parameterIndex++, imageBase64)
        }
    notifyQueries(-1_872_183_418) { emit ->
      emit("CharacterEntity")
    }
    return result
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CharacterEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CharacterEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(187_102_523, """SELECT CharacterEntity.id, CharacterEntity.name, CharacterEntity.sheetModelName, CharacterEntity.imageBase64 FROM CharacterEntity WHERE id = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, id)
    }

    override fun toString(): String = "CharacterEntity.sq:selectById"
  }
}
