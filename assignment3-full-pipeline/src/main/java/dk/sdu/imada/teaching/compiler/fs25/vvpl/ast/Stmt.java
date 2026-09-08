package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.StmtVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */

// igen overide er ikke rigtigt, den stopper bare med compiler fejl
public abstract class Stmt {
    public abstract <T> T accept(StmtVisitor<T> visitor);

    public static class BlockStmt extends Stmt {
        public final List<Stmt> statements;
        
        public BlockStmt(List<Stmt> statements){
            this.statements = statements;
        }

        @Override
        public <T> T accept (StmtVisitor<T> visitor){
            return visitor.visitBlock(this);
        }

    }

    public static class ExprStmt extends Stmt{
        public final Expr expr;

        public ExprStmt(Expr expr){
            this.expr = expr;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor){
            return visitor.visitExpr(this);
        }
    }

    public static class PrintStmt extends Stmt{
        public final Expr expression;

        public PrintStmt(Expr expression){
            this.expression = expression;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor){
            return visitor.visitPrint(this);
        }
    }

    public static class IfStmt extends Stmt{
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        public final Token token;

        public IfStmt(Token token, Expr condition, Stmt thenBranch, Stmt elseBranch){
            this.token = token;
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor){
            return visitor.visitIf(this);
        }
    }

    public static class ReturnStmt extends Stmt{
        public final Token keyword;
        public final Expr value;

        public ReturnStmt(Token keyword, Expr value){
            this.keyword = keyword;
            this.value = value;
        }
        
        @Override
        public <T> T accept(StmtVisitor<T> visitor){
            return visitor.visitReturn(this);
        }

    }

    public static class Var extends Stmt{
        public final Token name;
        public final Token type;
        public final Expr initializer;

        public Var(Token name, Token type, Expr intializer){
            this.name = name;
            this.type = type;
            this.initializer = intializer;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor){
            return visitor.visitVar(this);
        }
    }

    public static class WhileStmt extends Stmt{
        public final Token token;
        public final Expr condition;
        public final Stmt body;

        public WhileStmt(Token token, Expr condition, Stmt body){
            this.token = token;
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor){
            return visitor.visitWhile(this);
        }
    }


    public static class Function extends Stmt{
        public final Token name;
        public final List<Token> parameterNames;
        public final List<Token> parameterTypes;
        public final Token returnType;
        public final List<Stmt> body;

        public Function(Token name, List<Token> parameterNames, List<Token> parameterTypes, Token returnType, List<Stmt> body){
            this.name = name;
            this.parameterNames = parameterNames;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
            this.body = body;
        }

        @Override
        public <T> T accept(StmtVisitor<T> visitor){
            return visitor.visitFunction(this);
        }
    }

}
