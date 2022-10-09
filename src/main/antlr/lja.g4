grammar lja;

@header {
package ru.DmN.lj.asm;
}

/*
    PARSER
*/

/*
    FILE
*/

file
    : module* EOF
    ;

module
    : 'module' LITERAL endline extern* constant* variable* function*
    ;

/*
    EXTERN
*/

extern
    : 'extern' LITERAL (VAR|FUN) LITERAL (AS LITERAL)? endline
    ;

/*
    MODULE CONTENT
*/

constant
    : '#' NATURAL_NUMBER (NULL|HEX_NUMBER|FLOAT_NUMBER|DIGITAL_NUMBER|NATURAL_NUMBER|STRING|((LABEL|VAR|FUN) LITERAL)) endline
    ;

variable
    :  VAR LITERAL endline
    ;

/*
    FUNCTION
*/

function
    : FUN LITERAL ':' NATURAL_NUMBER endline (body endline)*
    ;

body
    : label
    | op0
    | op1
    | call
    ;

label
    : LITERAL ':'
    ;

/*
    OPERATIONS
*/

op0
    : 'nop'
    // nop - пустой опкод
    | 'pop'|'swap'|'dup'
    // pop - удаляет последний элемент стека
    // swap - меняет местами последние 2 элемента стека
    // dup - дублирует последний элемент стека
    | 'add'|'sub'|'mul'|'div'|'mod'
    // add - складывает 2 числа в стеке
    // sub - вычитает 2 числа в стеке
    // mul - умножает 2 числа в стеке
    // div - делит 2 числа в стеке
    // mod - берёт остаток до деления 2 чисел в стеке
    | 'cmpeq'|'cmpneq'|'cmpgt'|'cmpls'|'cmpn'|'cmpnn'
    // cmp(eq) - pop == pop
    // cmp(neq) - pop != pop
    // cmp(gt) - pop > pop
    // cmp(ls) - pop < pop
    // cmp(n) - pop == null
    // cmp(nn) - pop != null
    | 'ret'|'retv'|'rets'
    // ret - выход из функции
    // retv - выход из функции с возвратом значения
    // rets - выход из подпрограммы
    | 'convi'|'convd'
    // conv(i|d) - конвертация в (int|double)
    | 'ac'|'al'|'as'|'ag'
    // ac - создаёт массив
    // al - получает размер массива
    // as - устанавливает элемент в массиве
    // ag - получает элемент из массива
    | 'stjmp'|'stcall'
    // stjmp - переход к метке по указателю в стеке
    // stcall - вызов функции по указателю в стеке
    | 'debug'
    ;

op1
    : opcode=
    ('push'|'jmp'|'jmpif'|'jmps'|'setl'|'setg'|'getl'|'getg')
    // push - помещает константу в стек
    // jmp - безусловный переход к метке
    // jmpif - условный переход
    // jmps - безусловный переход к подпрограмме
    // set(l|g) - устанавливает значение в (lокальную|gлобальную) переменную
    // get(l|g) - получает значение (lокальной|gлобальной) переменной
    NATURAL_NUMBER
    ;

call
    : 'call' NATURAL_NUMBER NATURAL_NUMBER
    // call - вызов функции
    ;

/*
    SPECIAL
*/

endline
    : NEWLINE*
    ;

/*
    LEXER
*/

AS
    : 'as'
    ;

FUN
    : 'fun'
    ;

VAR
    : 'var'
    ;

LABEL
    : 'label'
    ;

LITERAL
    : ('`'|Letter) ('_'|Letter|Digit)*
    ;

STRING
    : '"' .*? '"'
    ;

HEX_NUMBER
    : '0x' ('a'..'z'|'A'..'Z')
    ;

FLOAT_NUMBER
    : (DIGITAL_NUMBER|NATURAL_NUMBER) '.' NATURAL_NUMBER
    ;

DIGITAL_NUMBER
    : '-' NATURAL_NUMBER
    ;

NATURAL_NUMBER
    : (Digit '_'?)+
    ;

NULL
    : 'null'
    ;

fragment Letter
    : 'a'..'z'|'A'..'Z'
    ;

fragment Digit
    : '0'..'9'
    ;

LINE_COMMENT
   : ';' ~ ('\n' | '\r')* '\r'? '\n' -> skip
   ;

NEWLINE
    : ('\r' '\n' | '\n' | '\r')+
    ;

WS
    : [ \t] -> skip
    ;