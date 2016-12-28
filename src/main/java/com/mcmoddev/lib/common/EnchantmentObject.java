package com.mcmoddev.lib.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * A new type for representing enchantment instances. This is very similar to
 * {@link EnchantmentData} however the level is not final, and it can be written to and read
 * from byte buffers.
 * 
 * @author Tyler Hancock (Darkhax)
 */
public class EnchantmentObject {
    
    /**
     * The enchantment held by the Enchantment Object. This can not be changed.
     */
    private final Enchantment enchantment;
    
    /**
     * The level of the enchantment.
     */
    private int level;
    
    /**
     * Constructs a new EnchantmentObject from the base requirements.
     * 
     * @param enchantment The enchantment to use.
     * @param level The level of the enchantment.
     */
    public EnchantmentObject(Enchantment enchantment, int level) {
        
        this.enchantment = enchantment;
        this.level = level;
    }
    
    /**
     * Constructs a new EnchantmentObject from a byte buffer. The first piece of data read is
     * expected to be a UTF8 string that represents an enchantment ID. The second piece of
     * expected data is an integer which represents the level of the enchantment.
     * 
     * @param buf The buffer of bytes to read from.
     */
    public EnchantmentObject(ByteBuf buf) {
        
        this.enchantment = Enchantment.getEnchantmentByLocation(ByteBufUtils.readUTF8String(buf));
        this.level = buf.readInt();
    }
    
    /**
     * Gets the enchantment being represented.
     * 
     * @return The enchantment being represented.
     */
    public Enchantment getEnchantment () {
        
        return this.enchantment;
    }
    
    /**
     * Gets the level of the enchantment effect.
     * 
     * @return The level of the effect.
     */
    public int getLevel () {
        
        return this.level;
    }
    
    /**
     * Sets the level of the enchantment effect.
     * 
     * @param level The new level for the effect.
     */
    public void setLevel (int level) {
        
        this.level = level;
    }
    
    /**
     * Writes the stored data to a byte buffer. Data is written in the same way that the
     * ByteBuf constructor expects.
     * 
     * @param buf The buffer of bytes to write to.
     */
    public void writeToBuffer (ByteBuf buf) {
        
        ByteBufUtils.writeUTF8String(buf, this.enchantment.getRegistryName().toString());
        buf.writeInt(this.level);
    }
}