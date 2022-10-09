package ru.DmN.lj.uo;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializationUtils {
    public static void writeModule(OutputStream stream, LJModule module) throws IOException {
        writeString(stream, module.name);

        writeInt(stream, module.externals.size());
        for (var e : module.externals) {
            writeExternal(stream, e);
        }

        writeInt(stream, module.constants.size());
        for (var e : module.constants) {
            writeConstant(stream, e);
        }

        writeInt(stream, module.variables.size());
        for (var e : module.variables) {
            writeString(stream, e.name);
        }

        writeInt(stream, module.functions.size());
        for (var e : module.functions) {
            writeFunction(stream, e);
        }
    }

    public static LJModule readModule(InputStream stream) throws IOException {
        var externals = new ArrayList<LJExternal>();
        var constants = new ArrayList<LJConstant>();
        var variables = new ArrayList<LJVariable>();
        var functions = new ArrayList<LJFunction>();
        var module = new LJModule(readString(stream), externals, constants, variables, functions);

        var i = readInt(stream);
        while (i-- > 0) {
            externals.add(readExternal(stream));
        }

        var j = readInt(stream);
        while (j-- > 0) {
            constants.add(readConstant(stream));
        }

        var l = readInt(stream);
        while (l-- > 0) {
            variables.add(new LJVariable(readString(stream)));
        }

        var k = readInt(stream);
        while (k-- > 0) {
            functions.add(readFunction(stream, module));
        }

        return module;
    }

    public static void writeFunction(OutputStream stream, LJFunction function) throws IOException {
        writeString(stream, function.name);
        writeInt(stream, function.vcount);

        writeInt(stream, function.labels.size());
        for (var e : function.labels.entrySet()) {
            writeString(stream, e.getKey());
            writeInt(stream, e.getValue());
        }

        writeInt(stream, function.opcodes.size());
        for (var e : function.opcodes) {
            writeOpcode(stream, e);
        }
    }

    public static LJFunction readFunction(InputStream stream, LJModule owner) throws IOException {
        var name = readString(stream);
        var vcount = readInt(stream);

        var labels = new HashMap<String, Integer>();
        var i = readInt(stream);
        while (i-- > 0) {
            labels.put(readString(stream), readInt(stream));
        }

        var opcodes = new ArrayList<LJOpcode>();
        var j = readInt(stream);
        while (j-- > 0) {
            opcodes.add(readOpcode(stream));
        }

        return new LJFunction(owner, name, vcount, labels, opcodes);
    }

    public static void writeOpcode(OutputStream stream, LJOpcode opcode) throws IOException {
        writeInt(stream, opcode.op);
        writeInt(stream, opcode.additional.length);
        for (var e : opcode.additional) {
            writeInt(stream, e);
        }
    }

    public static LJOpcode readOpcode(InputStream stream) throws IOException {
        var op = readInt(stream);
        var i = readInt(stream);
        var additional = new int[i];
        for (int j = 0; j < i; j++) {
            additional[j] = readInt(stream);
        }
        return new LJOpcode(op, additional);
    }

    public static void writeConstant(OutputStream stream, LJConstant constant) throws IOException {
        writeInt(stream, constant.i);
        writeInt(stream, constant.type.id);
        switch (constant.type) {
            case NULL -> {
            }
            case INT -> writeInt(stream, (Integer) constant.value);
            case FLOAT -> writeFloat(stream, (Double) constant.value);
            default -> writeString(stream, (String) constant.value);
        }
    }

    public static LJConstant readConstant(InputStream stream) throws IOException {
        var id = readInt(stream);
        var type = LJConstant.Type.of(readInt(stream));
        var value = switch(type) {
            case NULL -> null;
            case INT -> readInt(stream);
            case FLOAT -> readFloat(stream);
            default -> readString(stream);
        };
        return new LJConstant(id, type, value);
    }

    public static void writeExternal(OutputStream stream, LJExternal external) throws IOException {
        writeString(stream, external.module);

        writeInt(stream, external.variables.size());
        for (var e : external.variables.entrySet()) {
            writeString(stream, e.getKey());
            writeString(stream, e.getValue());
        }

        writeInt(stream, external.functions.size());
        for (var e : external.functions.entrySet()) {
            writeString(stream, e.getKey());
            writeString(stream, e.getValue());
        }
    }

    public static LJExternal readExternal(InputStream stream) throws IOException {
        var name = readString(stream);

        var variables = new HashMap<String, String>();
        var i = readInt(stream);
        while(i-- > 0) {
            variables.put(readString(stream), readString(stream));
        }

        var functions = new HashMap<String, String>();
        var j = readInt(stream);
        while(j-- > 0) {
            functions.put(readString(stream), readString(stream));
        }

        return new LJExternal(name, variables, functions);
    }

    public static void writeString(OutputStream stream, String str) throws IOException {
        if (str == null) {
            writeInt(stream, 0);
        } else {
            var b = str.getBytes();
            writeInt(stream, b.length);
            stream.write(b);
        }
    }

    public static String readString(InputStream stream) throws IOException {
        var i = readInt(stream);
        if (i == 0)
            return null;
        return new String(stream.readNBytes(i));
    }

    public static void writeFloat(OutputStream stream, double d) throws IOException {
        var v = Double.doubleToRawLongBits(d);
        writeInt(stream, (int) (v >> 32));
        writeInt(stream, (int) (v));
    }

    public static double readFloat(InputStream stream) throws IOException {
        return Double.longBitsToDouble((long) readInt(stream) << 32 | readInt(stream));
    }

    public static void writeInt(OutputStream stream, int i) throws IOException {
        stream.write((byte) (i >> 24));
        stream.write((byte) (i >> 16));
        stream.write((byte) (i >> 8));
        stream.write((byte) (i));
    }

    public static int readInt(InputStream stream) throws IOException {
        int a = stream.read() << 24;
        int b = stream.read() << 16;
        int c = stream.read() << 8;
        int d = stream.read();
        return a | b | c | d;
    }
}
