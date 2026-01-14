/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import kotlin.brs.brsName

class TestClass {
    fun handler() {}
    fun handlerWithParam(x: Int) {}
}

fun topLevel() {}

fun test() {
    val name1 = brsName(::topLevel)
    val name2 = brsName(TestClass::handler)
    val name3 = brsName(TestClass::handlerWithParam)
}
