package com.mcmoddev.lib.common.crafting;

import com.mcmoddev.lib.util.ItemStackUtils;

import net.minecraft.item.ItemStack;

public class AnvilRecipe implements IAnvilRecipe {

    /**
     * The ItemStack required in the left slot of the Anvil GUI.
     */
    private final ItemStack requiredLeft;

    /**
     * The ItemStack required in the right side of the Anvil GUI.
     */
    private final ItemStack requiredRight;

    /**
     * A name requirement for the recipe.
     */
    private final String requiredName;

    /**
     * The amount of experience levels to charge for this recipe.
     */
    private final int experienceCost;

    /**
     * The amount of items to consume, from the right slot of the Anvil GUI. If 0, the whole
     * stack will be consumed.
     */
    private final int materialCost;

    /**
     * The ItemStack output for this recipe.
     */
    private final ItemStack output;

    /**
     * Constructs a new AnvilRecipe, using all of the required parameters, except for the name
     * requirement and material cost.
     *
     * @param firstInput The ItemStack required in the left slot of the Anvil GUI.
     * @param secondInput The ItemStack required in the right slot of the Anvil GUI.
     * @param experience The amount of experience that this recipe should cost.
     * @param outputStack The ItemStack that will be created by this recipe.
     */
    public AnvilRecipe(ItemStack firstInput, ItemStack secondInput, int experience, ItemStack outputStack) {
        this(firstInput, secondInput, null, experience, 0, outputStack);
    }

    /**
     * Constructs a new AnvilRecipe, using all of the required parameters.
     *
     * @param firstInput The ItemStack required in the left slot of the Anvil GUI.
     * @param secondInput The ItemStack required in the right slot of the Anvil GUI.
     * @param requiredName A name requirement for this recipe.
     * @param experience The amount of experience that this recipe should cost.
     * @param materialCost The amount of items to consume from the right slot of the Anvil GUI.
     *        If 0, the whole stack will be consumed.
     * @param outputStack The ItemStack that will be created by this recipe.
     */
    public AnvilRecipe(ItemStack firstInput, ItemStack secondInput, String requiredName, int experience, int materialCost, ItemStack outputStack) {
        this.requiredLeft = firstInput;
        this.requiredRight = secondInput;
        this.requiredName = requiredName;
        this.experienceCost = experience;
        this.materialCost = materialCost;
        this.output = outputStack;
    }

    @Override
    public boolean isValidRecipe (ItemStack leftSlot, ItemStack rightSlot, String name) {
        return ItemStackUtils.areStacksSimilarWithSize(leftSlot, this.requiredLeft) && ItemStackUtils.areStacksSimilarWithSize(rightSlot, this.requiredRight) && this.requiredName != null && !this.requiredName.isEmpty() ? this.requiredName.equals(name) : true;
    }

    @Override
    public int getExperienceCost (ItemStack leftSlot, ItemStack rightSlot, String name) {
        return this.experienceCost;
    }

    @Override
    public int getMaterialCost (ItemStack leftSlot, ItemStack rightSlot, String name) {
        return this.materialCost;
    }

    @Override
    public ItemStack getOutput (ItemStack leftSlot, ItemStack rightSlot, String name) {
        return this.output.copy();
    }
}
