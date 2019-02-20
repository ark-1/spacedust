package me.arkadybazhanov.spacedust

import kotlin.reflect.*

operator fun <R> KProperty0<R>.getValue(instance: Any?, metadata: KProperty<*>): R = get()
operator fun <R> KMutableProperty0<R>.setValue(instance: Any?, metadata: KProperty<*>, value: R) = set(value)
operator fun <T, R> KProperty1<T, R>.getValue(instance: T, metadata: KProperty<*>): R = get(instance)
operator fun <T, R> KMutableProperty1<T, R>.setValue(instance: T, metadata: KProperty<*>, value: R) = set(instance, value)