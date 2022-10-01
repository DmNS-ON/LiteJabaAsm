package ru.DmN.lj.uo;

public class LJConstant {
    public final int i;
    public final Type type;
    public final Object value;

    public LJConstant(int i, Type type, Object value) {
        this.i = i;
        this.type = type;
        this.value = value;
    }

    public enum Type {
        INT,
        FLOAT,
        STRING,
        REF_LABEL,
        REF_VAR,
        REF_FUN
    }
}
