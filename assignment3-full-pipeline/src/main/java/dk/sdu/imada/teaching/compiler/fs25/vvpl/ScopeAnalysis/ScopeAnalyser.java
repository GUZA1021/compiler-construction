package dk.sdu.imada.teaching.compiler.fs25.vvpl.ScopeAnalysis;

import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Assign;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Binary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Call;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Cast;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Grouping;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Literal;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Logical;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Unary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Variable;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.BlockStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ExprStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.Function;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.IfStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.PrintStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ReturnStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.Var;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.WhileStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ExprVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.StmtVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class ScopeAnalyser implements ExprVisitor<Void>, StmtVisitor<Void> {

    private SymbolTable currentEnviornment = new SymbolTable();
    private final List<String> scopeErrors = new LinkedList<>();
    private final List<Stmt> program;

    private boolean inFunctionScope = false;
    private boolean insideBlock = false;

    public ScopeAnalyser(List<Stmt> program){
        this.program = program;
    }

    public List<String> analyse(){

        GetFunctionNames getFunctionNames = new GetFunctionNames();

        for (Stmt stmt : program){
            stmt.accept(getFunctionNames);
        }

        for(Token token : getFunctionNames.getFunctions().values()){
            String name = token.lexeme;
            try {
                currentEnviornment.put(name, token);
            } catch (SymbolTable.SymbolTableException e) {
                scopeError(token, "Function already declared");
            }
        }  

        scopeErrors.addAll(getFunctionNames.getErrors());
        
        
        for (Stmt stmt : program){
            stmt.accept(this);
        }
        return scopeErrors;
    }

    private void analyse(Stmt stmt){
        stmt.accept(this);
    }

    private void analyse(Expr expr){
        expr.accept(this);
    }



    @Override
    public Void visitBlock(BlockStmt stmt) {

        insideBlock = true;

        SymbolTable oldTable = currentEnviornment;
        currentEnviornment = new SymbolTable(currentEnviornment);

        for (Stmt s : stmt.statements) {
            analyse(s);
        }

        currentEnviornment = oldTable;
        insideBlock = false;
        return null;
    }

    @Override
    public Void visitExpr(ExprStmt stmt) {
        analyse(stmt.expr);
        return null;
    }

    @Override
    public Void visitPrint(PrintStmt stmt) {
        analyse(stmt.expression);
        return null;
    }

    @Override
    public Void visitIf(IfStmt stmt) {
        analyse(stmt.condition);
        analyse(stmt.thenBranch);
        
        if (stmt.elseBranch != null) {
            analyse(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitReturn(ReturnStmt stmt) {
        if (!inFunctionScope){
            scopeError(stmt.keyword, "Return statement outside function");
        }
        if (stmt.value != null){
            analyse(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVar(Var stmt) {

        String name = stmt.name.lexeme;

        if (currentEnviornment.containsCurrentScope(name)){
            scopeError(stmt.name, "Variable " + name + " already declared");
        } else if (isShadowing(name)){
            scopeError(stmt.name, "Shadowing");
        } else{
            try {
                currentEnviornment.put(name, stmt.name);
            } catch (SymbolTable.SymbolTableException e) {
                scopeError(stmt.name, "Variable " + name + " already declared in this scope");
            }
        }

        if (stmt.initializer != null) {
            analyse(stmt.initializer);
        }

        return null;
    }

    @Override
    public Void visitWhile(WhileStmt stmt) {
        analyse(stmt.condition);
        analyse(stmt.body);
        return null;
    }

    @Override
    public Void visitFunction(Function stmt) {

        if (insideBlock){
            scopeError(stmt.name, "Function declaration only in global scope");
        }
        SymbolTable oldTable = currentEnviornment;

        currentEnviornment = new SymbolTable(oldTable);
        inFunctionScope = true;

        for (int i = 0; i < stmt.parameterNames.size(); i++){
            Token parameterName = stmt.parameterNames.get(i);

            if (currentEnviornment.containsCurrentScope(parameterName.lexeme)){
                scopeError(parameterName, "Parameter already declare din this function scope");
            } else {
                try {
                    currentEnviornment.put(parameterName.lexeme, parameterName);
                } catch (SymbolTable.SymbolTableException e) {
                    scopeError(parameterName, "Parameter " + parameterName.lexeme + " already decalred in this function scope");
                }
            }
        }

        for (Stmt s : stmt.body){
            analyse(s);
        }

        //Restore

        currentEnviornment = oldTable;
        inFunctionScope = false;

        return null;
    }

    @Override
    public Void visitAssign(Assign expr) {
        if (!currentEnviornment.contains(expr.name.lexeme)){
            scopeError(expr.name, "Assigned to undefined variable " + expr.name.lexeme);
        }

        analyse(expr.value);
        return null;
    }

    @Override
    public Void visitLogical(Logical expr) {
        analyse(expr.left);
        analyse(expr.right);
        return null;
    }

    @Override
    public Void visitBinary(Binary expr) {
        analyse(expr.left);
        analyse(expr.right);
        return null;
    }

    @Override
    public Void visitUnary(Unary expr) {
        analyse(expr.right);
        return null;
    }

    @Override
    public Void visitLiteral(Literal expr) {
        return null;
    }

    @Override
    public Void visitVariable(Variable expr) {
        if (!currentEnviornment.contains(expr.name.lexeme)){
            scopeError(expr.name, "Undefined variable " + expr.name.lexeme);
        }

        return null;
    }


    @Override
    public Void visitGrouping(Grouping expr) {
        analyse(expr.expr);
        return null;
    }

    @Override
    public Void visitCast(Cast expr) {
        analyse(expr.expr);
        return null;
    }

    @Override
    public Void visitCall(Call expr) {
        analyse(expr.callee);

        for (Expr argument : expr.arguments){
            analyse(argument);
        }

        return null;
    }


    // Helper

    private boolean isShadowing(String name){
        if (currentEnviornment.outer == null){
            return false;
        } else if (!currentEnviornment.outer.contains(name)){
            return false;
        } else if (!inFunctionScope) {
            return true;
        }
        return currentEnviornment.outer.outer != null;
    }

    private void scopeError(Token token, String message){
        scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + token.line + " " + message);
    }
    
}
