package org.sandboxpowered.fabric.api

import net.minecraft.state.property.*
import org.graalvm.polyglot.HostAccess.Export

class PolyglotStateProperty private constructor(
    private val property: Property<*>?,
    @Export val name: String,
    @Export val type: String,
    val extra: Array<out Any>
) {
    // TODO: Coded : figure out what to do with this
    //Int Property
    @Export
    fun values(): Array<Int> {
        if (type == INT) return extra as Array<Int>
        throw UnsupportedOperationException("Cannot get minimum value of $type property")
    }

    companion object {
        infix fun from(property: Property<*>) = PolyglotStateProperty(
            property = property,
            name = property.name,
            type = when (property) {
                is BooleanProperty -> BOOLEAN
                is IntProperty -> INT
                is DirectionProperty -> DIRECTION
                is EnumProperty -> ENUM
                else -> error("Unknown Property type: $property")
            },
            extra = when (property) {
                is BooleanProperty -> emptyArray()
                else -> property.values.toTypedArray()
            }
        )

        fun from(name: String, type: String, extra: Array<out Any> = emptyArray()) = PolyglotStateProperty(
            property = null,
            name = name,
            type = type,
            extra = extra
        )

        const val BOOLEAN = "boolean"
        const val INT = "int"
        const val DIRECTION = "direction"
        const val ENUM = "enum"
    }
}