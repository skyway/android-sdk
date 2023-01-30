/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content.local

import com.ntt.skyway.core.content.Stream

abstract class LocalStream internal constructor(dto: Dto) : Stream(dto) {
    override val side = Side.LOCAL
}
