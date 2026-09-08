package dk.sdu.teaching.compiler.fs24.spl.codegen;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.sdu.teaching.compiler.fs24.spl.ast.Expr;
import dk.sdu.teaching.compiler.fs24.spl.ast.ExprVisitor;
import dk.sdu.teaching.compiler.fs24.spl.ast.Stmt;
import dk.sdu.teaching.compiler.fs24.spl.ast.StmtVisitor;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Assign;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Binary;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Literal;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Logical;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Unary;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Variable;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Block;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Expression;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.If;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Print;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Var;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.While;


public class LLVMEmitter implements ExprVisitor<String>, StmtVisitor<Void>{
    

    private int tempCounter = 0;
    private int labelCounter = 0;

    private final String indent = "    ";

    StringBuilder ir = new StringBuilder();
    private Map<String, String> values = new HashMap<>();


    public String getIR(){
        return ir.toString();
    }

        
    private String newTemp(){
        tempCounter++;
        return "%" + tempCounter;
    }

    private String newLabel(){
        labelCounter++;
        return "L" + labelCounter;
    }

    private void writeLine(String s){
        ir.append(s).append(System.lineSeparator());
    }

    private String compile(Expr expr){
        return expr.accept(this);
    }

    private Void compile(Stmt stmt){
        return stmt.accept(this);
    }



    public void generateCode(List<Stmt> statements) {
        for (Stmt s : statements){
            compile(s);
        }
    }

    public void saveCode(String path) throws IOException {
        Files.write(Paths.get(path), ir.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        for (Stmt s : stmt.statements){
            compile(s);
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        compile(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        String condition = compile(stmt.condition);

        String thenLabel = newLabel();
        String elseLabel = newLabel();
        String endLabel = newLabel();

        writeLine(indent + "br i1 "+ condition + ", label %" + thenLabel + ", label %" + elseLabel);
        writeLine(thenLabel + ":");
        compile(stmt.thenBranch);
        writeLine(indent + "br label %" + endLabel);

        writeLine(elseLabel + ":");
        if (stmt.elseBranch != null){
            compile(stmt.elseBranch);
        }
        writeLine(indent + "br label %" + endLabel);

        writeLine(endLabel + ":");

        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        String value = compile(stmt.expression);

        writeLine(indent + "print " + value);

        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        String name = stmt.name.lexeme;
        String value;
        
        if(stmt.initializer != null){
            value = compile(stmt.initializer);
        } else {
            value = "0"; 
        }

        writeLine(indent + "%" + name + " = " + value);

        values.put(name, "%" + name);

        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        String conditionLabel = newLabel();
        String bodyLabel = newLabel();
        String endLabel = newLabel();

        writeLine(indent + "br label %" + conditionLabel);

        writeLine(conditionLabel + ":");
        String condition = compile(stmt.condition);
        writeLine(indent + "br i1 " + condition + ", label %" + bodyLabel + ", label %" + endLabel);


        writeLine(bodyLabel + ":");
        compile(stmt.body);
        writeLine(indent + "br label %" + conditionLabel);

        writeLine(endLabel + ":");

        return null;
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        String value = compile(expr.value);
        String name = expr.name.lexeme;

        writeLine(indent + "%" + name + " = " + value);

        values.put(name, "%" + name);
        
        return "%" + name;
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        String l = compile(expr.left);
        String r = compile(expr.right);
        String t = newTemp();

        switch (expr.operator.type) {
            case PLUS:
                writeLine(indent + t + " = add i32 " + l + ", " + r);
                break;
            case MINUS:
                writeLine(indent + t + " = sub i32 " + l + ", " + r);
                break;
            case MULT:
                writeLine(indent + t + " = mul i32 " + l + ", " + r);
                break;
            case DIV:
                writeLine(indent + t + " = sdiv i32 " + l + ", " + r);
                break;
            case EQUAL_EQUAL:
                writeLine(indent + t + " = icmp eq i32 " + l + ", " + r);
                break;
            case NOT_EQUAL:
                writeLine(indent + t + " = icmp ne i32 " + l + ", " + r);
                break;
            case GREATER:
                writeLine(indent + t + " = icmp sgt i32 " + l + ", " + r);
                break;
            case GREATER_EQUAL:
                writeLine(indent + t + " = icmp sge i32 " + l + ", " + r);
                break;
            case LESS:
                writeLine(indent + t + " = icmp slt i32 " + l + ", " + r);
                break;
            case LESS_EQUAL:
                writeLine(indent + t + " = icmp sle i32 " + l + ", " + r);
                break;
            default:
                break;
        }

        return t;
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        String t = newTemp();

        if (expr.value instanceof Number num) {
            int toInt = num.intValue();
            writeLine(indent + t + " = " + toInt);
        } else if (expr.value instanceof Boolean b ) {
            writeLine(indent + t + " = " + b.toString());
        } else if (expr.value instanceof String s) {
            writeLine(indent + t + " = \"" + s + "\"");
        } else {
            writeLine(indent + t + " = 0 ; unknown literal " + expr.value);
        }

        return t;
    }

    @Override
    public String visitLogicalExpr(Logical expr) {
        String l = compile(expr.left);
        String r = compile(expr.right);
        String t = newTemp();
        
        switch (expr.operator.type) {
            case AND:
                writeLine(indent + t + " = and i1 " + l + ", " + r);     
                break;
            case OR:
                writeLine(indent + t + " = or i1 " + l + ", " + r);
                break;
            default:
                break;
        }

        return t;
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        String right = compile(expr.right);
        String t = newTemp();

        switch (expr.operator.type) {
            case NOT:
                writeLine(indent + t + " = xor i1 " + right + ", 1");
                break;
            case MINUS:
                writeLine(indent + t + " = sub i32 0, " + right);
                break;
            default:
                break;
        }
        
        return t;
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        String name = "%" + expr.name.lexeme;

        return name;
    }



}
