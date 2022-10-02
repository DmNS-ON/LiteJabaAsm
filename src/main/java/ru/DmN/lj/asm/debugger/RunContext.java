package ru.DmN.lj.asm.debugger;

import ru.DmN.lj.uo.LJFunction;

import java.util.Stack;

public class RunContext {
    public int ptr;
    public Stack<Object> stack = new Stack<>();
    public Object[] variables;
    public final LJFunction function;

    public RunContext(LJFunction function) {
        this.variables = new Object[function.vcount];
        this.function = function;
    }
}
