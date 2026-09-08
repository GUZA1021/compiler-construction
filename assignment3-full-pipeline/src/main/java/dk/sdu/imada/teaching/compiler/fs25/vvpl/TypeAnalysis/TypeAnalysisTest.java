package dk.sdu.imada.teaching.compiler.fs25.vvpl.TypeAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class TypeAnalysisTest {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("/Users/amin/Downloads/assignment1-main/vvpl-template/src/main/java/dk/sdu/imada/teaching/compiler/fs25/vvpl/TypeAnalysis/unkonwn.in");

        String program = Files.readString(path);

        Scanner scanner = new Scanner(program);
        List<Token> tokens = scanner.scanTokens();

        if (!scanner.getErrors().isEmpty()){
            return;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        if (!parser.getErrors().isEmpty()){
            return;
        }

        TypeAnalyser analyser = new TypeAnalyser(stmts);
        List<String> typeErrors = analyser.analyse();

        for (String errors : typeErrors){
            System.out.println(errors);
        }

    }

}