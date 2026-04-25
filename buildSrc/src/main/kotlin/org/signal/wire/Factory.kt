package com.servalabs.chat.wire

import com.squareup.wire.schema.SchemaHandler

class Factory : SchemaHandler.Factory {
  override fun create(): SchemaHandler {
    return Handler()
  }
}
