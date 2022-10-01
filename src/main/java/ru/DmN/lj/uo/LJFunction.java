package ru.DmN.lj.uo;

import java.util.List;
import java.util.Map;

public class LJFunction {
    public final LJModule owner;
    public final String name;
    public final int vcount;
    public final Map<String, Integer> labels;
    public final List<LJOpcode> opcodes;

    public LJFunction(LJModule owner, String name, int vcount, Map<String, Integer> labels, List<LJOpcode> opcodes) {
        this.owner = owner;
        this.name = name;
        this.vcount = vcount;
        this.labels = labels;
        this.opcodes = opcodes;
    }
}
