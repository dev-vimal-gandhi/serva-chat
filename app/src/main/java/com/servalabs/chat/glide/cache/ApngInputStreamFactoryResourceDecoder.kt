/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.glide.cache

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.servalabs.chat.apng.ApngDecoder
import com.servalabs.chat.glide.apng.ApngOptions
import com.servalabs.chat.glide.common.io.InputStreamFactory
import java.io.IOException

class ApngInputStreamFactoryResourceDecoder : ResourceDecoder<InputStreamFactory, ApngDecoder> {

  override fun handles(source: InputStreamFactory, options: Options): Boolean {
    return if (options.get(ApngOptions.ANIMATE) == true) {
      ApngDecoder.isApng(source.create())
    } else {
      false
    }
  }

  @Throws(IOException::class)
  override fun decode(source: InputStreamFactory, width: Int, height: Int, options: Options): Resource<ApngDecoder>? {
    val decoder = ApngDecoder.create { source.create() }
    return ApngResource(decoder)
  }
}
