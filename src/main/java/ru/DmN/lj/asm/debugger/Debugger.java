package ru.DmN.lj.asm.debugger;

import ru.DmN.lj.asm.compiler.Compiler;
import ru.DmN.lj.uo.LJFunction;
import ru.DmN.lj.uo.LJModule;
import ru.DmN.lj.uo.LJOpcode;
import ru.DmN.lj.uo.LJVariable;

import java.io.PrintStream;
import java.util.*;

public class Debugger {
    public final List<LoadedModule> modules = new ArrayList<>();
    public NativeFunction.Method debug;
    public boolean printDebugTrace;
    public PrintStream debugStream;

    public ThreadContext eval(Compiler compiler, String mainModule) {
        var module = new LoadedModule(compiler.modules.stream().filter(e -> e.name.equals(mainModule)).findFirst().orElseThrow(() -> new RuntimeException("Модуль `" + mainModule + "` не найден!")));
        this.modules.add(module);
        return this.eval(module);
    }

    public ThreadContext eval(String module) {
        return this.eval(this.modules.stream().filter(e -> e.src.name.equals(module)).findFirst().orElseThrow(() -> new RuntimeException("Модуль `" + module + "` не найден!")));
    }

    public ThreadContext eval(LoadedModule module) {
        var thread = new ThreadContext();
        thread.contexts.add(new RunContext(module.src.functions.stream().filter(e -> e.name.equals("main")).findFirst().orElseThrow(() -> new RuntimeException("Функция `main` в модуле " + module.src.name + " не найдена!"))));
        return eval(thread);
    }

    public ThreadContext eval(ThreadContext thread) {
        while (!thread.contexts.empty()) {
            var ctx = thread.contexts.pop();
            var constants = ctx.function.owner.constants;
            var opcodes = ctx.function.opcodes;
            var stack = ctx.stack;
            var variables = ctx.variables;
            ctx:
            for (; ctx.ptr < opcodes.size(); ctx.ptr++) {
                var oper = opcodes.get(ctx.ptr);
                if (this.printDebugTrace) {
                    debugStream.println((oper.debugInfo == null ? (oper.op == LJOpcode.DEBUG ? "[debug]" : oper.op == LJOpcode.NATIVE ? "[native]" : "[idk]") : oper.debugInfo) + ctx.stack.toString());
                }
                switch (oper.op) {
                    case LJOpcode.NOP -> {
                    }
                    //
                    case LJOpcode.POP -> stack.pop();
                    case LJOpcode.SWAP -> {
                        var a = stack.pop();
                        var b = stack.pop();
                        stack.push(a);
                        stack.push(b);
                    }
                    case LJOpcode.DUP -> stack.push(stack.peek());
                    //
                    case LJOpcode.ADD -> stack.push(MathUtils.add(stack.pop(), stack.pop()));
                    case LJOpcode.SUB -> stack.push(MathUtils.sub(stack.pop(), stack.pop()));
                    case LJOpcode.MUL -> stack.push(MathUtils.mul(stack.pop(), stack.pop()));
                    case LJOpcode.DIV -> stack.push(MathUtils.div(stack.pop(), stack.pop()));
                    case LJOpcode.MOD -> stack.push(MathUtils.mod(stack.pop(), stack.pop()));
                    //
                    case LJOpcode.CMPEQ -> stack.push(Objects.equals(stack.pop(), stack.pop()));
                    case LJOpcode.CMPNEQ -> stack.push(!Objects.equals(stack.pop(), stack.pop()));
                    case LJOpcode.CMPGT -> stack.push(popd(stack) > popd(stack));
                    case LJOpcode.CMPLS -> stack.push(popd(stack) < popd(stack));
                    case LJOpcode.CMPN -> stack.push(stack.pop() == null);
                    case LJOpcode.CMPNN -> stack.push(stack.pop() != null);
                    //
                    case LJOpcode.RET -> {
                        break ctx;
                    }
                    case LJOpcode.RETV -> {
                        thread.contexts.peek().stack.push(stack.pop());
                        break ctx;
                    }
                    case LJOpcode.RETS -> {
                        thread.contexts.push((RunContext) stack.pop());
                        break ctx;
                    }
                    //
                    case LJOpcode.GETL -> stack.push(variables[(int) constants.get(oper.additional[0]).value]);
                    case LJOpcode.GETG -> {
                        var id = oper.additional[0];
                        var module = getModule(ctx.function.owner, id);
                        stack.push(module.variables.get(getVariable(ctx.function.owner, module.src, id).name));
                    }
                    //
                    case LJOpcode.CONVF -> stack.push(popd(stack));
                    case LJOpcode.CONVI -> stack.push((int) popd(stack));
                    //
                    case LJOpcode.AC -> stack.push(new Object[(int) stack.pop()]);
                    case LJOpcode.AL -> stack.push(((Object[]) stack.pop()).length);
                    case LJOpcode.AS -> ((Object[]) stack.pop())[(int) popd(stack)] = stack.pop();
                    case LJOpcode.AG -> stack.push(((Object[]) stack.pop())[(int) popd(stack)]);
                    //
                    case LJOpcode.STJMP -> ctx.ptr = ctx.function.labels.get((String) stack.pop());
                    case LJOpcode.STCALL -> {
                        ctx.ptr++;
                        thread.contexts.push(ctx);
                        ctx = new RunContext(getFunction(ctx.function.owner, (String) stack.pop()));
                        var i = (int) stack.pop();
                        while (i-- > 0)
                            ctx.stack.push(stack.pop());
                        thread.contexts.push(ctx);
                        break ctx;
                    }
                    //
                    case LJOpcode.PUSH -> ctx.stack.push(constants.get(oper.additional[0]).value);
                    //
                    case LJOpcode.JMP -> ctx.ptr = ctx.function.labels.get(constants.get(oper.additional[0]).value);
                    case LJOpcode.JMPIF -> {
                        if ((boolean) stack.pop())
                            ctx.ptr = ctx.function.labels.get(constants.get(oper.additional[0]).value);
                    }
                    case LJOpcode.JMPS -> {
                        ctx.ptr++;
                        stack.push(ctx);
                        ctx = new RunContext(this.getFunction(ctx.function.owner, this.getModule(ctx.function.owner, oper.additional[0]).src, oper.additional[0]));
                        ctx.stack = stack;
                        ctx.variables = variables;
                        thread.contexts.push(ctx);
                        break ctx;
                    }
                    case LJOpcode.CALL -> {
                        ctx.ptr++;
                        thread.contexts.push(ctx);
                        var i = (int) constants.get(oper.additional[1]).value;
                        var module = this.getModule(ctx.function.owner, oper.additional[0]);
                        ctx = new RunContext(this.getFunction(ctx.function.owner, module.src, oper.additional[0]));
                        while (i-- > 0) {
                            ctx.stack.push(stack.pop());
                        }
                        thread.contexts.push(ctx);
                        break ctx;
                    }
                    //
                    case LJOpcode.SETL -> variables[(int) constants.get(oper.additional[0]).value] = stack.pop();
                    case LJOpcode.SETG -> {
                        var id = oper.additional[0];
                        var module = getModule(ctx.function.owner, id);
                        module.variables.put(getVariable(ctx.function.owner, module.src, id).name, stack.pop());
                    }
                    //
                    case LJOpcode.NATIVE -> ((NativeFunction) ctx.function).method.call(thread, ctx);
                    //
                    case LJOpcode.DEBUG -> this.debug.call(thread, ctx);
                }
            }
        }

        return thread;
    }

