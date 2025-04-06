# Genetic Programming Language - Custom Grammar

This project implements a custom genetic programming system that evolves executable programs based on a user-defined language grammar and fitness evaluation functions.

## 📜 Grammar Definition (`.g4` format)

The grammar below defines the syntax of a simple, custom programming language used in the genetic programming process.

```antlr
program : statement* EOF;

// Statements
statement
    : block                          # blockStmt
    | ifStatement                    # ifStmt
    | loopStatement                  # loopStmt
    | outputStatement                # outputStmt
    | assignmentStatement            # assignStmt
    | expr                           # exprStmt
    ;

// Program block
block : '(' statement* ')' ;

// Conditional instruction
ifStatement : 'if' '(' expr ')' block ;

// Loop
loopStatement : 'while' '(' expr ')' block ;

// Input/Output operations
inputExpression : 'input' NUMBER ;
outputStatement : 'output' expr ;

// Assignment
assignmentStatement : 'assign' IDENTIFIER expr ;

// Expressions
expr
    : op=('+' | '−') expr expr                       # arithmeticExpr
    | op=('*' | '/') expr expr                       # arithmeticExpr
    | op=('<' | '>' | '<=' | '>=' | '==' | '!=') expr expr # comparisonExpr
    | IDENTIFIER                                     # variableExpr
    | NUMBER                                         # numberExpr
    | '(' expr ')'                                   # parenExpr
    | inputExpression                                # inputExpr
    ;

// Tokens
NUMBER: [0-9]+ ;
IDENTIFIER: [a-zA-Z] [a-zA-Z0-9]* ;

// Whitespace
WS: [ \t\r\n]+ -> skip ;
```
## 🧠 Project Overview
This grammar is used as the foundation for a genetic programming engine. Programs are generated based on this syntax and evaluated using a fitness function to solve specific computational problems.

## 🏗️ Interpreter Design
The interpreter represents programs as abstract syntax trees (AST). Each individual program is executed directly from its tree representation.

Interpreter Features:
 * Infinite loop protection: Loops are interrupted if a code fragment is executed more than MAX_ITERATIONS times. A penalty is then applied to the fitness value.

 * Undeclared variable protection: Access to undefined variables is prevented.

 * Division by zero protection: Division operations are checked to avoid runtime errors.

## 🔁 Crossover Operator
The crossover operation works as follows:

1. Two random programs are selected using tournament selection.

2. A random node is selected from the first program.

3. A compatible node (same type) is selected from the second program to ensure semantic correctness (e.g., avoiding swapping a numeric value with a conditional statement).

4. The selected nodes are swapped between the trees.

5. The fitness of both new offspring programs is evaluated.

6. One of the original programs is chosen using negative tournament selection.

## 🧬 Selection Methods
1. Tournament Selection
 * A specified number of candidates ("players") are selected from the population.

 * The one with the lowest fitness value is selected as the winner.

2. Negative Tournament Selection
 * Works similarly to regular tournament selection.

 * However, the program with the highest fitness value (i.e., worst performer) is selected.

