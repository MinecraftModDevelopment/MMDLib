package com.mcmoddev.lib.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mcmoddev.lib.MMDLib;

import net.minecraftforge.fml.common.discovery.ASMDataTable;

public class AnnotationChecker {

    public static <T> List<T> getInstances (ASMDataTable asmDataTable, Class<? extends Annotation> annotationClass, Class<T> instanceClass) {
        final String annotationClassName = annotationClass.getCanonicalName();
        final Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        final List<T> instances = new ArrayList<>();
        for (final ASMDataTable.ASMData asmData : asmDatas)
            try {
                instances.add(Class.forName(asmData.getClassName()).asSubclass(instanceClass).newInstance());
            }
            catch (final Exception e) {
                MMDLib.LOG.error("Failed to load: {}", asmData.getClassName(), e);
            }
        return instances;
    }
}