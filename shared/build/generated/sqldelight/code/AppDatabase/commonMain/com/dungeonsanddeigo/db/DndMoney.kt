package com.dungeonsanddeigo.db

import kotlin.Long
import kotlin.String

public data class DndMoney(
  public val id: Long,
  public val characterId: Long,
  public val copper: Long,
  public val silver: Long,
  public val electrum: Long,
  public val gold: Long,
  public val platinum: Long,
  public val lifestyle: String,
)
