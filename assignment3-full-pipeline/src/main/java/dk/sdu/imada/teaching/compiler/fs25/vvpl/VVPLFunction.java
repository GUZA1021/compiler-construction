package dk.sdu.imada.teaching.compiler.fs25.vvpl;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpreter.Environment;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpreter.Interpreter;

public class VVPLFunction implements VVPLCallable {

    private final Stmt.Function declaration;
    private final Environment closure;

    public VVPLFunction(Stmt.Function declaration, Environment closure){
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.parameterNames.size();
    }


    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        Environment environment = new Environment(closure);   

        for(int i = 0; i < declaration.parameterNames.size(); i++){
            environment.define(declaration.parameterNames.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Interpreter.Return r) {
            return r.value;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "<function " + declaration.name.lexeme + ">";
    }
}