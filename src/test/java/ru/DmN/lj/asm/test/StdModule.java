package ru.DmN.lj.asm.test;

import ru.DmN.lj.asm.debugger.NativeFunction;
import ru.DmN.lj.uo.LJModule;

import java.util.ArrayList;

public class StdModule extends LJModule {
    public StdModule() {
        super("std", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        this.functions.add(new NativeFunction(this, "println", (thread, ctx) -> {
            while (!ctx.stack.empty()) {
                System.out.println(ctx.stack.pop());
            }
        }));
    }
}
