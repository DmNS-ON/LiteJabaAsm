package ru.DmN.lj.asm.debugger;

import ru.DmN.lj.uo.LJModule;

import java.util.HashMap;
import java.util.Map;

public class LoadedModule {
    public final LJModule src;
    public final Map<String, Object> variables;

    public LoadedModule(LJModule src) {
        this.src = src;
        this.variables = new HashMap<>(src.variables.size());
    }
}
