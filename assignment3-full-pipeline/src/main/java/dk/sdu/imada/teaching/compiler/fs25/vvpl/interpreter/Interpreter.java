package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpreter;

import java.util.ArrayList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLCallable;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLFunction;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Assign;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Binary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Call;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Cast;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Grouping;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Literal;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Logical;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Unary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Variable;
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
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void>{

    private Environment environment = new Environment();
    private final List<String> output;
    
    public void interpret(List<Stmt> statments){
        try{
            // Define all global functions first
            for (Stmt statement : statments) {
                if (statement instanceof Stmt.Function){
                    statement.accept(this);
                }
            }
            // Execute everything else
            for (Stmt statement : statments){
                if (!(statement instanceof Stmt.Function)){
                statement.accept(this);
                }
            } 
        }catch (RuntimeError error){
            throw error;
        }
    }

    public Interpreter(List<String> output){
        this.output = output;
    }

    public void execute(Stmt stmt){
        stmt.accept(this);
    }

    @Override
    public Void visitBlock(BlockStmt stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    // Temprorarily switched to new envionrment to execute statements, then return to previous
    public void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;
        try{
            this.environment = environment;
            for (Stmt stmt : statements){
                stmt.accept(this);
            }
        } finally{
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpr(ExprStmt stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitPrint(PrintStmt stmt) {
        Object value = evaluate(stmt.expression);
        output.add(stringify(value));
        return null;
    }

    @Override
    public Void visitIf(IfStmt stmt) {
        if (isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitReturn(ReturnStmt stmt) {
        Object value = null;

        if (stmt.value != null){
            value = evaluate(stmt.value);
        }
        throw new Return(value);
    }

    public static class Return extends RuntimeException{
        public final Object value;

        Return(Object value){
            super(null, null, false, false);
            this.value = value;
        }
    }

    @Override
    public Void visitVar(Var stmt) {
        Object value = null;

        if(stmt.initializer != null){
            value = evaluate((stmt.initializer));
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhile(WhileStmt stmt) {
        while (isTruthy(evaluate(stmt.condition))){
            execute(stmt.body);
        }
        return null;
    }


    // Expr
    

    @Override
    public Object visitAssign(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitLogical(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR){
            if (isTruthy(left)){
                return left;
            }
        } else {
            if (!isTruthy(left)){
                return left;
            }
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitBinary(Binary expr) {
        Object left = expr.left.accept(this);
        Object right = expr.right.accept(this);

        switch (expr.operator.type) {
            case SUB:
                checkNumberOperands(expr.operator, left, right);
                return ((Double)left) - ((Double)right);
            case DIV:
                checkNumberOperands(expr.operator, left, right);
                return ((Double)left) / ((Double)right);
            case MULTIPLY:
                checkNumberOperands(expr.operator, left, right);
                return ((Double)left) * ((Double)right);
            case PLUS:
                if (left instanceof Double && right instanceof Double){
                    return ((Double)left) + ((Double)right);
                }
                
                if (left instanceof String && right instanceof String){
                    return (String)left + (String)right;
                }

                break;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return ((Double)left) > ((Double)right);
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return ((Double)left) >= ((Double)right);
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return ((Double)left) < ((Double)right);
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return ((Double)left) <= ((Double)right);
            case NOT_EQUALS:
                return !isEqual(left, right);
            case EQUALS:
                return isEqual(left, right);
                
            default:
                break;
        }

        return null;
    }



    @Override
    public Object visitUnary(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return (-(Double) right);
            case NOT:
                return !isTruthy(right);
            default:
                throw new RuntimeError(expr.operator, "Not unary operator");
        }
    }

    @Override
    public Object visitLiteral(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitVariable(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitGrouping(Grouping expr) {
        return evaluate(expr.expr);
    }

@Override
public Object visitCast(Cast expr) {
    Object value = evaluate(expr.expr);

    String target = expr.type.lexeme;

    if (value == null) {
        return null;
    }


    switch (target) {
        case "Number":
            if (value instanceof Double) return value;
            break;
        case "Bool":
            if (value instanceof Boolean) return value;
            break;
        case "String":
            return stringify(value);
        default:
            throw new RuntimeError(expr.type, "Unknown cast target type '" + target + "'");
    }

    throw new RuntimeError(expr.type, "Cannot cast to " + target);
}
    

    // Function

    @Override
    public Void visitFunction(Function stmt) {
        VVPLFunction function = new VVPLFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitCall(Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof VVPLCallable)){
            throw new RuntimeError(expr.paren, "Can only call functions");
        }

        VVPLCallable function = (VVPLCallable)callee;

        if (arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren, "Epxected " + function.arity() + " arguemnts but got " + arguments.size());
        }
        return function.call(this, arguments);
    }

    //Helper functions

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    private boolean isEqual(Object a, Object b){
        if (a == null && b == null) {
            return true;
        }
        if (a == null){
            return false;
        }

        return a.equals(b);
    }

    private boolean isTruthy(Object object){
        if (object == null){
            return false;
        }

        if (object instanceof Boolean){
            return (boolean)object;
        }

        return true;
    }

    private String stringify(Object object){
        if (object == null){
            return "nill";
        }

        if (object instanceof Double){
            String text = object.toString();
            if (text.endsWith(".0")){
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private void checkNumberOperands(Token operator, Object left, Object right){
        if (left instanceof Double && right instanceof Double){
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers");
    }
    

    // runtime errors

    public static class RuntimeError extends RuntimeException{
        public final Token token;

        RuntimeError(Token token, String message){
            super(message);
            this.token = token;
        }


    }

}
