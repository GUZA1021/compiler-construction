package dk.sdu.imada.teaching.compiler.fs25.vvpl.TypeAnalysis;

import java.util.HashMap;
import java.util.Map;


public class SymbolTable {
    private Map<String, Type> symbols = new HashMap<>();
    public SymbolTable outer = null;

    public SymbolTable() {}

    public SymbolTable(SymbolTable outer){
        this.outer = outer;
    }

    public void creation(String name, Type type) throws SymbolTableException{
        if (symbols.containsKey(name)){
            throw new SymbolTableException("Symbol " + name + " already decalred in this scope");
        }
        symbols.put(name, type);
    }




    public Type get(String name) throws SymbolTableException{
        if (symbols.containsKey(name)){
            return symbols.get(name);
        } else if (outer != null){
            return outer.get(name);
        } else {
            throw new SymbolTableException(name);
        }
    }

    public boolean contains(String symbol) {
        if (symbols.containsKey(symbol)){
            return true;
        }
        
        if (outer != null){
            return outer.contains(symbol);
        }

        return false;
    }


    public class SymbolTableException extends Exception{
        public SymbolTableException(String message){
            super(message);
        }
    }
}
