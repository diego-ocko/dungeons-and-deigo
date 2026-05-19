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

public class DndInventoryItemQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByCharacterIdAndCategory(
    characterId: Long,
    category: String,
    mapper: (
      id: Long,
      characterId: Long,
      category: String,
      name: String,
      description: String,
      quantity: Long,
      equipped: Long,
    ) -> T,
  ): Query<T> = SelectByCharacterIdAndCategoryQuery(characterId, category) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun selectByCharacterIdAndCategory(characterId: Long, category: String): Query<DndInventoryItem> = selectByCharacterIdAndCategory(characterId, category, ::DndInventoryItem)

  public fun <T : Any> selectByCharacterId(characterId: Long, mapper: (
    id: Long,
    characterId: Long,
    category: String,
    name: String,
    description: String,
    quantity: Long,
    equipped: Long,
  ) -> T): Query<T> = SelectByCharacterIdQuery(characterId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun selectByCharacterId(characterId: Long): Query<DndInventoryItem> = selectByCharacterId(characterId, ::DndInventoryItem)

  public fun lastInsertId(): ExecutableQuery<Long> = Query(889_142_042, driver, "DndInventoryItem.sq", "lastInsertId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insert(
    characterId: Long,
    category: String,
    name: String,
    description: String,
    quantity: Long,
    equipped: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-726_319_255, """INSERT INTO DndInventoryItem(characterId, category, name, description, quantity, equipped) VALUES (?, ?, ?, ?, ?, ?)""", 6) {
          var parameterIndex = 0
          bindLong(parameterIndex++, characterId)
          bindString(parameterIndex++, category)
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, description)
          bindLong(parameterIndex++, quantity)
          bindLong(parameterIndex++, equipped)
        }
    notifyQueries(-726_319_255) { emit ->
      emit("DndInventoryItem")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun update(
    name: String,
    description: String,
    quantity: Long,
    equipped: Long,
    id: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-381_373_063, """UPDATE DndInventoryItem SET name = ?, description = ?, quantity = ?, equipped = ? WHERE id = ?""", 5) {
          var parameterIndex = 0
          bindString(parameterIndex++, name)
          bindString(parameterIndex++, description)
          bindLong(parameterIndex++, quantity)
          bindLong(parameterIndex++, equipped)
          bindLong(parameterIndex++, id)
        }
    notifyQueries(-381_373_063) { emit ->
      emit("DndInventoryItem")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteById(id: Long): QueryResult<Long> {
    val result = driver.execute(528_231_629, """DELETE FROM DndInventoryItem WHERE id = ?""", 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, id)
        }
    notifyQueries(528_231_629) { emit ->
      emit("DndInventoryItem")
    }
    return result
  }

  private inner class SelectByCharacterIdAndCategoryQuery<out T : Any>(
    public val characterId: Long,
    public val category: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndInventoryItem", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndInventoryItem", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_602_175_436, """SELECT DndInventoryItem.id, DndInventoryItem.characterId, DndInventoryItem.category, DndInventoryItem.name, DndInventoryItem.description, DndInventoryItem.quantity, DndInventoryItem.equipped FROM DndInventoryItem WHERE characterId = ? AND category = ?""", mapper, 2) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
      bindString(parameterIndex++, category)
    }

    override fun toString(): String = "DndInventoryItem.sq:selectByCharacterIdAndCategory"
  }

  private inner class SelectByCharacterIdQuery<out T : Any>(
    public val characterId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DndInventoryItem", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DndInventoryItem", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_704_361_215, """SELECT DndInventoryItem.id, DndInventoryItem.characterId, DndInventoryItem.category, DndInventoryItem.name, DndInventoryItem.description, DndInventoryItem.quantity, DndInventoryItem.equipped FROM DndInventoryItem WHERE characterId = ?""", mapper, 1) {
      var parameterIndex = 0
      bindLong(parameterIndex++, characterId)
    }

    override fun toString(): String = "DndInventoryItem.sq:selectByCharacterId"
  }
}
