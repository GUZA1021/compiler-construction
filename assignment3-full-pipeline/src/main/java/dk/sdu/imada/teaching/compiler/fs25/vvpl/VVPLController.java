package dk.sdu.imada.teaching.compiler.fs25.vvpl;

import java.util.ArrayList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ScopeAnalysis.ScopeAnalyser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.TypeAnalysis.TypeAnalyser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpreter.Interpreter;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class VVPLController {
    public List<String> execute(String input){
        List<String> result = new ArrayList<>();


        Scanner scanner = new Scanner(input);
        List<Token> tokens = scanner.scanTokens();
        if (!scanner.getErrors().isEmpty()){
            result.addAll(scanner.getErrors());
            return result;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();
        if (!parser.getErrors().isEmpty()){
            result.addAll(parser.getErrors());
            return result;
        }

        ScopeAnalyser scopeAnalyser = new ScopeAnalyser(stmts);
        List<String> scopeErrors = scopeAnalyser.analyse();
        if (!scopeErrors.isEmpty()){
            result.addAll(scopeErrors);
            return result;
        }


        TypeAnalyser typeAnalyser = new TypeAnalyser(stmts);
        List<String> typeErrors = typeAnalyser.analyse();
        if (!typeErrors.isEmpty()){
            result.addAll(typeErrors);
            return result;
        }

        Interpreter interpreter = new Interpreter(result);
        try {
            interpreter.interpret(stmts);
        } catch (Interpreter.RuntimeError e){
            result.add(ErrorTypeStrings.RUNTIME_ERROR);
        }

        return result;
    }
}

