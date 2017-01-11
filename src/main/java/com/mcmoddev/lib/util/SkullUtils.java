package com.mcmoddev.lib.util;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Contains a bunch of miscellaneous tools for working with, and creating player skulls.
 * 
 * @author Tyler Hancock (Darkhax)
 */
public final class SkullUtils {
    
    /**
     * Create a skull from an instance of EntityPlayer.
     *
     * @param player The EntityPlayer to use the skin from.
     * @return ItemStack An ItemStack containing a skull that represents the passed player.
     */
    public static ItemStack createSkull (EntityPlayer player) {
        return createSkull(player.getDisplayNameString(), player.getUniqueID());
    }
    
    /**
     * Creates a skull using a players UUID.
     * 
     * @param uuid The UUID of the player to base the skull on.
     * @return ItemStack An ItemStack containing a skull which represents the owner of the
     *         passed UUID.
     */
    public static ItemStack createSkull (String name, UUID uuid) {
        final ItemStack stack = new ItemStack(Items.SKULL, 1, 3);
        ItemStackUtils.prepareDataTag(stack);
        final NBTTagCompound ownerTag = new NBTTagCompound();
        ownerTag.setString("Name", name);
        ownerTag.setString("Id", uuid.toString());
        stack.getTagCompound().setTag("SkullOwner", ownerTag);
        return stack;
    }
    
    /**
     * Creates a skull from the list of publicly provided MHF accounts.
     * 
     * @param account The MHFAccount to create the skull from.
     * @return ItemStack An ItemStack containing a skull which represents the MHF account.
     */
    public static ItemStack createSkull (MHFAccount account) {
        return createSkull(account.getMHFName());
    }
    
    /**
     * Creates a skull that represents a player. This method can use plain text usernames, or
     * player UUID. It is recommended to use the UUID over the username, unless you are 100%
     * certain that the username will never change.
     * 
     * @param owner The owner of the skull being created. Can be a username of a UUID.
     * @return ItemStack An ItemStack containing a skull which represents the passed owner
     *         name.
     */
    public static ItemStack createSkull (String owner) {
        final ItemStack stack = new ItemStack(Items.SKULL, 1, 3);
        ItemStackUtils.prepareDataTag(stack);
        stack.getTagCompound().setString("SkullOwner", owner);
        return stack;
    }
    
    /**
     * Creates a vanilla Wither Skeleton Skull.
     * 
     * @return ItemStack An ItemStack containing a vanilla wither skeleton skull.
     */
    public static ItemStack getWitherSkeletonSkull () {
        return new ItemStack(Items.SKULL, 1, 1);
    }
    
    /**
     * Creates a vanilla Zombie Skull.
     * 
     * @return ItemStack An ItemStack containing a vanilla zombie skull.
     */
    public static ItemStack getZombieSkull () {
        return new ItemStack(Items.SKULL, 1, 2);
    }
    
    /**
     * Creates a vanilla Creeper Skull.
     * 
     * @return ItemStack An ItemStack containing a vanilla creeper skull.
     */
    public static ItemStack getCreeperSkull () {
        return new ItemStack(Items.SKULL, 1, 4);
    }
    
    /**
     * Creates a vanilla Steve Skull.
     * 
     * @return ItemStack An ItemStack containing a vanilla steve skull.
     */
    public static ItemStack getSteveSkull () {
        return new ItemStack(Items.SKULL, 1, 3);
    }
    
    /**
     * Creates a vanilla Skeleton Skull.
     * 
     * @return ItemStack An ItemStack containing a vanilla skeleton skull.
     */
    public static ItemStack getSkeletonSkull () {
        return new ItemStack(Items.SKULL, 1, 0);
    }
    
    /**
     * Creates an array of ItemStacks containing MHF Skulls.
     * 
     * @return ItemStack[] An array of ItemStack containing every skull from the MHFAccount
     *         enum.
     */
    public static ItemStack[] getMHFSkulls () {
        int counter = 0;
        final ItemStack[] MHFSkulls = new ItemStack[MHFAccount.values().length];
        for (final MHFAccount account : MHFAccount.values()) {
            MHFSkulls[counter] = createSkull(account);
            counter++;
        }
        return MHFSkulls;
    }
    
