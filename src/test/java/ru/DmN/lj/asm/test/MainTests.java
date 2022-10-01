package ru.DmN.lj.asm.test;

import org.junit.jupiter.api.Test;
import ru.DmN.lj.asm.Compiler;
import ru.DmN.lj.asm.Debugger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class MainTests {
    @Test
    public void main() throws IOException {
        try (var stream = MainTests.class.getClassLoader().getResourceAsStream("all.lja")) {
            var compiler = new Compiler();
            compiler.compile(new String(stream.readAllBytes()));
            var debugger = new Debugger();
            debugger.modules.add(new Debugger.LoadedModule(new StdModule()));
            debugger.debug = (thread, ctx) -> System.out.println("DEBUG!");
            debugger.printDebugTrace = true;
            debugger.debugStream = new PrintStream(new FileOutputStream("debug.log"));
            System.out.println(debugger.eval(compiler, "TM"));
            debugger.debugStream.close();
        }
    }
}
