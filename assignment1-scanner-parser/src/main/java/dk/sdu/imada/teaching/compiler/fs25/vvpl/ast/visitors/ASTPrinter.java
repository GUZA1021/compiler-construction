package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Logical;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */

public class ASTPrinter implements ExprVisitor<String>, StmtVisitor<String> {

  private final String NL = System.lineSeparator();
  private int indent = 0;
  private String space() {
    return "  ".repeat(indent);
  }

 

  public String print(Expr expr){
    return expr.accept(this);
  }

  public String print(Stmt stmt) {
    return stmt.accept(this);
  }

  //Expr

  @Override
  public String visitAssign(Expr.Assign expr){
    StringBuilder builder = new StringBuilder();

    builder.append("AssignExpr").append(NL);
    indent++;
    builder.append(space()).append(expr.name.lexeme).append(NL);
    builder.append(space()).append(expr.value.accept(this));
    indent--;

    return builder.toString();
  } 

  @Override
  public String visitLogical(Logical expr){
    StringBuilder builder = new StringBuilder();

    builder.append("LogicalExpr").append(NL);
    indent++;
    builder.append(space()).append(expr.left.accept(this));
    builder.append(space()).append(expr.operator.lexeme).append(NL);
    builder.append(space()).append(expr.right.accept(this));
    indent--;

    return builder.toString();

  }

  @Override
  public String visitBinary(Expr.Binary expr){
    StringBuilder builder = new StringBuilder();

    builder.append("BinaryExpr").append(NL);
    indent++;
    builder.append(space()).append(expr.left.accept(this));
    builder.append(space()).append(expr.operator.lexeme).append(NL);
    builder.append(space()).append(expr.right.accept(this));
    indent--;

    return builder.toString();
  }

  @Override
  public String visitUnary(Expr.Unary expr){
    StringBuilder builder = new StringBuilder();

    builder.append("UnaryExpr").append(NL);
    indent++;
    builder.append(space()).append(expr.operator.lexeme).append(NL);
    builder.append(space()).append(expr.right.accept(this));
    indent--;

    return builder.toString();
  }

  @Override
  public String visitLiteral(Expr.Literal expr){
    StringBuilder builder = new StringBuilder();

    builder.append("LiteralExpr").append(NL);
    indent++;
    if (expr.value == null){
      builder.append(space()).append("nil").append(NL);
    } else if (expr.value instanceof String){
      builder.append(space()).append("\"").append(expr.value).append("\"").append(NL);
    } else {
      builder.append(space()).append(expr.value).append(NL);
    }
    indent--;

    return builder.toString();
  }

  @Override
  public String visitVariable(Expr.Variable expr){
    StringBuilder builder = new StringBuilder();

    builder.append("VariableExpr").append(NL);
    indent++;
    builder.append(space()).append(expr.name.lexeme).append(NL);
    indent--;

    return builder.toString();
  }


  @Override
  public String visitGrouping(Expr.Grouping expr){
    StringBuilder builder = new StringBuilder();

    builder.append("GroupingExpr").append(NL);
    indent++;
    builder.append(space()).append(expr.expr.accept(this));
    indent--;

    return builder.toString(); 
  }


  @Override
  public String visitCast(Expr.Cast expr){

    if (expr.expr instanceof Expr.Literal literal){
      StringBuilder builder = new StringBuilder();
      builder.append("LiteralExpr").append(NL);
      indent++;
      builder.append(space()).append("Cast_To ").append(expr.type.lexeme).append(NL);
      if (literal.value == null){
        builder.append(space()).append("nil").append(NL);
    } else if (literal.value instanceof String){
        builder.append(space()).append("\"").append(literal.value).append("\"").append(NL);
    } else {
        builder.append(space()).append(literal.value).append(NL);
    }
      indent--;
      return builder.toString();

    }
    StringBuilder builder = new StringBuilder();

    builder.append("CastExpr").append(NL);
    indent++;
    builder.append(space()).append("Cast_To ").append(expr.type.lexeme).append(NL);
    builder.append(space()).append(expr.expr.accept(this));
    indent--;

    return builder.toString();
  }


  //Stmt


  @Override
  public String visitBlock(Stmt.BlockStmt stmt){
    StringBuilder builder = new StringBuilder();
    builder.append("BlockStmt").append(NL);
    indent++;
    for (Stmt stmts : stmt.statements){
      builder.append(space())
             .append(print(stmts));
    }
    indent--;

    return builder.toString();
  }


  @Override
  public String visitExpr(Stmt.ExprStmt stmt){
    StringBuilder builder = new StringBuilder();

    builder.append("ExprStmt").append(NL);
    indent++;
    builder.append(space()).append(stmt.expr.accept(this));
    indent--;

    return builder.toString();
  }

  @Override 
  public String visitPrint(Stmt.PrintStmt stmt){
    StringBuilder builder = new StringBuilder();

    builder.append(("PrintStmt")).append(NL);
    indent++;
    builder.append(space()).append(stmt.expression.accept(this));
    indent--;

    return builder.toString();
  }

  @Override
  public String visitIf(Stmt.IfStmt stmt){
    StringBuilder builder = new StringBuilder();
    builder.append("IfStmt").append(NL);
    indent++;
    builder.append(space()).append(print(stmt.condition));
    builder.append(space()).append(print(stmt.thenBranch));
    if (stmt.elseBranch != null){
      builder.append(space())
             .append(print(stmt.elseBranch));
    }
    indent--;

    return builder.toString();
  }


  @Override
  public String visitReturn(Stmt.ReturnStmt stmt){
    if (stmt.value == null){
      return "ReturnStmt" + NL;
    }

    StringBuilder builder = new StringBuilder();

    builder.append("ReturnStmt").append(NL);
    indent++;
    builder.append(space()).append(stmt.value.accept(this));
    indent--;

    return builder.toString();
  }

  @Override
  public String visitVar(Stmt.Var stmt){
    StringBuilder builder = new StringBuilder();

    builder.append("VarDecl").append(NL);
    indent++;
    builder.append(space()).append(stmt.name.lexeme).append(NL);
    builder.append(space()).append(stmt.type.lexeme).append(NL);

    if (stmt.intializer != null){
      builder.append(space()).append(stmt.intializer.accept(this));
    }
    indent--;

    return builder.toString();
    
  }

  @Override
  public String visitWhile(Stmt.WhileStmt stmt){
    StringBuilder builder = new StringBuilder();
    builder.append("WhileStmt").append(NL);
    indent++;
    builder.append(space()).append(print(stmt.condition));
    builder.append(space()).append((print(stmt.body)));
    indent--;
    return builder.toString();
  }


}
