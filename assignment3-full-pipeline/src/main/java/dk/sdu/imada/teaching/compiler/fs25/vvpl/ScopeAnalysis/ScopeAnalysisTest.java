package dk.sdu.imada.teaching.compiler.fs25.vvpl.ScopeAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class ScopeAnalysisTest {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("/Users/amin/Downloads/assignment1-main/vvpl-template/src/main/java/dk/sdu/imada/teaching/compiler/fs25/vvpl/ScopeAnalysis/extra_tests/shadowing_error.in");

        String program = Files.readString(path);

        Scanner scanner = new Scanner(program);
        List<Token> tokens = scanner.scanTokens();

        if (!scanner.getErrors().isEmpty()){
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        if (!parser.getErrors().isEmpty()){
            for (String errors : parser.getErrors()){
                System.out.println(errors);
            }
            return;
        }

        ScopeAnalyser analyser = new ScopeAnalyser(stmts);
        List<String> scopeErrors = analyser.analyse();

        for (String errors : scopeErrors){
            System.out.println(errors);
        }

    }

}

