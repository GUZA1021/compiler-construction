package dk.sdu.imada.teaching.compiler.fs25.vvpl.TypeAnalysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class TypeAnalyser implements ExprVisitor<Type>, StmtVisitor<Void> {

    private SymbolTable currentEnviornment = new SymbolTable();
    private final Map<String, FunctionInfo> functions = new HashMap<>();

    private Type currentFuncReturnType = Type.UNKNOWN;

    private final List<Stmt> program;
    private final List<String> typeErrors = new LinkedList<>();

    
    public TypeAnalyser(List<Stmt> program){
        this.program = program;
    }

    public List<String> analyse(){

        // Preload al function (Global function scope)
        GetFunctions getFunctions = new GetFunctions();

        for (Stmt stmt : program){
            stmt.accept(getFunctions);
        }

        functions.putAll(getFunctions.getFunctions());
        typeErrors.addAll(getFunctions.getErrors());

        // Normal type checking
        for (Stmt stmt : program){
            stmt.accept(this);
        }

        return typeErrors;
    }

    private void analyse(Stmt stmt){
        stmt.accept(this);
    }

    private Type analyse(Expr expr){
        return expr.accept(this);
    }






    @Override
    public Void visitBlock(BlockStmt stmt) {
        SymbolTable oldTable = currentEnviornment;
        currentEnviornment = new SymbolTable(currentEnviornment);

        for (Stmt s : stmt.statements) {
            analyse(s);
        }

        currentEnviornment = oldTable;

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
        Type conditionType = analyse(stmt.condition);

        if (conditionType != Type.BOOL){
            typeError(stmt.token, "If condition should be Bool, and not " + conditionType);
        }

        analyse(stmt.thenBranch);

        if (stmt.elseBranch != null) {
            analyse(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitReturn(ReturnStmt stmt) {
        Type exprType;

        if (stmt.value == null){
            exprType = Type.NOTHING;
        } else {
            exprType = stmt.value.accept(this);
        }

        if (currentFuncReturnType == Type.UNKNOWN){
            currentFuncReturnType = exprType;
        } else if (currentFuncReturnType != exprType){
            typeError(stmt.keyword, "Execpted " + currentFuncReturnType + " but got " + exprType);
        }

        return null;
    }

    @Override
    public Void visitVar(Var stmt) {
        Type varType = tokenToType(stmt.type);

        try {
            currentEnviornment.creation(stmt.name.lexeme, varType);
        } catch (SymbolTable.SymbolTableException e) {
            return null; 
        }

        if (stmt.initializer == null){
            typeError(stmt.name, "Variable must be initialized");
        } 

        Type initType = analyse(stmt.initializer);

        if (initType != varType  && initType != Type.UNKNOWN){
            typeError(stmt.name, "Cannot assign " + initType + "to " + varType);
        }

        return null;
    }

    @Override
    public Void visitWhile(WhileStmt stmt) {
        Type conditionType = analyse(stmt.condition);
        if (conditionType != Type.BOOL){
            typeError(stmt.token, "While condition should be Bool, and not " + conditionType);
        }

        analyse(stmt.body);
        return null;
    }

    @Override
    public Void visitFunction(Function stmt) {


        FunctionInfo functionInfo = functions.get(stmt.name.lexeme);
        if (functionInfo == null){
            funcError(stmt.name, "Function does not exist");
            return null;
        }

        SymbolTable oldTable = currentEnviornment;
        currentEnviornment = new SymbolTable(oldTable) ;

        for (int i = 0; i < stmt.parameterNames.size(); i++){
            Token parameterName = stmt.parameterNames.get(i);
            Token parameterType = stmt.parameterTypes.get(i);
            Type typeParameterType = tokenToType(parameterType);
            
            try {
                currentEnviornment.creation(parameterName.lexeme, typeParameterType);
            } catch (SymbolTable.SymbolTableException e) {
                typeError(parameterName, "Parameter " + parameterName.lexeme + " already decalred in scope");
            }
        }

        Type oldReturnType = currentFuncReturnType;
        currentFuncReturnType = functionInfo.returnType;

        for (Stmt s : stmt.body){
            analyse(s);
        }

        currentFuncReturnType = oldReturnType;
        currentEnviornment = oldTable;

        return null;
    }

    @Override
    public Type visitAssign(Assign expr) {
        Type exprType = analyse(expr.value);
        Type varType; 

        try {
            varType = currentEnviornment.get(expr.name.lexeme);
        } catch (SymbolTable.SymbolTableException e) {
            return Type.UNKNOWN;
        }

        if (varType == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }

        if (varType != exprType && exprType != Type.UNKNOWN){
            typeError(expr.name, "Expected " + varType + " but got " + exprType);
        }

        return varType;
    }

    @Override
    public Type visitLogical(Logical expr) {
        Type left = analyse(expr.left);
        Type right = analyse(expr.right);

        if (left != Type.BOOL || right != Type.BOOL){
            typeError(expr.operator, "Logical operator needs Bool operator");
            return Type.UNKNOWN;
        }

        return Type.BOOL;
    }

    @Override
    public Type visitBinary(Binary expr) {
        Type left = analyse(expr.left);
        Type right = analyse(expr.right);

        if (left == Type.UNKNOWN || right == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }

        switch (expr.operator.type) {
            case PLUS:
                if (left == Type.NUMBER && right == Type.NUMBER){
                    return Type.NUMBER;
                }

                if (left == Type.STRING && right == Type.STRING){
                    return Type.STRING;
                }
                typeError(expr.operator, "Require two Numbers or two Strings");

                return Type.UNKNOWN;
        
            case MINUS:
                if (left == Type.NUMBER && right == Type.NUMBER){
                    return Type.NUMBER;
                } else {
                    typeError(expr.operator, "null");
                    return Type.UNKNOWN;
                }
            case LESS:
            case LESS_EQUAL:
            case GREATER:
            case GREATER_EQUAL:
                if (left == Type.NUMBER && right == Type.NUMBER){
                    return Type.BOOL;
                } else {
                    typeError(expr.operator, "Need numbers to compare");
                    return Type.BOOL;
                }
            case EQUALS:
            case NOT_EQUALS:
                if (left != right){
                    typeError(expr.operator, "Cant compare " + left + "with " + right);
                }
                return Type.BOOL;
            
            default:
                return Type.UNKNOWN;
        }
    }

    @Override
    public Type visitUnary(Unary expr) {
        Type operandType = analyse(expr.right);

        switch (expr.operator.type) {
            case NOT:
                if (operandType != Type.BOOL){
                    typeError(expr.operator, "Operator requires Bool");
                    return Type.UNKNOWN;
                }
                return Type.BOOL;
            case MINUS:
                if (operandType != Type.NUMBER){
                    typeError(expr.operator, "Operator requires Number");
                    return Type.UNKNOWN;
                }
                return Type.NUMBER;
            default:
                return Type.UNKNOWN;
        }
    }

    @Override
    public Type visitLiteral(Literal expr) {
        return switch (expr.value) {
            case Double d -> Type.NUMBER;
            case Boolean b -> Type.BOOL;
            case String s -> Type.STRING;
            case null -> Type.NOTHING;
            default -> Type.UNKNOWN;
        };
    }

    @Override
    public Type visitVariable(Variable expr) {
        try {
            return currentEnviornment.get(expr.name.lexeme);
        } catch (SymbolTable.SymbolTableException e) {
            return Type.UNKNOWN;
        }
    }
    

    @Override
    public Type visitGrouping(Grouping expr) {
        return analyse(expr.expr);
    }

    @Override
    public Type visitCast(Cast expr) {
        analyse(expr.expr);
        return tokenToType(expr.type);
    }


    @Override
    public Type visitCall(Call expr) {
        if (!(expr.callee instanceof Variable)){
            return Type.UNKNOWN;
        }

        Variable var = (Variable) expr.callee;
        FunctionInfo function = functions.get(var.name.lexeme);

        if (function == null){
            typeError(var.name, "Undefined function");
            return Type.UNKNOWN;
        }

        if (expr.arguments.size() != function.parameterTypes.size()){
            typeError(var.name, "Expected " + function.parameterTypes.size() + "argument but got " + expr.arguments.size() );
            return function.returnType;
        }

        for (int i = 0; i < expr.arguments.size(); i++){
            Type argumenType = analyse(expr.arguments.get(i));
            Type parameterType = tokenToType(function.parameterTypes.get(i));

            if(argumenType != parameterType && argumenType !=Type.UNKNOWN){
                typeError(var.name, "Arguemnt type mismatch");
            }
        }

        return function.returnType;
        
    }

    // Helper

    private Type tokenToType(Token token){
        return switch (token.lexeme){
            case "Number" -> Type.NUMBER;
            case "Bool" -> Type.BOOL;
            case "String" -> Type.STRING;
            case "Nothing" -> Type.NOTHING;
            default -> Type.UNKNOWN;
        };
    }

    private void typeError(Token token, String message){
        typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + token.line + " " + message);
    }

    private void funcError(Token token, String message){
        typeErrors.add(ErrorTypeStrings.FUNC_ERROR + ", line " + token.line + " " + message);
    }
    
}
