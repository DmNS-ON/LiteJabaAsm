package ru.DmN.lj.asm.debugger;

import ru.DmN.lj.uo.LJFunction;
import ru.DmN.lj.uo.LJModule;
import ru.DmN.lj.uo.LJOpcode;

import java.util.HashMap;
import java.util.List;

public class NativeFunction extends LJFunction {
    public final Method method;

    public NativeFunction(LJModule owner, String name, Method method) {
        super(owner, name, 0, new HashMap<>(), List.of(new LJOpcode(LJOpcode.NATIVE, new int[0])));
        this.method = method;
    }

    @FunctionalInterface
    public interface Method {
        void call(ThreadContext thread, RunContext ctx);
    }
}
