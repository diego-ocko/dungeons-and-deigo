package com.dungeonsanddeigo.db

import kotlin.Long
import kotlin.String

public data class DndMainInfo(
  public val id: Long,
  public val characterId: Long,
  public val mainClass: String?,
  public val mainSubClass: String?,
  public val mainClassLevel: Long?,
  public val secondaryClass: String?,
  public val secondarySubClass: String?,
  public val secondaryClassLevel: Long?,
  public val race: String?,
  public val subRace: String?,
  public val origin: String?,
  public val alignment: String?,
)
