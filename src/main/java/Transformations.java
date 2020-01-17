import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import static org.eclipse.jdt.core.dom.Modifier.*;


public class Transformations {

    private static boolean hasEmptyConstructor = false;
    private static String className = "";
    private static AnonymousClassDeclaration classNode = null;


    // parse string
    public static void parse(String str, ASTParser parser,CompilationUnit cu) {
        //ASTParser parser = ASTParser.newParser(AST.JLS8);
        //parser.setSource(str.toCharArray());
        //parser.setKind(ASTParser.K_COMPILATION_UNIT);
        //final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new ASTVisitor() {
            Set names = new HashSet();

            public boolean visit(FieldDeclaration node) {

                removeModifiersFields(node);

                //System.out.println("Declaration of '" + name + "' at line"
                //        + cu.getLineNumber(name.getStartPosition()));
                return true; // do not continue
            }


            public boolean visit(SimpleName node) {
                if (this.names.contains(node.getIdentifier())) {
                    //System.out.println("Usage of '" + node + "' at line "
                      //      + cu.getLineNumber(node.getStartPosition()));
                }
                return true;
            }
            public boolean visit(MethodDeclaration node){
                if(node.isConstructor()){
                    if(node.parameters().isEmpty()){
                        hasEmptyConstructor = true;
                    }
                    className = node.getName().toString();
                    //System.out.println(className);
                }
                removeModifiersMethods(node);
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
                //parse(readFileToString(filePath));
            }
        }
    }

    private static void addEmptyConstructor(String str, ASTParser parser, CompilationUnit cu){
        //ASTParser parser = ASTParser.newParser(AST.JLS8);
        //parser.setSource(str.toCharArray());
        //parser.setKind(ASTParser.K_COMPILATION_UNIT);
        //final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        MethodDeclaration newConstructor = ast.newMethodDeclaration();

        newConstructor.setName(ast.newSimpleName(className));
        newConstructor.setConstructor(true);
        newConstructor.setBody(ast.newBlock());
        ModifierKeyword amp = ModifierKeyword.PUBLIC_KEYWORD;
        newConstructor.modifiers().add(ast.newModifier(amp));
        TypeDeclaration typeDeclaration = ( TypeDeclaration )cu.types().get( 0 );
        typeDeclaration.bodyDeclarations().add(newConstructor);

        System.out.println(typeDeclaration.toString());

    }

    private static void removeModifiersFields(FieldDeclaration node){
        //SimpleName name = node);
        //this.names.add(name.getIdentifier());
        //System.out.println(node.toString());
        List<Modifier> modifiersToRemove = new ArrayList<Modifier>();
        for(Modifier mod : (List<Modifier>) node.modifiers()){
            if(mod.isFinal()|| mod.isProtected()){
                modifiersToRemove.add(mod);
            }
        }
        for(Modifier mod : modifiersToRemove){
            node.modifiers().remove(mod);
        }
        //System.out.println(node.modifiers());
    }
    //TODO remove duplicated code
    private static void removeModifiersMethods(MethodDeclaration node){
        //SimpleName name = node);
        //this.names.add(name.getIdentifier());
        //System.out.println(node.toString());
        List<Modifier> modifiersToRemove = new ArrayList<Modifier>();


        for(Modifier mod : (List<Modifier>) node.modifiers()){
            if(mod.isFinal()|| mod.isProtected() ){
                modifiersToRemove.add(mod);
            }else if (mod.isPrivate()){
                mod.setKeyword(ModifierKeyword.PUBLIC_KEYWORD);
            }
        }
        for(Modifier mod : modifiersToRemove){
            node.modifiers().remove(mod);
        }
        //System.out.println(node.modifiers());
    }

    public static void main(String[] args) throws IOException {


        ASTParser parser = ASTParser.newParser(AST.JLS8);
        String str = readFileToString("/home/jprm/Documents/test/src/main/java/Ball.java");
        parser.setSource(str.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        parse(readFileToString("/home/jprm/Documents/test/src/main/java/Ball.java"),parser, cu);
        if(!hasEmptyConstructor) {
            addEmptyConstructor(readFileToString("/home/jprm/Documents/test/src/main/java/Ball.java"),parser,cu);
        }
    }
}