    /**
     * An enum of all accounts contained within Marc's Head Format. MHF is a list of accounts
     * provided by Mojang to give builders better building blocks within the world. The skins
     * of said players will never change.
     * 
     * @author Tyler Hancock (Darkhax)
     */
    public static enum MHFAccount {
        ALEX("Alex", "6ab43178-89fd-4905-97f6-0f67d9d76fd9"),
        BLAZE("Blaze", "4c38ed11-596a-4fd4-ab1d-26f386c1cbac"),
        CAVE_SPIDER("CaveSpider", "cab28771-f0cd-4fe7-b129-02c69eba79a5"),
        CHICKEN("Chicken", "92deafa9-4307-42d9-b003-88601598d6c0"),
        COW("Cow", "f159b274-c22e-4340-b7c1-52abde147713"),
        ENDERMAN("Enderman", "40ffb372-12f6-4678-b3f2-2176bf56dd4b"),
        GHAST("Ghast", "063085a6-797f-4785-be1a-21cd7580f752"),
        GOLEM("Golem", "757f90b2-2344-4b8d-8dac-824232e2cece"),
        HEROBRINE("Herobrine", "9586e5ab-157a-4658-ad80-b07552a9ca63"),
        LAVASLIME("LavaSlime", "0972bdd1-4b86-49fb-9ecc-a353f8491a51"),
        MOOSHROOM("MushroomCow", "a46817d6-73c5-4f3f-b712-af6b3ff47b96"),
        OCELOT("Ocelot", "1bee9df5-4f71-42a2-bf52-d97970d3fea3"),
        PIG("Pig", "8b57078b-f1bd-45df-83c4-d88d16768fbe"),
        PIG_ZOMBIE("PigZombie", "18a2bb50-334a-4084-9184-2c380251a24b"),
        SHEEP("Sheep", "dfaad551-4e7e-45a1-a6f7-c6fc5ec823ac"),
        SLIME("Slime", "870aba93-40e8-48b3-89c5-32ece00d6630"),
        SPIDER("Spider", "5ad55f34-41b6-4bd2-9c32-18983c635936"),
        SQUID("Squid", "72e64683-e313-4c36-a408-c66b64e94af5"),
        STEVE("Steve", "c06f8906-4c8a-4911-9c29-ea1dbd1aab82"),
        VILLAGER("Villager", "bd482739-767c-45dc-a1f8-c33c40530952"),
        CACTUS("Cactus", "1d9048db-e836-4b9a-a108-55014922f1ae"),
        CAKE("Cake", "afb489c4-9fc8-48a4-98f2-dd7bea414c9a"),
        CHEST("Chest", "73d4e068-3a6d-4c8b-8f85-3323546955c4"),
        COCONUT_BROWN("CoconutB", "62efa973-f626-4092-aede-57ffbe84ff2b"),
        COCONUT_GREEN("CoconutG", "74556fea-28ed-4458-8db2-9a8220da0c12"),
        MELON("Melon", "1c7d9784-47ea-4bf3-bc23-acf260b436e6"),
        LOG("OakLog", "e224e5ec-e299-4005-ae22-3b0f77a57714"),
        PUMPKIN("Pumpkin", "f44d355b-b6ae-4ba8-8e62-ae6441854785"),
        TNT1("TNT", "d43af93c-c330-4a3d-bab8-ee74234a011a"),
        TNT2("TNT2", "55e73380-a973-4a52-9bb5-1efa5256125c"),
        ARROW_UP("ArrowUp", "fef039ef-e6cd-4987-9c84-26a3e6134277"),
        ARROW_DOWN("ArrowDown", "68f59b9b-5b0b-4b05-a9f2-e1d1405aa348"),
        ARROW_LEFT("ArrowLeft", "a68f0b64-8d14-4000-a95f-4b9ba14f8df9"),
        ARROW_RIGHT("ArrowRight", "50c8510b-5ea0-4d60-be9a-7d542d6cd156"),
        EXCLAMATION("Exclamation", "d3c47f6f-ae3a-45c1-ad7c-e2c762b03ae6"),
        QUESTION("Question", "606e2ff0-ed77-4842-9d6c-e1d3321c7838"),
        PRESENT_GREEN("Present1", "156b251b-12e0-4829-a130-a61b53ba7720"),
        PRESENT_RED("Present2", "f1eb7cad-e2c0-4e9e-8aad-1eae21d5fd95");
        
        /**
         * The base of the username, without the MHF prefix.
         */
        private final String username;
        
        /**
         * The UUID tied to the account.
         */
        public String UUID;
        
        /**
         * An enumeration of all accounts provided by Mojang under the MHF format.
         * 
         * @param username The username tied to the account.
         * @param uuid The uuid tied to the account.
         */
        MHFAccount(String username, String uuid) {
            this.username = username;
            this.UUID = uuid;
        }
        
        /**
         * Provides the base name for this skull. This is the base name, and not the full
         * username. Use getMHFName to get an actual username that can be used.
         * 
         * @return String The skull name, without the MHF_ prefix.
         */
        public String getBaseName () {
            return this.username;
        }
        
        /**
         * Provides the username in the MHF format. The MHF format is a format used by Mojang
         * to provide a series of additional player names which can reliably be used for things
         * like skulls.
         * 
         * @return String The basic username, with the MHF_ prefix.
         */
        public String getMHFName () {
            return "MHF_" + this.username;
        }
    }
}