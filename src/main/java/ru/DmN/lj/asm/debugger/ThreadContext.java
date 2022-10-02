package ru.DmN.lj.asm.debugger;

import java.util.Stack;

public class ThreadContext {
    public final Stack<RunContext> contexts = new Stack<>();
}
