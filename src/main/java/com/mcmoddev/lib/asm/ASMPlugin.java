package com.mcmoddev.lib.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mcmoddev.lib.util.Platform;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@Name("mmdlib")
@MCVersion("1.10.2")
@SortingIndex(1001)
public class ASMPlugin implements IFMLLoadingPlugin {

    static List<ITransformer> transformerList = new ArrayList<>();

    public ASMPlugin() {
        transformerList.add(new EntityHorseTransformer());
        transformerList.add(new HorseArmorTypeTransformer());
    }

    @Override
    public String[] getASMTransformerClass () {
        return new String[] { ASMTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass () {
        return null;
    }

    @Override
    public String getSetupClass () {
        return null;
    }

    @Override
    public void injectData (Map<String, Object> data) {
        Platform.setDev((Boolean) data.get("runtimeDeobfuscationEnabled"));
    }

    @Override
    public String getAccessTransformerClass () {
        return null;
    }
}