    public LoadedModule getModule(String name) {
        return this.modules.stream().filter(e -> e.src.name.equals(name)).findFirst().orElseThrow(() -> new RuntimeException("Модуль `" + name + "` не найден!"));
    }

    public LJFunction getFunction(LJModule caller, String name) {
        for (var external : caller.externals) {
            if (external.functions.containsKey(name)) {
                var nname = external.functions.get(name);
                if (nname != null)
                    name = nname;
                return getFunction(caller, getModule(external.module).src, name);
            } else if (external.functions.containsValue(name)) {
                String fname = name;
                return getFunction(caller, getModule(external.module).src, external.functions.entrySet().stream().filter(e -> e.getValue().equals(fname)).findFirst().get().getKey());
            }
        }
        throw new RuntimeException();
    }

    public LJFunction getFunction(LJModule caller, LJModule callable, String name) {
        for (var external : caller.externals) {
            if (external.functions.containsKey(name)) {
                var nname = external.functions.get(name);
                if (nname != null)
                    name = nname;
                break;
            } else if (external.functions.containsValue(name)) {
                String fname = name;
                name = external.functions.entrySet().stream().filter(e -> e.getValue().equals(fname)).findFirst().get().getKey();
                break;
            }
        }
        String fname = name;
        return callable.functions.stream().filter(e -> e.name.equals(fname)).findFirst().get();
    }

    public LoadedModule getModule(LJModule caller, int id) {
        var name = (String) caller.constants.get(id).value;
        String mname = null;
        if (caller.variables.stream().anyMatch(e -> e.name.equals(name)) || caller.functions.stream().anyMatch(e -> e.name.equals(name))) {
            mname = caller.name;
        } else {
            for (var external : caller.externals) {
                if (external.variables.containsKey(name) || external.variables.containsValue(name) || external.functions.containsKey(name) || external.functions.containsValue(name)) {
                    mname = external.module;
                }
            }
        }
        String finalMname = mname;
        return this.modules.stream().filter(e -> finalMname.equals(e.src.name)).findFirst().get();
    }

    public LJVariable getVariable(LJModule caller, LJModule callable, int id) {
        var name = (String) caller.constants.get(id).value;
        for (var external : caller.externals) {
            if (external.variables.containsKey(name)) {
                var nname = external.variables.get(name);
                if (nname != null)
                    name = nname;
                break;
            } else if (external.variables.containsValue(name)) {
                String fname = name;
                name = external.variables.entrySet().stream().filter(e -> e.getValue().equals(fname)).findFirst().get().getKey();
                break;
            }
        }
        String fname = name;
        return callable.variables.stream().filter(e -> e.name.equals(fname)).findFirst().get();
    }

    public LJFunction getFunction(LJModule caller, LJModule callable, int id) {
        return getFunction(caller, callable, (String) caller.constants.get(id).value);
    }

    public static double popd(Stack<Object> stack) {
        var o = stack.pop();
        if (o instanceof Boolean v)
            return v ? 1 : 0;
        if (o instanceof Integer v)
            return v;
        return (double) o;
    }
}
