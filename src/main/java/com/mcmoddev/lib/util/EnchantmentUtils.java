package com.mcmoddev.lib.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * A collection of miscellaneous utilities for working with enchantments.
 * 
 * @author Tyler Hancock (Darkhax)
 */
public final class EnchantmentUtils {
    
    /**
     * A check to see if an ItemStack can be enchanted. For this to be true, the ItemStack must
     * have an enchantability grater than 0, and be a book, enchanted book, or an tool.
     * 
     * @param itemStack The ItemStack to check.
     * @return boolean Whether or not the ItemStack is enchantable.
     */
    public static boolean isItemEnchantable (ItemStack itemStack) {
        return itemStack.getItem().getItemEnchantability(itemStack) > 0 && (itemStack.getItem() == Items.BOOK || itemStack.getItem() == Items.ENCHANTED_BOOK || itemStack.isItemEnchantable());
    }
    
    /**
     * Calculates how many experience points are it would take to get to the specified level.
     * 
     * @param level The level to calculate for.
     * @return int The amount of experience points required to go from level 0 to the specified
     *         level.
     */
    public static int getExperienceFromLevel (int level) {
        return (int) (level < 16 ? 17 * level : level > 15 && level < 31 ? 1.5f * (level * level) - 29.5f * level + 360 : 3.5f * (level * level) - 151.5f * level + 2220);
    }
    
    /**
     * Calculate the amount of experience to go from one level to another.
     * 
     * @param startingLevel The level you are currently at.
     * @param destinationLevel The level you want to go to.
     * @return int The amount of experience points needed to go from the startingLevel to the
     *         destinationLevel.
     */
    public static int getExperienceToLevel (int startingLevel, int destinationLevel) {
        return getExperienceFromLevel(destinationLevel) - getExperienceFromLevel(startingLevel);
    }
    
    /**
     * Calculates the amount of levels that an amount of EXP is equal to. This is a pretty
     * costly method due to the level curve.
     * 
     * @param exp The amount of EXP to solve for.
     * @return int The amount of levels that can be created by the amount of exp.
     */
    public static int getLevelsFromExperience (int exp) {
        int currentLevel = 0;
        float levelCap = getExperienceToLevel(currentLevel, currentLevel + 1);
        while (exp >= levelCap) {
            exp -= levelCap;
            currentLevel += 1;
            levelCap = getExperienceToLevel(currentLevel, currentLevel + 1);
        }
        return currentLevel;
    }
    
    /**
     * Checks if two enchantments are compatible with each other. For two enchantments to be
     * compatible, both of their canApplyTegether methods must return true.
     * 
     * @param firstEnchantment The first Enchantment to check.
     * @param secondEnchantment The second Enchantment to check.
     * @return boolean Whether or not the two enchantments are compatible or not.
     */
    public static boolean areEnchantmentsCompatible (Enchantment firstEnchantment, Enchantment secondEnchantment) {
        return firstEnchantment.canApplyTogether(secondEnchantment) && secondEnchantment.canApplyTogether(firstEnchantment);
    }
    
    /**
     * Gets the display string for an amount of EXP.
     * 
     * @param xp The amount of experience to represent.
     * @return The amount to display.
     */
    public static String getExpForDisplay (int xp) {
        final int levels = getLevelsFromExperience(xp);
        return levels > 0 ? levels + "L" : xp + "xp";
    }
}