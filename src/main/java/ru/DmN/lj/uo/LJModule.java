package ru.DmN.lj.uo;

import java.util.List;

public class LJModule {
    public final String name;
    public final List<LJExternal> externals;
    public final List<LJConstant> constants;
    public final List<LJVariable> variables;
    public final List<LJFunction> functions;

    public LJModule(String name, List<LJExternal> externals, List<LJConstant> constants, List<LJVariable> variables, List<LJFunction> functions) {
        this.name = name;
        this.constants = constants;
        this.variables = variables;
        this.functions = functions;
        this.externals = externals;
    }
}
