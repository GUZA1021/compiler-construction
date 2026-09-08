package dk.sdu.imada.teaching.compiler.fs25.vvpl.TypeAnalysis;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class FunctionInfo {
    public final List<Token> parameterNames;
    public final List<Token> parameterTypes;
    public final Type returnType;
    public final List<Stmt> body;


    public FunctionInfo(List<Token> parameterNames, List<Token> parameterTypes, Type returnType, List<Stmt> body){
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.body = body;
    }
}
