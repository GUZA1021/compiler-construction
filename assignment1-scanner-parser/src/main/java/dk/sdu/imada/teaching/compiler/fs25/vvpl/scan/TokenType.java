package dk.sdu.imada.teaching.compiler.fs25.vvpl.scan;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */

public enum TokenType {
    // Single-character tokens
    SEMICOLON, LEFT_PAREN, RIGHT_PAREN, 
    LEFT_BRACE, RIGHT_BRACE, COMMA, MINUS,

    // Type
    NUMBER_TYPE, STRING_TYPE, BOOL_TYPE,

    // Literals
    NUMBER, STRING, IDENTIFIER,

    // Keywords
    VAR, ASSIGN, 
    IF, ELSE, 
    WHILE,
    PRINT,
    AND, OR, NOT,
    EQUALS, NOT_EQUALS,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    PLUS, SUB, MULTIPLY, DIV, 
    FUNCTION, RETURN, 
    CAST,
    TRUE, FALSE,
    TYPE_DEF, //Type of the variable
 


    // End-of-file
    EOF
}
