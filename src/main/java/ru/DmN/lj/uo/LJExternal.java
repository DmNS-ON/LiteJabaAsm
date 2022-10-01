package ru.DmN.lj.uo;

import java.util.Map;

public class LJExternal {
    public final String module;
    public final Map<String, String> variables;
    public final Map<String, String> functions;

    public LJExternal(String module, Map<String, String> variables, Map<String, String> functions) {
        this.module = module;
        this.variables = variables;
        this.functions = functions;
    }
}
