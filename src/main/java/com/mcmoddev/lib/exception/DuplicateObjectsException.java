package com.mcmoddev.lib.exception;

import net.minecraftforge.fml.common.EnhancedRuntimeException;

public class DuplicateObjectsException extends EnhancedRuntimeException {

    private final Object[] objects;

    public DuplicateObjectsException(Object... objects) {
        this.objects = objects;
    }

    @Override
    protected void printStackTrace (WrappedPrintStream stream) {
        stream.println("Duplicate Objects:");
        stream.println(String.format("\t%s", this.objects.toString()));
        stream.println("");
    }
}