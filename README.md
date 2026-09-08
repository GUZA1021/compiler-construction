# Compiler Construction (DM546)

University coursework building compilers and interpreters for two small
imperative languages in Java. Three assignments cover the full path from raw
source text to executed program, using the visitor pattern over an AST
throughout.

The two back-ends contrast the standard approaches: assignment 2 compiles
ahead of time to LLVM IR, assignment 3 walks the tree and executes directly.

## Layout

    assignment1-scanner-parser/   scanner and parser for VVPL
    assignment2-llvm-codegen/     LLVM IR code generation for SPL
    assignment3-full-pipeline/    semantic analysis and interpreter for VVPL

Each is a self-contained Maven project.

## 1. Scanner and parser (VVPL)

### Scanner

Turns raw source into a list of tokens. Token data lives in `scan/Token.java`,
the token kinds in `scan/TokenType.java`.

The scanner switches on the first character of the current lexeme, sometimes
the first two for operators like `>=`, to handle punctuation and symbols.
Anything not matched there is a number, an identifier or a reserved keyword.
Keywords are separated from identifiers by a hashmap lookup, so `if` and
`while` are never mistaken for variable names.

### Parser

Takes the token list and produces a list of statements, checking that the
tokens make grammatical sense. This is syntactic grammar rather than the
lexical grammar the scanner deals with.

Since the set of valid programs is infinite, the grammar is expressed as a
finite set of productions in BNF: each rule has a head (a non-terminal) and a
body (terminals and non-terminals). The parser implements these rules as
recursive descent, a top-down parser that begins at the top of the grammar,
where the lowest-precedence rules sit, and descends toward `primary()`.

Structuring it that way enforces operator precedence directly, which matters
because an ambiguous grammar would let the same expression produce two
different trees. Parsing an expression descends to `primary()`, consumes the
first token, then works back up; if the next token is `+`, re-entering `term()`
recognises the addition and builds a `Binary` node. Statements are dispatched
from `stmt()`, which calls the appropriate rule and returns a statement node.

An `ASTPrinter` visitor renders the resulting tree for inspection.

Error reporting is not implemented at this stage; it arrives in assignment 3.

## 2. LLVM code generation (SPL)

A separate language, compiled ahead of time rather than interpreted. The
emitter performs a direct structural translation from AST nodes into
LLVM-style instructions, with no optimisation pass.

The design is built around SSA-style temporary registers. Every expression
allocates a fresh register (`%1`, `%2`, ...); a map tracks which register
currently holds each variable's value; control flow becomes generated labels
(`L1`, `L2`, ...) plus branch instructions for `if`, `else` and `while`.
Arithmetic and logical expressions translate one-to-one into the corresponding
instructions, each yielding a new SSA value. Output accumulates in a
`StringBuilder` and is returned by `getIR()`.

The generated IR is not run on the JVM; the JVM only runs the compiler itself.
LLVM IR was chosen because it is simple, low-level, close to the three-address
code real compilers work with, and retargetable, so a real LLVM back-end could
lower it to machine code for different architectures.

Optimisations the emitter deliberately does not perform:

- **Constant folding** - expressions with compile-time-known operands (`3 + 3`)
  could be evaluated immediately instead of emitting an `add`.
- **Dead code elimination** - the emitter never checks whether a register's
  value is used; unused instructions could be removed.
- **Common subexpression elimination** - repeated expressions with unchanged
  operands allocate a new register each time rather than reusing the previous.

Verified by running a collection of small `.spl` programs through `Spl.java`
and checking the emitted IR for expressions, assignment, control flow and
register allocation.

## 3. Semantic analysis and interpreter (VVPL)

The full pipeline for VVPL:

| Stage | Implementation |
|---|---|
| Scanning | `scan/Scanner.java` |
| Parsing | `parse/Parser.java` |
| Scope analysis | `ScopeAnalysis/ScopeAnalyser.java`, with a scoped symbol table |
| Type checking | `TypeAnalysis/TypeAnalyser.java` |
| Execution | `interpreter/Interpreter.java`, with a chained `Environment` |

Both analysis passes run before execution and report undeclared variables,
illegal shadowing, type mismatches and mismatched function signatures.
User-defined functions are supported, including recursion; the test suite
includes Fibonacci.

## Running

    cd assignment3-full-pipeline
    mvn test
    mvn compile

Tests pair `.in` input files with expected `.output` files under
`src/test/resources/`, covering scanner errors, parse errors, semantic errors
and interpreter output.

## Technologies

Java, Maven, JUnit, LLVM IR

## Team

* **Karim Amin**
* **Mohammad Amin**
* **Taaha Khan**

## Disclaimer

Developed for educational purposes as part of a university course. The AST node
definitions and test harnesses were provided by the course; the scanner,
parser, analysers, interpreter and code generator are ours.
