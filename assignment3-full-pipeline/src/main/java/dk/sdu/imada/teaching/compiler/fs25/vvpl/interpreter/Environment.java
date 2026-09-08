package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpreter;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpreter.Interpreter.RuntimeError;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment outer;

    public Environment(){
        this.outer = null;
    }

    public Environment(Environment outer){
        this.outer = outer;
    }
    

    public void define(String name, Object value){
        values.put(name, value);
    }
    
    public Object get(Token name){
        if (values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        } else if (outer != null){
            return outer.get(name);
        } else{
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }


    public void assign(Token name, Object value){
        if (values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        } else if (outer != null){
            outer.assign(name, value);
            return;
        } else{
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }

}
