package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */

public interface StmtVisitor<T> {
    T visitBlock(Stmt.BlockStmt stmt);
    T visitExpr(Stmt.ExprStmt stmt);
    T visitPrint(Stmt.PrintStmt stmt);
    T visitIf(Stmt.IfStmt stmt);
    T visitReturn(Stmt.ReturnStmt stmt);
    T visitVar(Stmt.Var stmt);
    T visitWhile(Stmt.WhileStmt stmt);
    T visitFunction(Stmt.Function stmt);
}