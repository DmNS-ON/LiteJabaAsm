package ru.DmN.lj.asm.test;

import org.junit.jupiter.api.Test;
import ru.DmN.lj.asm.compiler.Compiler;
import ru.DmN.lj.asm.debugger.Debugger;
import ru.DmN.lj.asm.debugger.LoadedModule;
import ru.DmN.lj.uo.SerializationUtils;

import java.io.*;

public class MainTests {
    @Test
    public void main() throws IOException {
        try (var stream = MainTests.class.getClassLoader().getResourceAsStream("all.lja")) {
            var compiler = new Compiler();
            compiler.compile(new String(stream.readAllBytes()));
            //
            new File("run").mkdir();
            //
            try (var s = new FileOutputStream("run/all.ljuo")) {
                SerializationUtils.serialize(s, compiler.modules.get(0));
            }
            try (var s = new FileInputStream("run/all.ljuo")) {
                var module = SerializationUtils.deserialize(s);
                System.out.println(module); // for check - set breakpoint
            }
            //
            var debugger = new Debugger();
            debugger.modules.add(new LoadedModule(new StdModule()));
            debugger.debug = (thread, ctx) -> System.out.println("DEBUG!");
            debugger.printDebugTrace = true;
            debugger.debugStream = new PrintStream(new FileOutputStream("run/debug.log"));
            System.out.println(debugger.eval(compiler, "TM"));
            debugger.debugStream.close();
            //
        }
    }
}
