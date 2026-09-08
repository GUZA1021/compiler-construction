package dk.sdu.imada.teaching.compiler.fs25.vvpl.ScopeAnalysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.BlockStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ExprStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.Function;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.IfStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.PrintStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ReturnStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.Var;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.WhileStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.StmtVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class GetFunctionNames implements StmtVisitor<Void> {
    

    private final Map<String, Token> functions = new HashMap<>();
    private final List<String> scopeErrors = new LinkedList<>();


    public Map<String, Token> getFunctions(){
        return functions;
    }

    public List<String> getErrors(){
        return scopeErrors;
    }

    @Override
    public Void visitFunction(Function stmt) {
        String name = stmt.name.lexeme;

        if (functions.containsKey(name)){
            scopeError(stmt.name, "Function already declared");
            return null;
        }

        functions.put(name, stmt.name);        
        return null;
    }


    @Override
    public Void visitBlock(BlockStmt stmt) {

        return null;
    }

    @Override
    public Void visitExpr(ExprStmt stmt) {
        return null;
    }

    @Override
    public Void visitPrint(PrintStmt stmt) {
        return null;
    }

    @Override
    public Void visitIf(IfStmt stmt) {
        return null;
    }

    @Override
    public Void visitReturn(ReturnStmt stmt) {
        return null;
    }

    @Override
    public Void visitVar(Var stmt) {
        return null;
    }

    @Override
    public Void visitWhile(WhileStmt stmt) {
        return null;
    }


    //Helper


    private void scopeError(Token token, String message){
        scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + token.line + " " + message);
    }



}
