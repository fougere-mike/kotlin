/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.contracts

import kotlin.internal.ContractsDsl

/**
 * This marker distinguishes the experimental contract declaration API and is used to opt-in for that feature
 * when declaring contracts of user functions.
 */
@SinceKotlin("1.3")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS)
public annotation class ExperimentalContracts

/**
 * Specifies the contract of a function.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public inline fun contract(builder: ContractBuilder.() -> Unit) { }

/**
 * Provides a scope, where the functions of the contract DSL can be used to describe the contract of a function.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface ContractBuilder {
    /**
     * Describes a situation when a function returns normally.
     */
    public fun returns(): Returns

    /**
     * Describes a situation when a function returns normally with the specified return [value].
     */
    public fun returns(value: Any?): Returns

    /**
     * Describes a situation when a function returns normally with any value that is not `null`.
     */
    public fun returnsNotNull(): ReturnsNotNull

    /**
     * Specifies that the function parameter [lambda] is invoked in place.
     */
    public fun <R> callsInPlace(lambda: Function<R>, kind: InvocationKind = InvocationKind.UNKNOWN): CallsInPlace
}

/**
 * Represents an effect of a function being called and returning normally.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface Returns : SimpleEffect

/**
 * Represents an effect of a function being called and returning normally with a not-null value.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface ReturnsNotNull : SimpleEffect

/**
 * Represents an effect of a function parameter being called in place.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface CallsInPlace : Effect

/**
 * Specifies how many times a function invokes its function parameter in place.
 */
@ExperimentalContracts
@SinceKotlin("1.3")
public enum class InvocationKind {
    /** The lambda is called at most once. */
    AT_MOST_ONCE,
    /** The lambda is called exactly once. */
    EXACTLY_ONCE,
    /** The lambda is called at least once. */
    AT_LEAST_ONCE,
    /** The lambda's invocation count is unknown. */
    UNKNOWN
}

/**
 * An effect of some condition being true after observing another effect of a function.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface ConditionalEffect : Effect

/**
 * An effect that can be observed after a function invocation.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface Effect

/**
 * An effect that can be used in implies clause.
 */
@ContractsDsl
@ExperimentalContracts
@SinceKotlin("1.3")
public interface SimpleEffect : Effect {
    /**
     * Specifies that this effect, when observed, guarantees [booleanExpression] to be true.
     */
    public infix fun implies(booleanExpression: Boolean): ConditionalEffect
}
