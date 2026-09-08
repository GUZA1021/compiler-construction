package dk.sdu.imada.teaching.compiler.fs25.vvpl.TypeAnalysis;

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

public class GetFunctions implements StmtVisitor<Void> {

    private final Map<String, FunctionInfo> functions = new HashMap<>();
    private final List<String> funcErrors = new LinkedList<>();


    public Map<String, FunctionInfo> getFunctions(){
        return functions;
    }

    public List<String> getErrors(){
        return funcErrors;
    }

    @Override
    public Void visitFunction(Function stmt) {

        String name = stmt.name.lexeme;

        if (functions.containsKey(name)){
            funcError(stmt.name, "Function already declared");
            return null;
        }

        Type returnType = Type.NOTHING;

        if (stmt.returnType != null){
            returnType = tokenToType(stmt.returnType);
        }

        functions.put(name, new FunctionInfo(stmt.parameterNames, stmt.parameterTypes, returnType, stmt.body));
        
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


    private void funcError(Token token, String message){
        funcErrors.add(ErrorTypeStrings.FUNC_ERROR + ", line " + token.line + " " + message);
    }
    
   private Type tokenToType(Token token){
        return switch (token.lexeme){
            case "Number" -> Type.NUMBER;
            case "Bool" -> Type.BOOL;
            case "String" -> Type.STRING;
            case "Nothing" -> Type.NOTHING;
            default -> Type.UNKNOWN;
        };
    }
}
