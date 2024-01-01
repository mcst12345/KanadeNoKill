package net.minecraft.world.storage;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class WorldInfo {
    public NBTTagCompound playerTag;
    public Map<String, NBTBase> additionalProperties;

}