package com.dungeonsanddeigo.db

import kotlin.Long
import kotlin.String

public data class DndInventoryItem(
  public val id: Long,
  public val characterId: Long,
  public val category: String,
  public val name: String,
  public val description: String,
  public val quantity: Long,
  public val equipped: Long,
)
