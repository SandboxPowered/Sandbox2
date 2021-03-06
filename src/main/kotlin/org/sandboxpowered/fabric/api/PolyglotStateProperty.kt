package org.sandboxpowered.fabric.api

import net.minecraft.state.property.*
import org.graalvm.polyglot.HostAccess.Export

class PolyglotStateProperty private constructor(
    private val property: Property<*>?,
    @JvmField @Export val name: String,
    @JvmField @Export val type: String,
    @JvmField @Export val values: Array<*>
) {
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
            values = when (property) {
                is BooleanProperty -> emptyArray()
                else -> property.values.toTypedArray()
            }
        )

        fun from(name: String, type: String, extra: Array<*> = emptyArray<Any>()) = PolyglotStateProperty(
            property = null,
            name = name,
            type = type,
            values = extra
        )

        const val BOOLEAN = "boolean"
        const val INT = "int"
        const val DIRECTION = "direction"
        const val ENUM = "enum"
    }
}