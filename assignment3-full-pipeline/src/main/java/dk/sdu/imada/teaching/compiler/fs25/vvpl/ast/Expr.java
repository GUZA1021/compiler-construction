package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ExprVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/**
 * @author Mohammad Adnan Amin, Karim Adnan Amin, Taaha Najeeb Khan
 * @version CompilerConstruction FT 2025
 */


 public abstract class Expr {
    public abstract <T> T accept(ExprVisitor<T> visitor);


    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value){
            this.name = name; 
            this.value = value;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visitAssign(this);
        }
    }

    public static class Logical extends Expr{
        public final Expr left;
        public final Token operator;
        public final Expr  right;

        public Logical(Expr left, Token operator, Expr right){
            this.left = left; 
            this.operator = operator; 
            this.right = right;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visitLogical(this);
        }

    }


    public static class Binary extends Expr{
        public final Expr left;
        public final Token operator;
        public final Expr  right;
        
        public Binary(Expr left, Token operator, Expr right){
            this.left = left; 
            this.operator = operator; 
            this.right = right;
        }
        
        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visitBinary(this);
        }
    }
    
    public static class Unary extends Expr{
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right){
            this.operator = operator;
            this.right = right; 
        }
        
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visitUnary(this);
        }
    }

    public static class Literal extends Expr{
        public final Object value;

        public Literal(Object value){
            this.value = value;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visitLiteral(this);
        }
    }
    
    public static class Variable extends Expr{
        public final Token name;

        public Variable(Token name){
            this.name = name; 
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visitVariable(this);
        }
    }

    public static class Grouping extends Expr{
        public final Expr expr;

        public Grouping(Expr expr){
            this.expr = expr;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor) {
            return visitor.visitGrouping(this);
        }
    }

    public static class Cast extends Expr{
        public final Expr expr;
        public final Token type;

        public Cast(Expr expr, Token type){
            this.expr = expr;
            this.type = type;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor){
            return visitor.visitCast(this);
        }
        
    }

    public static class Call extends Expr{
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
        
        public Call(Expr callee, Token paren, List<Expr> argument){
            this.callee = callee;
            this.paren = paren;
            this.arguments = argument;
        }

        @Override
        public <T> T accept(ExprVisitor<T> visitor){
            return visitor.visitCall(this);
        }
    }

}
