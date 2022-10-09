package ru.DmN.lj.asm.debugger;

import ru.DmN.lj.uo.LJModule;

import java.io.PrintStream;
import java.util.ArrayList;

public class StdModule extends LJModule {
    public PrintStream out;

    public StdModule(PrintStream out) {
        super("std", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        this.out = out;
        this.add("exit", (thread, ctx) -> {
            throw new MainComplete((int) ctx.stack.pop());
        });
        this.add("println", (thread, ctx) -> {
            while (!ctx.stack.empty()) {
                this.out.println(ctx.stack.pop());
            }
        });
    }

    public void add(String name, NativeFunction.Method function) {
        this.functions.add(new NativeFunction(this, name, function));
    }

    public static class MainComplete extends RuntimeException {
        public final int exitCode;

        public MainComplete(int exitCode) {
            this.exitCode = exitCode;
        }
    }
}
