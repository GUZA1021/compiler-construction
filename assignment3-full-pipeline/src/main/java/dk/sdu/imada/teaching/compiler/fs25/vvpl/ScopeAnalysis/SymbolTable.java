package dk.sdu.imada.teaching.compiler.fs25.vvpl.ScopeAnalysis;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class SymbolTable {
    private Map<String, Token> symbols = new HashMap<>();
    public SymbolTable outer = null;

    public SymbolTable() {}

    public SymbolTable(SymbolTable outer){
        this.outer = outer;
    }

    public void put(String symbol, Token token) throws SymbolTableException{
        if (symbols.containsKey(symbol)) {
            throw new SymbolTableException("Symbol " + symbol + "already declared in this scope");
        }
         symbols.put(symbol, token);
    }

    // For the entire scope + outer scopes
    public boolean contains(String symbol) {
        if (symbols.containsKey(symbol)){
            return true;
        }
        
        if (outer != null){
            return outer.contains(symbol);
        }

        return false;
    }


    // For the current scope 
    public boolean containsCurrentScope(String symbol){
        return symbols.containsKey(symbol);
    }

    public class SymbolTableException extends Exception{
        public SymbolTableException(String message){
            super(message);
        }
    }
}
