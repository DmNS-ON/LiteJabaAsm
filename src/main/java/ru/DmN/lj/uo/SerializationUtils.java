package ru.DmN.lj.uo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializationUtils {
    public static void serialize(OutputStream stream_, LJModule module) throws IOException {
        var stream = new DataOutputStream(stream_);
        //
        writeString(stream, module.name);
        //
        stream.writeInt(module.externals.size());
        for (var external : module.externals) {
            writeString(stream, external.module);
            //
            stream.writeInt(external.variables.size());
            for (var variable : external.variables.entrySet()) {
                writeString(stream, variable.getKey());
                writeString(stream, variable.getValue());
            }
            //
            stream.writeInt(external.functions.size());
            for (var function : external.functions.entrySet()) {
                writeString(stream, function.getKey());
                writeString(stream, function.getValue());
            }
        }
        //
        stream.writeInt(module.constants.size());
        for (var constant : module.constants) {
            stream.writeInt(constant.i);
            stream.writeInt(constant.type.id);
            switch (constant.type) {
                case INT -> stream.writeInt((Integer) constant.value);
                case FLOAT -> stream.writeDouble((Double) constant.value);
                case STRING, REF_LABEL, REF_VAR, REF_FUN -> writeString(stream, (String) constant.value);
            }
        }
        //
        stream.writeInt(module.variables.size());
        for (var variable : module.variables) {
            writeString(stream, variable.name);
        }
        //
        stream.writeInt(module.functions.size());
        for (var function : module.functions) {
            writeString(stream, function.name);
            stream.writeInt(function.vcount);
            stream.writeInt(function.labels.size());
            for (var label : function.labels.entrySet()) {
                writeString(stream, label.getKey());
                stream.writeInt(label.getValue());
            }
            stream.writeInt(function.opcodes.size());
            for (var opcode : function.opcodes) {
                stream.writeInt(opcode.op);
                stream.writeInt(opcode.additional.length);
                for (var i : opcode.additional) {
                    stream.writeInt(i);
                }
            }
        }
    }

    public static LJModule deserialize(InputStream stream_) throws IOException {
        var stream = new DataInputStream(stream_);
        //
        var module = new LJModule(readString(stream), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        //
        {
            var i = stream.readInt();
            while (i-- > 0) {
                var external = new LJExternal(readString(stream), new HashMap<>(), new HashMap<>());
                //
                var j = stream.readInt();
                while (j-- > 0) {
                    external.variables.put(readString(stream), readString(stream));
                }
                //
                var k = stream.readInt();
                while (k-- > 0) {
                    external.functions.put(readString(stream), readString(stream));
                }
                //
                module.externals.add(external);
            }
        }
        //
        {
            var i = stream.readInt();
            while (i-- > 0) {
                var id = stream.readInt();
                var type = LJConstant.Type.of(stream.readInt());
                var value = switch (type) {
                    case INT -> stream.readInt();
                    case FLOAT -> stream.readDouble();
                    case STRING, REF_LABEL, REF_VAR, REF_FUN -> readString(stream);
                };
                module.constants.add(new LJConstant(id, type, value));
            }
        }
        //
        {
            var i = stream.readInt();
            while (i-- > 0) {
                module.variables.add(new LJVariable(readString(stream)));
            }
        }
        //
        {
            var i = stream.readInt();
            while (i-- > 0) {
                var name = readString(stream);
                var vcount = stream.readInt();
                var labels = new HashMap<String, Integer>();
                var j = stream.readInt();
                while (j-- > 0) {
                    labels.put(readString(stream), stream.readInt());
                }
                var opcodes = new ArrayList<LJOpcode>();
                var k = stream.readInt();
                while (k-- > 0) {
                    var op = stream.readInt();
                    var l = stream.readInt();
                    var additional = new int[l];
                    for (int f = 0; f < l; f++) {
                        additional[f] = stream.readInt();
                    }
                    opcodes.add(new LJOpcode(op, additional));
                }
                module.functions.add(new LJFunction(module, name, vcount, labels, opcodes));
            }
        }
        //
        return module;
    }

    public static void writeString(DataOutputStream stream, String str) throws IOException {
        if (str == null) {
            stream.writeInt(1024);
        } else {
            stream.writeInt(2048);
            for (var c : str.toCharArray())
                stream.write(c);
            stream.write('\0');
        }
    }

    public static String readString(DataInputStream stream) throws IOException {
        if (stream.readInt() == 1024)
            return null;
        var str = new StringBuilder();
        var c = (char) stream.read();
        str.append(c);
        while (c != '\0')
            str.append(c = (char) stream.read());
        return str.toString();
    }
}
