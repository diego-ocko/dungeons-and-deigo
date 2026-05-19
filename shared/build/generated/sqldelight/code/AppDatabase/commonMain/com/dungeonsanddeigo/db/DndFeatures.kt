package com.dungeonsanddeigo.db

import kotlin.Long
import kotlin.String

public data class DndFeatures(
  public val id: Long,
  public val characterId: Long,
  public val name: String,
  public val source: String,
  public val sourceClass: String?,
  public val sourceClassLevel: Long?,
  public val sourceOrigin: String?,
  public val sourceRace: String?,
  public val sourceSubRace: String?,
  public val sourceCustom: String?,
  public val type: String,
  public val description: String,
  public val maxQuantity: Long?,
  public val reloadRule: String?,
  public val currentUsages: Long,
)
