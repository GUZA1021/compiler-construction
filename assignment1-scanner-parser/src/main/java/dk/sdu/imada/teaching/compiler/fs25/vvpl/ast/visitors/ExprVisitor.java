package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */

public interface ExprVisitor<T> {
    T visitAssign(Expr.Assign expr);
    T visitLogical(Expr.Logical expr);
    T visitBinary(Expr.Binary expr);
    T visitUnary(Expr.Unary expr);
    T visitLiteral(Expr.Literal expr);
    T visitVariable(Expr.Variable expr);
    T visitGrouping(Expr.Grouping expr);
    T visitCast(Expr.Cast expr);
}