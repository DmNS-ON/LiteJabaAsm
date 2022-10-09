package ru.DmN.lj.asm.debugger;

import ru.DmN.lj.asm.compiler.Compiler;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class ConsoleDebugger implements Runnable {
    public static final List<String> COMMANDS0 = List.of("exit - завершает работу отладчика", "help - выводит список комманд", "reset - сбрасывает отладчик", "load <filename> - загружает файл", "run <modulename> - запускает программу");
    public PrintStream out;
    public Scanner in;
    public Debugger debugger;

    public ConsoleDebugger(PrintStream out, Scanner in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {
        this.resetDebugger();
        this.printCommands(COMMANDS0);
        this.out.println();
        while (!Thread.interrupted()) {
            this.out.print("> ");
            var cmd = in.nextLine().split(" ");
            switch (cmd[0]) {
                case "load" -> {
                    if (cmd.length == 1) {
                        this.printInvalidCommandUsageError("load");
                    } else {
                        var file = cmd[1];
                        try (var stream = new FileInputStream(file)) {
                            var compiler = new Compiler();
                            compiler.compile(new String(stream.readAllBytes()));
                            this.out.print("Из файла `" + file + "` ");
                            if (compiler.modules.size() == 0) {
                                this.out.println("не было загружено ни одного модуля!");
                            } else {
                                this.out.print("были загружены: [");
                                var modules = compiler.modules;
                                var dmodules = this.debugger.modules;
                                for (int i = 0; i < modules.size(); i++) {
                                    var module = modules.get(i);
                                    dmodules.add(new LoadedModule(module));
                                    this.out.print(module.name);
                                    if (i + 1 < modules.size()) {
                                        this.out.print(' ');
                                    }
                                }
                                this.out.println("]");
                            }
                        } catch (FileNotFoundException e) {
                            this.out.println("Ошибка! Файл `" + file + "` не найден!");
                        } catch (OutOfMemoryError e) {
                            this.out.println("Ошибка! Файл `" + file + "`слишком большой!");
                        } catch (EOFException e) {
                            this.out.println("Ошибка! Файл `" + file + "` повреждён!");
                        } catch (IOException e) {
                            this.out.println("Ошибка! [Exception]" + e.getLocalizedMessage());
                        }
                    }
                }
                case "run" -> {
                    if (cmd.length == 1) {
                        this.printInvalidCommandUsageError("run");
                    } else {
                        try {
                            debugger.eval(cmd[1]);
                        } catch (StdModule.MainComplete complete) {
                            this.out.println("Программа завершилась с кодом " + complete.exitCode +"!");
                        } catch (RuntimeException e) {
                            this.out.println("Ошибка! " + e.getLocalizedMessage());
                        }
                    }
                }
                case "reset" -> this.resetDebugger();
                case "help" -> this.printCommands(COMMANDS0);
                case "exit" -> Thread.currentThread().interrupt();
                default -> this.printUnknownCommandError(cmd[0], COMMANDS0);
            }
            this.out.println();
        }
    }

    public void printInvalidCommandUsageError(String cmd) {
        this.out.println("Ошибка! Неправильное использование комманды `" + cmd + "`!");
        COMMANDS0.stream().filter(e -> e.startsWith(cmd + " ")).findFirst().ifPresent(e -> this.out.println("[|" + e));
    }

    public void printUnknownCommandError(String cmd, List<String> commands) {
        this.out.println("Ошибка! Неизвестная комманда `" + cmd + "`!");
        this.printCommands(commands);
    }

    public void printCommands(List<String> commands) {
        this.out.println("[Список комманд:");
        commands.forEach(cmd -> this.out.println("| " + cmd));
        this.out.println("]");
    }

    public void resetDebugger() {
        this.debugger = new Debugger();
        this.debugger.debugStream = this.out;
        this.debugger.modules.add(new LoadedModule(new StdModule(this.out)));
        this.out.println("Отладчик сброшен!");
    }

    public static void main(String[] args) {
        new ConsoleDebugger(System.out, new Scanner(System.in)).run();
    }
}
