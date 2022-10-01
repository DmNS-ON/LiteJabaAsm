package ru.DmN.lj.uo;

public class LJDebugInfo {
    public final int line;

    public LJDebugInfo(int line) {
        this.line = line;
    }

    public static LJDebugInfo of(int line) {
        return new LJDebugInfo(line);
    }

    @Override
    public String toString() {
        return "[" + line + ']';
    }
}
