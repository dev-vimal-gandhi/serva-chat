/*
 * Copyright 2019 Zhou Pengfei
 * SPDX-License-Identifier: Apache-2.0
 */

package com.servalabs.chat.glide.common.loader;

import com.servalabs.chat.glide.common.io.Reader;

import java.io.IOException;

/**
 * @Description: Loader
 * @Author: pengfei.zhou
 * @CreateDate: 2019-05-14
 */
public interface Loader {
    Reader obtain() throws IOException;
}
