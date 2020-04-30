import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.corext.codemanipulation.*;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

import static org.eclipse.jdt.core.dom.Modifier.*;


public class Transformations {


    //todo if it doesnt have any constructor?
    public static boolean hasEmptyConstructor(TypeDeclaration node){

        for(MethodDeclaration method : node.getMethods()){
            if (method.isConstructor() && method.parameters().isEmpty()){
               return true;
            }
        }

        return false;
    }

    public static void transform(final CompilationUnit cu) {

        cu.accept(new ASTVisitor() {

            public boolean visit(FieldDeclaration node) {
                RemoveFinalModifierAndMakeFieldPublic(node);
                //addGettersAndSetters(node);
                //addgetset(node);
                return true;
            }

            public boolean visit(MethodDeclaration node) {
                RemoveFinalModifierAndMakeMethodPublic(node);
                return true;
            }
            public boolean visit(TypeDeclaration node){
                addEmptyConstructorAndMakeClassPublic(node);
                addgettersSetters(node);
                return true;
            }


        });
    }
    private static Block createGetterMethodBody(AST ast, String variableName)
    {
        String body = "return this."+ variableName+";";
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(body.toCharArray());
        ASTNode astNodeWithMethodBody = parser.createAST(null);
        ASTNode convertedAstNodeWithMethodBody =
                ASTNode.copySubtree(ast, astNodeWithMethodBody);
        Block block = (Block)convertedAstNodeWithMethodBody;

        return block;
    }
    private static Block createSetterMethodBody(AST ast, String variableName)
    {
        String body = "this."+ variableName+" = " + variableName + ";";
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(body.toCharArray());
        ASTNode astNodeWithMethodBody = parser.createAST(null);
        ASTNode convertedAstNodeWithMethodBody =
                ASTNode.copySubtree(ast, astNodeWithMethodBody);
        Block block = (Block)convertedAstNodeWithMethodBody;

        return block;
    }
    public static void addgettersSetters(TypeDeclaration node){

        List<String> variableNames = new ArrayList<String>();
        List<Type> variableTypes = new ArrayList<Type>();

        if(!node.isInterface()){
            for(FieldDeclaration field: node.getFields()){

                variableTypes.add(field.getType());

                Object fragments = field.fragments().get(0);
                if(fragments instanceof VariableDeclarationFragment){
                    String variableName = ((VariableDeclarationFragment) fragments).getName().toString();
                    variableNames.add(variableName);
                    System.out.println(variableName);
                }

            }
            ModifierKeyword publicMod = ModifierKeyword.PUBLIC_KEYWORD;
            AST astNode = node.getAST();


            //AST ast = node.getAST();
            for(int i = 0 ; i< variableNames.size(); i++) {
                Type type = variableTypes.get(i);


                ASTNode converted = ASTNode.copySubtree(astNode,type);
                Type tipo = (Type) converted;




                MethodDeclaration getter = astNode.newMethodDeclaration();
                getter.setName(astNode.newSimpleName("get"+ variableNames.get(i)));
                getter.modifiers().add(astNode.newModifier(publicMod));
                getter.setReturnType2(tipo);
                Block getterBody = createGetterMethodBody(astNode, variableNames.get(i));
                getter.setBody(getterBody);

                node.bodyDeclarations().add(getter);


                ASTNode converted2 = ASTNode.copySubtree(astNode,type);
                Type tipo2 = (Type) converted2;

                MethodDeclaration setter = astNode.newMethodDeclaration();
                setter.setName(astNode.newSimpleName("set"+ variableNames.get(i)));
                setter.modifiers().add(astNode.newModifier(publicMod));
                setter.setReturnType2(astNode.newPrimitiveType(PrimitiveType.VOID));
                SingleVariableDeclaration parameter = astNode.newSingleVariableDeclaration();
                parameter.setType(tipo2);
                parameter.setName(astNode.newSimpleName(variableNames.get(i)));
                setter.parameters().add(parameter);
                Block setterBody = createSetterMethodBody(astNode, variableNames.get(i));
                setter.setBody(setterBody);

                node.bodyDeclarations().add(setter);



                //MethodDeclaration setter = astNode.newMethodDeclaration();
                //setter.setName(astNode.newSimpleName("get"+ variableNames.get(i)));

                //setter.setReturnType2(astNode.newPrimitiveType(PrimitiveType.VOID));
                //setter.modifiers().add(astNode.newModifier(amp));
                //setter.setReturnType2(tipo);

                //SingleVariableDeclaration variableDeclaration = astNode.newSingleVariableDeclaration();
                //Type b = variableTypes.get(i);
                //Object c = variableTypes.get(i);


                //variableDeclaration.setName(astNode.newSimpleName(variableNames.get(i)));
                //boolean is = b.isParameterizedType();
                //ast.newTypeLiteral().setType(b);
                //astNode.newParameterizedType(b);
                //variableDeclaration.setType(tipo);
                //ast.newParameterizedType();
                //b.getParent().
                //setter.parameters().add(variableDeclaration);

                //node.bodyDeclarations().add(setter);
            }
            /*
            ModifierKeyword amp = ModifierKeyword.PUBLIC_KEYWORD;
            AST ast = node.getAST();
            for(int i = 0 ; i< variableNames.size(); i++){
                MethodDeclaration getter = ast.newMethodDeclaration();
                getter.setName(ast.newSimpleName(variableNames.get(i)));

                getter.setReturnType2(null);
                //getter.setReturnType2(variableTypes.get(i));
                getter.modifiers().add(ast.newModifier(amp));
                node.bodyDeclarations().add(getter);

            }
            */








        }

    }


    public static void addgetset(FieldDeclaration node){

        AST ast = node.getAST();


/*

        String className = node.getName().getFullyQualifiedName();
        MethodDeclaration newConstructor = ast.newMethodDeclaration();

        newConstructor.setName(ast.newSimpleName(className));
        newConstructor.setConstructor(true);
        newConstructor.setBody(ast.newBlock());
        ModifierKeyword amp = ModifierKeyword.PUBLIC_KEYWORD;
        newConstructor.modifiers().add(ast.newModifier(amp));

        node.bodyDeclarations().add(newConstructor);
  */
    }

    public static void addGettersAndSetters(FieldDeclaration node) {
        List<VariableDeclarationFragment> fragments = node.fragments();
        for (VariableDeclarationFragment  fragment : fragments) {
            //VariableDeclarationFragment fragment = fragments.get(0);
            //IJavaElement fieldElement = fragment.resolveBinding().getJavaElement();
            IVariableBinding binding = fragment.resolveBinding();
            IField field = (IField) binding.getJavaElement();


            if (field instanceof IField) {
                try {
                    if (GetterSetterUtil.getGetter((IField) field) == null) {
                        String getter = GetterSetterUtil.getGetterName((IField) field, null);
                        String stub = GetterSetterUtil.getGetterStub((IField) field, getter, false, 0);
                        System.out.println(stub);
                    }
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                //if(GetterSetterUtil.getSetter((IField) fieldElement) != null){

                //}
            }
        }
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
        if(!node.isInterface()){

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
        //String path = args[0];
        //System.out.println(path);
        File file = new File("/home/jprm/Documents/test/src/main/ExplodedArchive.java");
        //File file = new File(path);
        Transformations.runTransformation(file);
    }
}

//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main"
//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main" -Dexec.args="arg0 arg1 arg2"
//mvn exec:java -Dexec.mainClass="com.vineetmanohar.module.Main" -Dexec.classpathScope=runtime

