package ru.DmN.lj.uo;

import java.util.Arrays;

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
        INT(0),
        FLOAT(1),
        STRING(2),
        REF_LABEL(3),
        REF_VAR(4),
        REF_FUN(5),
        NULL(6);

        public final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type of(int id) {
            return Arrays.stream(values()).filter(e -> e.id == id).findFirst().get();
        }
    }
}
