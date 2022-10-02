package ru.DmN.lj.asm.debugger;

public class MathUtils {
    public static Object add(Object a, Object b) {
        return toNumber(toDouble(a) + toDouble(b));
    }

    public static Object sub(Object a, Object b) {
        return toNumber(toDouble(a) - toDouble(b));
    }

    public static Object mul(Object a, Object b) {
        return toNumber(toDouble(a) * toDouble(b));
    }

    public static Object div(Object a, Object b) {
        return toNumber(toDouble(a) / toDouble(b));
    }

    public static Object mod(Object a, Object b) {
        return toNumber(toDouble(a) % toDouble(b));
    }

    public static Object toNumber(double d) {
        if (d - (int) d == 0)
            return (int) d;
        return d;
    }

    public static double toDouble(Object o) {
        if (o instanceof Boolean b) {
            return b ? 1 : 0;
        } else if (o instanceof Integer i) {
            return i;
        } else if (o instanceof Double d) {
            return d;
        } else {
            throw new IllegalArgumentException(); // todo: description
        }
    }
}
