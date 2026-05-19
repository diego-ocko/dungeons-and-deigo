package com.dungeonsanddeigo.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class DndMagicItem(
  public val id: Long,
  public val characterId: Long,
  public val name: String,
  public val needSynch: Long,
  public val isSynched: Long,
  public val effect: String,
  public val weight: Double,
  public val price: Long,
  public val priceCurrency: String,
  public val tags: String,
)
