package keiyoushi.gradle.extensions

import java.io.Serializable

@DslMarker
annotation class OverridesDsl

/**
 * A typed value passed to a source's primary constructor via `source { overrides { } }`.
 * The concrete variant is retained so the compiler can emit a correctly-typed literal
 * (e.g. `Int` vs `Long`), which JSON numbers alone would not preserve.
 */
sealed interface OverrideValue : Serializable {
    data class IntValue(val value: Int) : OverrideValue
    data class LongValue(val value: Long) : OverrideValue
    data class BooleanValue(val value: Boolean) : OverrideValue
    data class StringValue(val value: String) : OverrideValue
}

@OverridesDsl
class OverridesBuilder {

    // Preserve declaration order for stable, readable generated code.
    private val values = LinkedHashMap<String, OverrideValue>()

    infix fun String.to(value: Int) {
        values[this] = OverrideValue.IntValue(value)
    }

    infix fun String.to(value: Long) {
        values[this] = OverrideValue.LongValue(value)
    }

    infix fun String.to(value: Boolean) {
        values[this] = OverrideValue.BooleanValue(value)
    }

    infix fun String.to(value: String) {
        values[this] = OverrideValue.StringValue(value)
    }

    fun build(): Map<String, OverrideValue> = values
}
