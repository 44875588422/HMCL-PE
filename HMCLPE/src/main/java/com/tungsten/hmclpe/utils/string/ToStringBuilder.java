package com.tungsten.hmclpe.utils.string;

public class ToStringBuilder {

    private final StringBuilder stringBuilder;
    private boolean first = true;

    public ToStringBuilder(Object object) {
        stringBuilder = new StringBuilder(object.getClass().getSimpleName()).append(" [");
    }

    public ToStringBuilder append(String name, Object content) {
        if (!first)
            stringBuilder.append(", ");
        first = false;
        stringBuilder.append(name).append('=').append(content);
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString() + "]";
    }
}
