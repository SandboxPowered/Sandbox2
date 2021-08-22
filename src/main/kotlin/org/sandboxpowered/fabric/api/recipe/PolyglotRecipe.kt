package org.sandboxpowered.fabric.api.recipe

import com.google.gson.JsonObject
import net.minecraft.util.Identifier
import org.sandboxpowered.fabric.api.recipe.PolyglotIngredient as Ingredient

interface PolyglotRecipe {
    val id: Identifier
    val recipeJson: JsonObject

    fun hasInput(ingredient: Ingredient)
    fun hasOutput(ingredient: Ingredient)

    fun replaceInput(
        before: Ingredient,
        after: Ingredient,
        configurator: (Ingredient, Ingredient) -> Ingredient = { original, new ->
            new.withCount(original.getCount())
        }
    )

    fun replaceOutput(
        before: Ingredient,
        after: Ingredient,
        configurator: (Ingredient, Ingredient) -> Ingredient = { original, new ->
            new.withCount(original.getCount())
        }
    )
}