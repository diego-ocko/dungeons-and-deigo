package com.dungeonsanddeigo.db

import kotlin.Long
import kotlin.String

public data class CharacterEntity(
  public val id: Long,
  public val name: String,
  public val sheetModelName: String,
  public val imageBase64: String?,
)
