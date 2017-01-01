package com.mcmoddev.lib.client.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPool {

    public static final Class OBJ = ModelPoolObjEntry.class;
    private static Map<String, ModelPoolEntry> modelMap = new HashMap<>();
    private static String[] resourceDir = new String[] { "minecraft/resources/models/", "minecraft/resources/mod/models/" };

    public static ModelPoolEntry addFile (String file, Class modelClass, Map<String, TransformGroup> group, Map<String, TextureGroup> textureGroup) {
        ModelPoolEntry entry = null;
        if (modelMap.containsKey(file)) {
            entry = modelMap.get(file);
            entry.applyGroups(group, textureGroup);
            return entry;
        }
        try {
            entry = (ModelPoolEntry) modelClass.newInstance();
        }
        catch (final Exception e) {
            System.out.println("A new " + entry.getClass().getName() + " could not be initialized.");
            System.out.println(e.getMessage());
            return null;
        }
        File modelFile = null;
        for (int i = 0; i < resourceDir.length && (modelFile == null || !modelFile.exists()); i++) {
            String absPath = new File(Loader.instance().getConfigDir().getParent(), resourceDir[i]).getAbsolutePath();
            if (!absPath.endsWith("/") || !absPath.endsWith("\\"))
                absPath += "/";
            modelFile = entry.checkValidPath(absPath + file);
        }
        if (modelFile == null || !modelFile.exists()) {
            System.out.println("The model with the name " + file + " does not exist.");
            return null;
        }
        entry.groups = new HashMap<>();
        entry.textures = new HashMap<>();
        entry.name = file;
        entry.setGroup("0");
        entry.setTextureGroup("0");
        entry.getModel(modelFile);
        entry.applyGroups(group, textureGroup);
        modelMap.put(file, entry);
        return entry;
    }

    public static ModelPoolEntry addFileF (String file, Class modelClass, Map<String, TransformGroup> group, Map<String, TextureGroup> textureGroup) throws IOException {
        ModelPoolEntry entry = null;
        if (modelMap.containsKey(file)) {
            entry = modelMap.get(file);
            entry.applyGroups(group, textureGroup);
            return entry;
        }
        try {
            entry = (ModelPoolEntry) modelClass.newInstance();
        }
        catch (final Exception e) {
            System.out.println("A new " + entry.getClass().getName() + " could not be initialized.");
            System.out.println(e.getMessage());
            return null;
        }
        File modelFile = null;
        final InputStream in = entry.getClass().getResourceAsStream("/assets/" + file + ".obj");
        final File tempfile = File.createTempFile(file, ".obj");
        final FileOutputStream out = new FileOutputStream(tempfile);
        tempfile.deleteOnExit();
        IOUtils.copy(in, out);
        System.out.println("RENDER: " + tempfile.getPath().toString());
        modelFile = tempfile;
        if (modelFile == null || !modelFile.exists()) {
            System.out.println("The model with the name " + file + " does not exist.");
            return null;
        }
        entry.groups = new HashMap<>();
        entry.textures = new HashMap<>();
        entry.name = file;
        entry.setGroup("0");
        entry.setTextureGroup("0");
        entry.getModel(modelFile);
        entry.applyGroups(group, textureGroup);
        modelMap.put(file, entry);
        return entry;
    }
}
