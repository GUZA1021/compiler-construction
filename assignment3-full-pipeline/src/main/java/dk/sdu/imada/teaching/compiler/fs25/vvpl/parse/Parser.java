package dk.sdu.imada.teaching.compiler.fs25.vvpl.parse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
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

    private final List<String> errors = new LinkedList<>();

   
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
       
        while(!isAtEnd()){
            Stmt stmt = decl();
            if (stmt != null){
                statements.add(stmt);
            }
        } 

        return statements;
    }

    private void synchronize() {

        advance(); //  throw "bad" token away

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON){
                return;
            }
            switch (peek().type) {
                case VAR:
                case FUNCTION:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                case LEFT_BRACE:
                case RIGHT_BRACE:
                    return;
                    
                default:
                    break;
            }
            
            advance();
        }
    }

    //Starter point
    private Stmt decl(){
        try {
            if (match(TokenType.VAR)) {
                return varDecl();
            }

            if (match(TokenType.FUNCTION)) {
                return funcDecl();
            }

            return stmt();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }


    private Stmt varDecl() {

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


    
    private Stmt funcDecl() {

        // Reads function name after 'function'
        Token name = consume(TokenType.IDENTIFIER, "Expected function name after 'function'");

        // Function must starts with '('
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name");

        // List for parameter names and their type, and statements inside functoin body
        List<Token> parameterNames = new ArrayList<>();
        List<Token> paremeterTypes = new ArrayList<>();
        List<Stmt> body = new ArrayList<>();

        // Parameter list if not empty
        if (!check(TokenType.RIGHT_PAREN)){
            do{
                if (parameterNames.size() >= 255){
                    error(peek(), "cant have more than 255 parameters");
                }

                //Parameter name
                Token parameterName = consume(TokenType.IDENTIFIER, "Expected parameter name");
                
                //Each parameter need to declare its type
                consume(TokenType.TYPE_DEF, "Expected 'has_type' after parameter name");

                // Parameter type needs to be Number, String, or Bool
                Token parameterType;
                if (match(TokenType.NUMBER_TYPE, TokenType.STRING_TYPE, TokenType.BOOL_TYPE)){
                    parameterType = previous();
                } else {
                    throw error(peek(), "Expected Number, String or Bool after has_type");
                }

                parameterNames.add(parameterName);
                paremeterTypes.add(parameterType);

            } while (match(TokenType.COMMA));
        }

        // End parameter list
        consume((TokenType.RIGHT_PAREN), "Expected ')' after parameters");

        // Return type
        Token returnType = null;
        if (match(TokenType.TYPE_DEF)){
            if (match(TokenType.NUMBER_TYPE, TokenType.STRING_TYPE, TokenType.BOOL_TYPE)){
                returnType = previous();
            } else {
                throw error(peek(), "Expected return type to be Number, String or Bool after 'has_type'");
            }
        }
        // Functionn body needs to starts with '{'
        consume(TokenType.LEFT_BRACE, "Expected '{' before function body");

        //Parse function body statements untill '}'
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            body.add(decl());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after funcion body");

        // Create Function AST
        return new Stmt.Function(name, parameterNames, paremeterTypes, returnType, body);
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
        Token token = previous();
        
        consume(TokenType.LEFT_PAREN, "Expected '(' after");
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "null");
        Stmt stmt = stmt();
        Stmt elseStmt = null;
        if (match(TokenType.ELSE)) {
            elseStmt = stmt();
        }
        return new IfStmt(token, expr, stmt, elseStmt);
    }

    private Stmt whileStmt(){
        Token token = previous();

        consume(TokenType.LEFT_PAREN, "null");
        Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "null");
        Stmt stmt = stmt();
        return new WhileStmt(token, expr, stmt);
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

        if (match(TokenType.CAST)){
            Token type;
            if(match(TokenType.NUMBER_TYPE, TokenType.STRING_TYPE, TokenType.BOOL_TYPE)){
                type = previous();
            } else {
                throw error(peek(), "Expected a type Number, String or Bool after 'cast_to'");
            }
            Expr expr = unary();
            return new Expr.Cast(expr, type);
        }


        return call();
        
    }

    private Expr call(){
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)){
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee){
        List <Expr> arguments = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)){
            do{
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments" );
        return new Expr.Call(callee, paren, arguments);
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

        Token current = peek();

        if (current.type == TokenType.ELSE || current.type == TokenType.RIGHT_BRACE){
            throw error(previous(), "Expected expression.");
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

    private static class ParseError extends RuntimeException {
        public ParseError(String message){
            super(message);
        }
    }

    private ParseError error(Token token, String message){

        message = (ErrorTypeStrings.PARSE_ERROR + ", line " + token.line + " " + message);
        errors.add(message);
        return new ParseError(message);
    }

    public List<String> getErrors() {
        return errors;
    }


  }
