import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;

import static org.eclipse.jdt.core.dom.Modifier.*;


public class Transformations {

    private static String className = "";
    private static AnonymousClassDeclaration classNode = null;
    private static List<String> emptyConctructors= new ArrayList();
    //static boolean hasEmptyConstructor = false;

    public static boolean parseEmptyConstructor(TypeDeclaration node){
        final boolean[] hasEmptyConstructor = {false};
        node.accept(new ASTVisitor() {

            public boolean visit(MethodDeclaration node){
                if(node.isConstructor()) {
                    if (node.parameters().isEmpty()) {
                        hasEmptyConstructor[0] = true;
                    }
                }
                return true;
            }

        });
        return hasEmptyConstructor[0];
    }
    // parse string
    public static void parse(String str, final ASTParser parser, final CompilationUnit cu) {
        //ASTParser parser = ASTParser.newParser(AST.JLS8);
        //parser.setSource(str.toCharArray());
        //parser.setKind(ASTParser.K_COMPILATION_UNIT);
        //final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new ASTVisitor() {
            Set names = new HashSet();

            public boolean visit(FieldDeclaration node) {
                System.out.println(node);

                removeModifiersFields(node);

                //System.out.println("Declaration of '" + name + "' at line"
                //        + cu.getLineNumber(name.getStartPosition()));
                return true; // do not continue
            }

            public boolean visit(MethodDeclaration node) {
                if(node.isConstructor()){
                    if(node.parameters().isEmpty()){
                        //hasEmptyConstructor = true;
                    }
                    //className = node.getName().toString();
                }
                //System.out.println(node);
                removeModifiersMethods(node, cu);

                return true;
            }
            public boolean visit(TypeDeclaration node){
                addEmptyConstructor(node);
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

    public static void addEmptyConstructor(TypeDeclaration node){

        if(parseEmptyConstructor(node)){
            return;
        }

        AST ast = node.getAST();
        //ASTRewrite rewriter = ASTRewrite.create(ast);

        String className = node.getName().getFullyQualifiedName();

        MethodDeclaration newConstructor = ast.newMethodDeclaration();

        newConstructor.setName(ast.newSimpleName(className));
        newConstructor.setConstructor(true);
        newConstructor.setBody(ast.newBlock());
        ModifierKeyword amp = ModifierKeyword.PUBLIC_KEYWORD;
        newConstructor.modifiers().add(ast.newModifier(amp));

        // typeDeclaration = ( TypeDeclaration )cu.types().get( 0 );

        node.bodyDeclarations().add(newConstructor);

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
    private static void removeModifiersMethods(MethodDeclaration node, CompilationUnit cu){
        //SimpleName name = node);
        //this.names.add(name.getIdentifier());
        //System.out.println(node.toString());
        AST ast = cu.getAST();
        List<Modifier> modifiersToRemove = new ArrayList<Modifier>();

        int i = 0;

        while(i < node.modifiers().size()){
            if (node.modifiers().get(i) instanceof Modifier){
                Modifier mod = (Modifier) node.modifiers().get(i);
                if(mod.isFinal()|| mod.isProtected() ){
                    modifiersToRemove.add(mod);
                }else if (mod.isPrivate()){
                    mod.setKeyword(ModifierKeyword.PUBLIC_KEYWORD);
                }
            }
            i++;
        }


        for(Modifier mod : modifiersToRemove){
            node.modifiers().remove(mod);
        }
        //System.out.println(node.modifiers());
        System.out.println(node.modifiers());
        if(node.modifiers().size() > 0) {
            if (node.modifiers().get(0) instanceof Modifier) {
                Modifier a = (Modifier) node.modifiers().get(0);

                if (!a.isPublic()) {
                    node.modifiers().add(0, ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
                }
            }
        }else{
            node.modifiers().add(0, ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
        }
    }

    static final void runTransformation(File file) throws IOException {
        final String str = FileUtils.readFileToString(file);
        org.eclipse.jface.text.Document document = new org.eclipse.jface.text.Document(str);

        ASTParser parser = ASTParser.newParser(AST.JLS8);
        Map options = JavaCore.getOptions(); // New!
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options); // New!
        parser.setCompilerOptions(options);

        parser.setSource(document.get().toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        //String str = readFileToString(file);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);


        parse(str,parser, cu);

        FileWriter fooWriter = new FileWriter(file, false); // true to append
        fooWriter.write(cu.toString());
        fooWriter.close();

    }

    public static void main(String[] args) throws IOException {
        String path = args[0];
        //System.out.println(path);
        //File file = new File("/home/jprm/Documents/test/src/main/HikariPool.java");
        File file = new File(path);
        Transformations.runTransformation(file);
    }
}

//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main"
//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main" -Dexec.args="arg0 arg1 arg2"
//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main" -Dexec.classpathScope=runtime

