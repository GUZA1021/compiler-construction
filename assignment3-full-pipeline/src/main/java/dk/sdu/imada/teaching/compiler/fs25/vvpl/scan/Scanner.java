package dk.sdu.imada.teaching.compiler.fs25.vvpl.scan;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;

import java.util.HashMap;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */

public class Scanner {

    private final List<Token> scannedTokens = new LinkedList<>();
    private final List<String> errors = new LinkedList<>();

    private final String inputString;
    // Scanning state

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();


    static{
        KEYWORDS.put("variable", TokenType.VAR);
        KEYWORDS.put("is", TokenType.ASSIGN);

        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("loop_while", TokenType.WHILE);
        KEYWORDS.put("write_to_console", TokenType.PRINT);

        KEYWORDS.put("AND", TokenType.AND);
        KEYWORDS.put("OR", TokenType.OR);
        KEYWORDS.put("NOT", TokenType.NOT);

        KEYWORDS.put("EQUALS", TokenType.EQUALS);
        KEYWORDS.put("NOT_EQUALS", TokenType.NOT_EQUALS);

        KEYWORDS.put("GREATER", TokenType.GREATER);
        KEYWORDS.put("GREATER_EQUAL", TokenType.GREATER_EQUAL);
        KEYWORDS.put("LESS", TokenType.LESS);
        KEYWORDS.put("LESS_EQUAL", TokenType.LESS_EQUAL);

        KEYWORDS.put("add", TokenType.PLUS);
        KEYWORDS.put("subtract", TokenType.SUB);
        KEYWORDS.put("multiply", TokenType.MULTIPLY);
        KEYWORDS.put("divide", TokenType.DIV);
       
        KEYWORDS.put("function", TokenType.FUNCTION);
        KEYWORDS.put("return", TokenType.RETURN);

        KEYWORDS.put("cast_to", TokenType.CAST);

        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);

        KEYWORDS.put("has_type", TokenType.TYPE_DEF);
        
        KEYWORDS.put("Number", TokenType.NUMBER_TYPE);
        KEYWORDS.put("String", TokenType.STRING_TYPE);
        KEYWORDS.put("Bool", TokenType.BOOL_TYPE);

    }


    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String inputString) {
        this.inputString = inputString;
    }

    public List<Token> scanTokens() {

        while (!isAtEnd()){
            start = current;
            char c = advance();
            switch (c) {
                case '(':
                    addToken(TokenType.LEFT_PAREN);
                    break;
                case ')':
                    addToken(TokenType.RIGHT_PAREN);
                    break;
                case '{':
                    addToken(TokenType.LEFT_BRACE);
                    break;
                case '}':
                    addToken(TokenType.RIGHT_BRACE);
                    break;
                case ',':
                    addToken(TokenType.COMMA);
                    break;
                case ';':
                    addToken(TokenType.SEMICOLON);
                    break;
                case '.':
                    if(isDigit(peek())){
                        start = current - 1;
                        number();
                    } else {
                        addError("unexpected character: .");
                    }
                    break;

                case '-': //Negation not substraction
                    addToken(TokenType.MINUS);
                    break;
                    
                // case '+':
                //     addToken(TokenType.PLUS);
                //     break;
                // case '*':
                //     addToken(TokenType.MULTIPLY);
                //     break;
                // case '/':
                //     addToken(TokenType.DIV);
                //     break;

                // case '>':
                //     addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                //     break;
                // case '<':
                //     addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                //     break;

                // Ignore whitespace
                case ' ':
                case '\r':
                case '\t': 
                    break;

                case '\n':
                    line++;
                    break;

                case '"':
                    string();
                    break;
                case '#':
                    while(peek() != '\n' && !isAtEnd()){
                        advance();
                    }
                    break;
                

                default:
                    if (isDigit(c)){
                        number();
                    } else if (isAlpha(c)){
                        identifier();
                    } else {
                        addError("unexpected character" + c);
                    }
                    break;

            }
        }

        start = current;
        addToken(TokenType.EOF);
        return scannedTokens;
    }



    //////////////////////////////////////////////////////////////////////
    // helper methods
    //////////////////////////////////////////////////////////////////////
    private char advance() {
        return inputString.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = inputString.substring(start, current);
        scannedTokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected){
        if(isAtEnd()){
            return false;
        }
        if(inputString.charAt(current) != expected){
            return false;
        }
        
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return inputString.charAt(current);
    }

    private char peekNext(){
        if (current + 1 >= inputString.length()){
            return '\0';
        }

        return inputString.charAt(current + 1);

    }

    private boolean isAtEnd() {
        return current >= inputString.length();
    }

    private void string(){

        while (peek()!= '"' && !isAtEnd() && peek() != '\n'){
            advance();
        }
        

        if (isAtEnd() || peek() == '\n'){
            addError("Unexpected string");
            return;
        }

        advance();

        String value = inputString.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    private void identifier(){
        while(isAlphaNumeric(peek())){
            advance();
        }

        String text = inputString.substring(start, current);
        TokenType type = KEYWORDS.get(text);
        
        if(type == null){
            type = TokenType.IDENTIFIER;
        }
        addToken(type);
    }



    private void number() {
        while (!isAtEnd() && isDigit(peek())){
            advance();
        }

        if (!isAtEnd() && peek() == '.' && isDigit(peekNext())){
            advance();

            while (!isAtEnd() && isDigit(peek())){
                advance();
            } 
        }
        
        addToken(TokenType.NUMBER, Double.parseDouble(inputString.substring(start,current)));

    }

    private void addError(String message) {
        errors.add(ErrorTypeStrings.SCAN_ERROR + ", line " + line + " " +  message);
    }

    public List<String> getErrors() {
        return errors;
    }
}
