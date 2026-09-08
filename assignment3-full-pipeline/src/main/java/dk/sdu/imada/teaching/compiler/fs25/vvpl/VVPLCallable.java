package dk.sdu.imada.teaching.compiler.fs25.vvpl;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpreter.Interpreter;

public interface VVPLCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
