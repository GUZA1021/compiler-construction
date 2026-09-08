package dk.sdu.imada.teaching.compiler.fs25.vvpl.parse;

import java.util.ArrayList;
import java.util.List;


import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Unary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.PrintStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.BlockStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ExprStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.IfStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ReturnStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.WhileStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.Var;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */

public class Parser {


    private List<Token> tokens;
    private int current = 0;

   
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
       
        while(!isAtEnd()){
            statements.add(decl());
        } 

        return statements;
    }

    private void synchronize() {
        System.out.println("Synchronize called");
    }

    //Starter point
    private Stmt decl(){
        if(match(TokenType.VAR)){
            Token name = consume(TokenType.IDENTIFIER, "Expected variable name after 'variable'");

            consume(TokenType.TYPE_DEF, "Expected 'has_type' after variable name");

            Token type;
            if(match(TokenType.NUMBER_TYPE, TokenType.STRING_TYPE, TokenType.BOOL_TYPE)){
                type = previous();
            } else {
                throw error(peek(), "Expected Number, String or Bool after 'has_type'");
            }

            Expr initializer = null;
            if (match(TokenType.ASSIGN)) {
                initializer = expression();
            }
            consume(TokenType.SEMICOLON, "Expected semicolon");
            return new Var(name, type, initializer);
            
        }

        if(match(TokenType.FUNCTION)){
            //Implementation not required yet
        }

        return stmt();
    }


    //statements
    private Stmt stmt(){
        if (match(TokenType.PRINT)){
            return print();
        }

        if (match(TokenType.RETURN)){
            return returnStmt();
        }

        if (match(TokenType.IF)){
            return ifStmt();
        }

        if (match(TokenType.WHILE)){
            return whileStmt();
        }

        if (match(TokenType.LEFT_BRACE)){
            return block();
        }

        return exprStmt();
    }

    private Stmt print(){
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Semicolon");
        return new PrintStmt(expr);
    }

    private Stmt block(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            Stmt stmt = decl();
            statements.add(stmt);
        }
        consume(TokenType.RIGHT_BRACE, "null");
        return new BlockStmt(statements);
    }

    private Stmt ifStmt(){
        consume(TokenType.LEFT_PAREN, "");
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "null");
        Stmt stmt = stmt();
        Stmt elseStmt = null;
        if (match(TokenType.ELSE)) {
            elseStmt = stmt();
        }
        return new IfStmt(expr, stmt, elseStmt);
    }

    private Stmt whileStmt(){
        consume(TokenType.LEFT_PAREN, "null");
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "null");
        Stmt stmt = stmt();
        return new WhileStmt(expr, stmt);
    }

    private Stmt returnStmt(){
        Token keyword = previous();
        Expr expr = null;
        
        if (!check(TokenType.SEMICOLON)) {
            expr = expression();
        }

        consume(TokenType.SEMICOLON, "Expected semicolon");
        return new ReturnStmt(keyword, expr);
    }

    private Stmt exprStmt(){
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "null");
        return new ExprStmt(expr);
    }

    //Expression
        private Expr expression(){
        return assign();
    }

    private Expr assign(){
        Expr expr = logicOr();

        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            Expr value = assign();

            if (expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } 

            throw error(equals, "Invalid assign target");


        }
        return expr;    
    }

    private Expr logicOr() {
        Expr left = logicAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicAnd();
            left = new Expr.Logical(left, operator, right);
        }

        return left;
    }

    private Expr logicAnd() {
        Expr left = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            left = new Expr.Logical(left, operator, right);
        }

        return left;
    }

    private Expr equality(){
        Expr left = compare();

        while (match(TokenType.NOT_EQUALS, TokenType.EQUALS)){
            Token operator = previous();

            Expr right = compare();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
        
    }

    private Expr compare(){
        Expr left = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr term(){
        if (match(TokenType.PLUS, TokenType.SUB, TokenType.MULTIPLY, TokenType.DIV)){
            Token operator = previous();
            consume(TokenType.LEFT_PAREN, "No start parenthesis");
            Expr left = term();
            Expr right = term();
            Expr expr = new Expr.Binary(left, operator, right);
            consume(TokenType.RIGHT_PAREN, "No end parenthesis");
            return expr;
        }

        return unary();
    }

    private Expr unary(){
        if (match(TokenType.NOT, TokenType.MINUS)){
            Token operator = previous();
            Expr expr = unary();
            return new Unary(operator, expr);
        }

        return call();
        
    }

    private Expr call(){
        return primary();
    }

    private Expr primary(){


        if (match(TokenType.FALSE)){
            return new Expr.Literal(false);
        }

         if (match(TokenType.TRUE)){
            return new Expr.Literal(true);
        }    
        
        if (match(TokenType.NUMBER, TokenType.STRING)){
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)){
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping((expr));
        }

        if (match(TokenType.IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if (match(TokenType.CAST)){
            Token type;
            if(match(TokenType.NUMBER_TYPE, TokenType.STRING_TYPE, TokenType.BOOL_TYPE)){
                type = previous();
            } else {
                throw error(peek(), "Expected a type Number, String or Bool after 'cast_to'");
            }
            Expr inner = primary();
            return new Expr.Cast(inner, type);
        }

        throw error(peek(),"Expected expression.");
    }



    //HELPER METHODS
    //return the current token
    private Token peek(){
        return tokens.get(current);
    }

    //return the previous token
    private Token previous() {
        return tokens.get(current - 1);
    }

    //check if we are at the end of the token list
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    } 

    //Check if token matches type without consuming it
    private boolean check(TokenType type){
        if (isAtEnd()){
            return false;
        } 
        return peek().type == type;
    }

    //check if token matches type
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                current++;
                return true;
            }
        }
        return false;
    }

    //Consume token iff token matches type
    private Token consume(TokenType type, String message){
        if(check(type)){
            return advance();
        } else {
            throw error(peek(), message);
        }

    }

    private Token advance(){
        if (!isAtEnd()){
            current ++;
        }
        return previous();
    }

    private static class ParseError extends RuntimeException {}

    private ParseError error(Token token, String message){
        System.out.println("Parse error at line " + token.line + ": " + message);
        return new ParseError();
    }
  }
