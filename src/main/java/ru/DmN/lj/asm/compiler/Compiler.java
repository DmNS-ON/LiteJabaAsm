package ru.DmN.lj.asm.compiler;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import ru.DmN.lj.uo.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Compiler extends ru.DmN.lj.asm.ljaBaseListener {
    public final List<LJModule> modules = new ArrayList<>();
    protected LJModule module = null;
    protected LJFunction function = null;
    protected List<LJOpcode> opcodes = null;

    public void compile(String code) {
        new ParseTreeWalker().walk(this, new ru.DmN.lj.asm.ljaParser(new CommonTokenStream(new ru.DmN.lj.asm.ljaLexer(CharStreams.fromString(code)))).file());
    }

    @Override
    public void enterModule(ru.DmN.lj.asm.ljaParser.ModuleContext ctx) {
        this.modules.add(this.module = new LJModule(parseLiteral(ctx.LITERAL()), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
    }

    @Override
    public void exitModule(ru.DmN.lj.asm.ljaParser.ModuleContext ctx) {
        this.module = null;
    }

    @Override
    public void enterExtern(ru.DmN.lj.asm.ljaParser.ExternContext ctx) {
        var module = parseLiteral(ctx.LITERAL(0));
        var external = this.module.externals.stream().filter(e -> e.module.equals(module)).findFirst().orElseGet(() -> {
            var e = new LJExternal(module, new HashMap<>(), new HashMap<>());
            this.module.externals.add(e);
            return e;
        });
        (ctx.VAR() == null ? external.functions : external.variables).put(parseLiteral(ctx.LITERAL(1)), ctx.AS() == null ? null : parseLiteral(ctx.LITERAL(2)));
    }

    @Override
    public void enterConstant(ru.DmN.lj.asm.ljaParser.ConstantContext ctx) {
        Object value;
        LJConstant.Type type;
        if (ctx.NULL() == null) {
            if (ctx.NATURAL_NUMBER().size() == 1) {
                if (ctx.DIGITAL_NUMBER() == null) {
                    if (ctx.HEX_NUMBER() == null) {
                        if (ctx.FLOAT_NUMBER() == null) {
                            if (ctx.STRING() == null) {
                                value = parseLiteral(ctx.LITERAL());
                                type = ctx.LABEL() == null ? ctx.VAR() == null ? LJConstant.Type.REF_FUN : LJConstant.Type.REF_VAR : LJConstant.Type.REF_LABEL;
                            } else {
                                var v = ctx.STRING().getText();
                                value = v.substring(1, v.length() - 1);
                                type = LJConstant.Type.STRING;
                            }
                        } else {
                            value = Double.parseDouble(ctx.FLOAT_NUMBER().getText());
                            type = LJConstant.Type.FLOAT;
                        }
                    } else {
                        throw new UnsupportedOperationException(); // TODO: parse hex
                    }
                } else {
                    value = Integer.parseInt(ctx.DIGITAL_NUMBER().getText());
                    type = LJConstant.Type.INT;
                }
            } else {
                value = Integer.parseInt(ctx.NATURAL_NUMBER(1).getText());
                type = LJConstant.Type.INT;
            }
        } else {
            value = null;
            type = LJConstant.Type.NULL;
        }
        this.module.constants.add(new LJConstant(Integer.parseInt(ctx.NATURAL_NUMBER(0).getText()), type, value));
    }

    @Override
    public void enterVariable(ru.DmN.lj.asm.ljaParser.VariableContext ctx) {
        this.module.variables.add(new LJVariable(parseLiteral(ctx.LITERAL())));
    }

    @Override
    public void enterFunction(ru.DmN.lj.asm.ljaParser.FunctionContext ctx) {
        this.module.functions.add(this.function = new LJFunction(this.module, parseLiteral(ctx.LITERAL()), Integer.parseInt(ctx.NATURAL_NUMBER().getText()), new HashMap<>(), new ArrayList<>()));
    }

    @Override
    public void exitFunction(ru.DmN.lj.asm.ljaParser.FunctionContext ctx) {
        this.function = null;
    }

    @Override
    public void enterBody(ru.DmN.lj.asm.ljaParser.BodyContext ctx) {
        this.opcodes = this.function.opcodes;
    }

    @Override
    public void exitBody(ru.DmN.lj.asm.ljaParser.BodyContext ctx) {
        this.opcodes = null;
    }

    @Override
    public void enterLabel(ru.DmN.lj.asm.ljaParser.LabelContext ctx) {
        this.function.labels.put(parseLiteral(ctx.LITERAL()), this.opcodes.size());
        this.opcodes.add(new LJOpcode(LJOpcode.NOP, new int[0], LJDebugInfo.of(ctx.start.getLine())));
    }

    @Override
    public void enterOp0(ru.DmN.lj.asm.ljaParser.Op0Context ctx) {
        this.opcodes.add(LJOpcode.of(ctx.getText(), new int[0], LJDebugInfo.of(ctx.start.getLine())));
    }

    @Override
    public void enterOp1(ru.DmN.lj.asm.ljaParser.Op1Context ctx) {
        this.opcodes.add(LJOpcode.of(ctx.opcode.getText(), new int[]{Integer.parseInt(ctx.NATURAL_NUMBER().getText())}, LJDebugInfo.of(ctx.start.getLine())));
    }

    @Override
    public void enterCall(ru.DmN.lj.asm.ljaParser.CallContext ctx) {
        this.opcodes.add(new LJOpcode(LJOpcode.CALL, new int[]{Integer.parseInt(ctx.NATURAL_NUMBER(0).getText()), Integer.parseInt(ctx.NATURAL_NUMBER(1).getText())}, LJDebugInfo.of(ctx.start.getLine())));
    }

    public static String parseLiteral(TerminalNode node) {
        var txt = node.getText();
        if (txt.startsWith("`"))
            return txt.substring(1);
        return txt;
    }
}
