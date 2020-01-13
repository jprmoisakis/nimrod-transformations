import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;


public class Transformations {

    private static boolean hasEmptyConstructor = false;
    private static String className = "";
    private static AnonymousClassDeclaration classNode = null;

    // parse string
    public static void parse(String str) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(str.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new ASTVisitor() {
            Set names = new HashSet();

            public boolean visit(VariableDeclarationFragment node) {
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());
                //System.out.println("Declaration of '" + name + "' at line"
                //        + cu.getLineNumber(name.getStartPosition()));
                return false; // do not continue
            }

            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
                    //System.out.println("Usage of '" + node + "' at line "
                          //  + cu.getLineNumber(node.getStartPosition()));
                }
                return true;
            }
            public boolean visit(MethodDeclaration node){
                if(node.isConstructor()){
                    if(node.parameters().isEmpty()){
                        hasEmptyConstructor = true;
                    }
                    className = node.getName().toString();
                    System.out.println(className);

                }
                return true;
            }



        });
    }
    public static String readFileToString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[10];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
    //loop directory to get file list
    public static void ParseFilesInDir() throws IOException{
        File dirs = new File(".");
        String dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator;
        File root = new File(dirPath);
        File[] files = root.listFiles ( );
        String filePath = null;
        for (File f : files ) {
            filePath = f.getAbsolutePath();
            //System.out.println(f);
            if(f.isFile()){
                parse(readFileToString(filePath));
            }
        }
    }

    private static void addEmptyConstructor(String str){
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(str.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        MethodDeclaration newConstructor = ast.newMethodDeclaration();

        newConstructor.setName(ast.newSimpleName(className));
        newConstructor.setConstructor(true);
        newConstructor.setBody(ast.newBlock());
        Modifier.ModifierKeyword amp = Modifier.ModifierKeyword.PUBLIC_KEYWORD;
        newConstructor.modifiers().add(ast.newModifier(amp));

        TypeDeclaration typeDeclaration = ( TypeDeclaration )cu.types().get( 0 );
        typeDeclaration.bodyDeclarations().add(newConstructor);

        System.out.println(typeDeclaration.toString());

    }

    public static void main(String[] args) throws IOException {

        parse(readFileToString("/home/jprm/Documents/test/src/main/java/Ball.java"));
        if(!hasEmptyConstructor) {
            addEmptyConstructor(readFileToString("/home/jprm/Documents/test/src/main/java/Ball.java"));
        }
    }
}


