package ru.DmN.lj.uo;

public class LJOpcode {
    public static final int NOP = 0;
    //
    public static final int POP = 1;
    public static final int SWAP = 2;
    public static final int DUP = 3;
    //
    public static final int ADD = 4;
    public static final int SUB = 5;
    public static final int MUL = 6;
    public static final int DIV = 7;
    public static final int MOD = 8;
    //
    public static final int CMPEQ = 9;
    public static final int CMPNEQ = 10;
    public static final int CMPGT = 11;
    public static final int CMPLS = 12;
    //
    public static final int RET = 13;
    public static final int RETV = 14;
    public static final int RETS = 15;
    //
    public static final int GETL = 16;
    public static final int GETG = 17;
    //
    public static final int CONVI = 18;
    public static final int CONVD = 19;
    //
    public static final int AC = 20;
    public static final int AL = 21;
    public static final int AS = 22;
    public static final int AG = 23;
    //
    public static final int STJMP = 24;
    public static final int STCALL = 25;
    //
    public static final int PUSH = 26;
    //
    public static final int JMP = 27;
    public static final int JMPS = 28;
    public static final int JMPIF = 29;
    public static final int CALL = 30;
    //
    public static final int SETL = 31;
    public static final int SETG = 32;
    //
    public static final int NATIVE = 254;
    //
    public static final int DEBUG = 255;

    public final int op; // 1 byte
    public final int[] additional; // 2 bytes
    public final LJDebugInfo debugInfo;

    public LJOpcode(int op, int[] additional) {
        this.op = op;
        this.additional = additional;
        this.debugInfo = null;
    }

    public LJOpcode(int op, int[] additional, LJDebugInfo debugInfo) {
        this.op = op;
        this.additional = additional;
        this.debugInfo = debugInfo;
    }

    public static LJOpcode of(String opcode, int[] additional) {
        return of(opcode, additional, null);
    }

    public static LJOpcode of(String opcode, int[] additional, LJDebugInfo debugInfo) {
        try {
            return new LJOpcode((Integer) LJOpcode.class.getField(opcode.toUpperCase()).get(null), additional, debugInfo);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
