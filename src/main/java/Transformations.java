import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;


import static org.eclipse.jdt.core.dom.Modifier.*;


public class Transformations {


    //todo if it doesnt have any constructor?
    public static boolean hasEmptyConstructor(TypeDeclaration node){
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

    public static void transform(final CompilationUnit cu) {

        cu.accept(new ASTVisitor() {

            public boolean visit(FieldDeclaration node) {
                RemoveFinalModifierAndMakeFieldPublic(node);
                return true;
            }

            public boolean visit(MethodDeclaration node) {
                RemoveFinalModifierAndMakeMethodPublic(node);
                return true;
            }
            public boolean visit(TypeDeclaration node){
                addEmptyConstructorAndMakeClassPublic(node);
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
//add empty constructor
    public static void addEmptyConstructorAndMakeClassPublic(TypeDeclaration node){

        int i = 0;
        while (i < node.modifiers().size()) {
            if (node.modifiers().get(i) instanceof Modifier) {
                Modifier mod = (Modifier) node.modifiers().get(i);
                if (mod.isPrivate() || mod.isProtected()) {
                    mod.setKeyword(ModifierKeyword.PUBLIC_KEYWORD);
                }
            }
            i++;
        }

        if(!node.isInterface()){
            if(hasEmptyConstructor(node)){
                return;
            }

            AST ast = node.getAST();
            String className = node.getName().getFullyQualifiedName();
            MethodDeclaration newConstructor = ast.newMethodDeclaration();

            newConstructor.setName(ast.newSimpleName(className));
            newConstructor.setConstructor(true);
            newConstructor.setBody(ast.newBlock());
            ModifierKeyword amp = ModifierKeyword.PUBLIC_KEYWORD;
            newConstructor.modifiers().add(ast.newModifier(amp));

            node.bodyDeclarations().add(newConstructor);
        }


    }

    //Remove remove Final modifiers and set private and protected modifiers
    private static void RemoveFinalModifierAndMakeFieldPublic(FieldDeclaration node) {
        List<Modifier> modifiersToRemove = new ArrayList<Modifier>();
        int i = 0;

        while (i < node.modifiers().size()) {
            if (node.modifiers().get(i) instanceof Modifier) {
                Modifier mod = (Modifier) node.modifiers().get(i);
                if (mod.isFinal()) {
                    modifiersToRemove.add(mod);
                } else if (mod.isPrivate() || mod.isProtected()) {
                    mod.setKeyword(ModifierKeyword.PUBLIC_KEYWORD);
                }
            }
            i++;
        }
        for (Modifier mod : modifiersToRemove) {
            node.modifiers().remove(mod);
        }

        if (node.modifiers().size() > 0 && !node.toString().contains("public ")) {

            Object firstMod = node.modifiers().get(0);
            if (firstMod instanceof Annotation) {
                if(!((Modifier) node.modifiers().get(1)).isPublic()){
                    node.modifiers().add(1, node.getAST().newModifier(ModifierKeyword.PUBLIC_KEYWORD));
                }

            } else if (firstMod instanceof Modifier) {
                if(!((Modifier) firstMod).isPublic()){
                    node.modifiers().add(0, node.getAST().newModifier(ModifierKeyword.PUBLIC_KEYWORD));
                }
            }
        }


    }

    private static void RemoveFinalModifierAndMakeMethodPublic(MethodDeclaration node){

        List<Modifier> modifiersToRemove = new ArrayList<Modifier>();
        int i = 0;

        while (i < node.modifiers().size()) {
            if (node.modifiers().get(i) instanceof Modifier) {
                Modifier mod = (Modifier) node.modifiers().get(i);
                if (mod.isFinal()) {
                    modifiersToRemove.add(mod);
                } else if (mod.isPrivate() || mod.isProtected()) {
                    mod.setKeyword(ModifierKeyword.PUBLIC_KEYWORD);
                }
            }
            i++;
        }
        for (Modifier mod : modifiersToRemove) {
            node.modifiers().remove(mod);
        }

        if (node.modifiers().size() > 0 && !node.toString().contains("public ")) {

            Object firstMod = node.modifiers().get(0);
            if (firstMod instanceof Annotation) {
                if(!((Modifier) node.modifiers().get(1)).isPublic()){
                    node.modifiers().add(1, node.getAST().newModifier(ModifierKeyword.PUBLIC_KEYWORD));
                }

            } else if (firstMod instanceof Modifier) {
                if(!((Modifier) firstMod).isPublic()){
                    node.modifiers().add(0, node.getAST().newModifier(ModifierKeyword.PUBLIC_KEYWORD));
                }
            }
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

        transform(cu);

        FileWriter fooWriter = new FileWriter(file, false); // true to append
        fooWriter.write(cu.toString());
        fooWriter.close();

    }

    public static void main(String[] args) throws IOException {
        String path = args[0];
        //System.out.println(path);
        //File file = new File("/home/jprm/Documents/test/src/main/ExplodedArchive.java");
        File file = new File(path);
        Transformations.runTransformation(file);
    }
}

//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main"
//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main" -Dexec.args="arg0 arg1 arg2"
//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main" -Dexec.classpathScope=runtime

