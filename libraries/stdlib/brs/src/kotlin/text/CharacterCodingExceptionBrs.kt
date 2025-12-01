/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

@SinceKotlin("1.4")
public actual open class CharacterCodingException(message: String?) : Exception(message) {
    public actual constructor() : this(null)
}
