package fr.inptoulouse.sn.ide7.uml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Genere une classe Java simple a partir d'une description UML textuelle.
 */
public class UMLClassGenerator {
    private static final Pattern CLASS_PATTERN = Pattern.compile("(?m)^\\s*(class|interface|enum)\\s+(\\w+)\\s*\\{([\\s\\S]*?)^\\s*\\}");
    private static final Pattern MEMBER_PATTERN = Pattern.compile("^\\s*([+\\-#~])\\s+(\\w+)\\s*(?:\\(([^)]*)\\)\\s*(?::\\s*([\\w<>\\[\\]]+))?|:\\s*([\\w<>\\[\\]]+))\\s*$");

    public JavaClass parseFirstClass(String umlText) {
        List<JavaClass> classes = parseAll(umlText);
        if (classes.isEmpty()) {
            throw new IllegalArgumentException("Aucune classe UML trouvee. Format attendu: class Nom { ... }");
        }
        return classes.get(0);
    }

    public List<JavaClass> parseAll(String umlText) {
        if (umlText == null || umlText.trim().isEmpty()) {
            throw new IllegalArgumentException("Le texte UML est vide.");
        }

        List<JavaClass> classes = new ArrayList<JavaClass>();
        Matcher classMatcher = CLASS_PATTERN.matcher(umlText);
        while (classMatcher.find()) {
            JavaClass javaClass = new JavaClass(classMatcher.group(2));
            javaClass.setKind(classMatcher.group(1));
            parseMembers(classMatcher.group(3), javaClass);
            classes.add(javaClass);
        }

        return classes;
    }

    public String generateSource(JavaClass javaClass, String packageName) {
        return generateSource(javaClass, packageName, new ArrayList<JavaRelation>());
    }

    public String generateSource(JavaClass javaClass, String packageName, List<JavaRelation> relations) {
        return generateSource(javaClass, packageName, relations, new ArrayList<JavaClass>());
    }

    public String generateSource(
        JavaClass javaClass,
        String packageName,
        List<JavaRelation> relations,
        List<JavaClass> allClasses
    ) {
        StringBuilder builder = new StringBuilder();
        if (packageName != null && !packageName.isEmpty()) {
            builder.append("package ").append(packageName).append(";\n\n");
        }

        if ("interface".equals(javaClass.getKind())) {
            appendInterface(builder, javaClass, relations);
            return builder.toString();
        }
        if ("enum".equals(javaClass.getKind())) {
            builder.append("public enum ").append(javaClass.getName()).append(" {\n    VALUE\n}\n");
            return builder.toString();
        }

        if ("abstract class".equals(javaClass.getKind())) {
            builder.append("public abstract class ");
        } else {
            builder.append("public class ");
        }
        builder.append(javaClass.getName());
        appendClassRelations(builder, relations);
        builder.append(" {\n");

        for (JavaAttribut attribut : javaClass.getAttributes()) {
            builder.append("    ")
                .append(keyword(attribut.getVisibility()))
                .append(' ')
                .append(attribut.getType())
                .append(' ')
                .append(attribut.getName())
                .append(";\n");
        }

        appendRelationAttributes(builder, javaClass, relations);

        List<JavaMethode> inheritedConstructors = constructorsToMirror(javaClass, relations, allClasses);

        if (!javaClass.getAttributes().isEmpty()
            && (!javaClass.getMethods().isEmpty() || !inheritedConstructors.isEmpty())) {
            builder.append('\n');
        }

        appendInheritedConstructors(builder, javaClass, inheritedConstructors);

        for (JavaMethode methode : javaClass.getMethods()) {
            appendMethod(builder, javaClass, methode);
        }

        builder.append("}\n");
        return builder.toString();
    }

    private List<JavaMethode> constructorsToMirror(
        JavaClass javaClass,
        List<JavaRelation> relations,
        List<JavaClass> allClasses
    ) {
        List<JavaMethode> result = new ArrayList<JavaMethode>();

        if (hasConstructor(javaClass)) {
            return result;
        }

        String parentName = firstParentName(relations);
        if (parentName == null) {
            return result;
        }

        JavaClass parentClass = findClass(parentName, allClasses);
        if (parentClass == null) {
            return result;
        }

        for (JavaMethode method : parentClass.getMethods()) {
            if (isConstructor(parentClass, method)) {
                result.add(method);
            }
        }

        return result;
    }

    private void appendInheritedConstructors(
        StringBuilder builder,
        JavaClass javaClass,
        List<JavaMethode> parentConstructors
    ) {
        for (JavaMethode parentConstructor : parentConstructors) {
            builder.append("    public ")
                .append(javaClass.getName())
                .append('(');

            for (int i = 0; i < parentConstructor.getParameters().size(); i++) {
                JavaParamettre parameter = parentConstructor.getParameters().get(i);
                builder.append(parameter.getType()).append(' ').append(parameter.getName());
                if (i < parentConstructor.getParameters().size() - 1) {
                    builder.append(", ");
                }
            }

            builder.append(") {\n        super(");
            for (int i = 0; i < parentConstructor.getParameters().size(); i++) {
                JavaParamettre parameter = parentConstructor.getParameters().get(i);
                builder.append(parameter.getName());
                if (i < parentConstructor.getParameters().size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append(");\n    }\n\n");
        }
    }

    private boolean hasConstructor(JavaClass javaClass) {
        for (JavaMethode method : javaClass.getMethods()) {
            if (isConstructor(javaClass, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean isConstructor(JavaClass javaClass, JavaMethode method) {
        return javaClass.getName().equals(method.getName())
            || method.getReturnType() == null
            || method.getReturnType().isEmpty();
    }

    private String firstParentName(List<JavaRelation> relations) {
        if (relations == null) {
            return null;
        }
        for (JavaRelation relation : relations) {
            if ("héritage".equals(relation.getType())) {
                return relation.getTarget();
            }
        }
        return null;
    }

    private JavaClass findClass(String className, List<JavaClass> classes) {
        if (classes == null) {
            return null;
        }
        for (JavaClass javaClass : classes) {
            if (className.equals(javaClass.getName())) {
                return javaClass;
            }
        }
        return null;
    }

    private void appendInterface(StringBuilder builder, JavaClass javaClass, List<JavaRelation> relations) {
        builder.append("public interface ").append(javaClass.getName());
        appendInterfaceRelations(builder, relations);
        builder.append(" {\n");

        for (JavaMethode methode : javaClass.getMethods()) {
            builder.append("    ")
                .append(methode.getReturnType() == null || methode.getReturnType().isEmpty() ? "void" : methode.getReturnType())
                .append(' ')
                .append(methode.getName())
                .append('(');

            for (int i = 0; i < methode.getParameters().size(); i++) {
                JavaParamettre parameter = methode.getParameters().get(i);
                builder.append(parameter.getType()).append(' ').append(parameter.getName());
                if (i < methode.getParameters().size() - 1) {
                    builder.append(", ");
                }
            }

            builder.append(");\n");
        }

        builder.append("}\n");
    }

    private void appendClassRelations(StringBuilder builder, List<JavaRelation> relations) {
        List<String> parents = targetsByType(relations, "héritage");
        List<String> interfaces = targetsByType(relations, "implémentation");

        if (!parents.isEmpty()) {
            builder.append(" extends ").append(parents.get(0));
        }
        if (!interfaces.isEmpty()) {
            builder.append(" implements ").append(join(interfaces));
        }
    }

    private void appendRelationAttributes(StringBuilder builder, JavaClass javaClass, List<JavaRelation> relations) {
        if (relations == null) {
            return;
        }

        for (JavaRelation relation : relations) {
            if (!isStructuralAttributeRelation(relation.getType())) {
                continue;
            }
            if (hasAttribute(javaClass, relation.getTarget())) {
                continue;
            }

            builder.append("    private ")
                .append(relation.getTarget())
                .append(' ')
                .append(lowerFirst(relation.getTarget()))
                .append(";\n");
        }
    }

    private boolean isStructuralAttributeRelation(String type) {
        return "association".equals(type)
            || "agrégation".equals(type)
            || "composition".equals(type);
    }

    private boolean hasAttribute(JavaClass javaClass, String targetType) {
        for (JavaAttribut attribut : javaClass.getAttributes()) {
            if (targetType.equals(attribut.getType())) {
                return true;
            }
        }
        return false;
    }

    private String lowerFirst(String value) {
        if (value == null || value.isEmpty()) {
            return "value";
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private void appendInterfaceRelations(StringBuilder builder, List<JavaRelation> relations) {
        List<String> parents = targetsByType(relations, "héritage");
        if (!parents.isEmpty()) {
            builder.append(" extends ").append(join(parents));
        }
    }

    private List<String> targetsByType(List<JavaRelation> relations, String type) {
        List<String> targets = new ArrayList<String>();
        if (relations == null) {
            return targets;
        }

        for (JavaRelation relation : relations) {
            if (type.equals(relation.getType()) && !targets.contains(relation.getTarget())) {
                targets.add(relation.getTarget());
            }
        }
        return targets;
    }

    private String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private void parseMembers(String body, JavaClass javaClass) {
        String[] lines = body.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher matcher = MEMBER_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            Visibility visibility = visibility(matcher.group(1));
            String name = matcher.group(2);
            String params = matcher.group(3);
            String returnType = matcher.group(4);
            String attributeType = matcher.group(5);

            if (attributeType != null) {
                javaClass.addAttribute(new JavaAttribut(name, attributeType, visibility));
            } else {
                JavaMethode methode = new JavaMethode(name, returnType == null ? "" : returnType, visibility);
                parseParameters(params, methode);
                javaClass.addMethod(methode);
            }
        }
    }

    private void parseParameters(String params, JavaMethode methode) {
        if (params == null || params.trim().isEmpty()) {
            return;
        }

        String[] parts = params.split(",");
        for (int i = 0; i < parts.length; i++) {
            String[] tokens = parts[i].trim().split("\\s*:\\s*");
            if (tokens.length == 2 && !tokens[0].isEmpty() && !tokens[1].isEmpty()) {
                methode.addParameter(new JavaParamettre(tokens[0].trim(), tokens[1].trim()));
            }
        }
    }

    private void appendMethod(StringBuilder builder, JavaClass javaClass, JavaMethode methode) {
        boolean constructor = methode.getName().equals(javaClass.getName())
            || methode.getReturnType() == null
            || methode.getReturnType().isEmpty();

        builder.append("    ").append(keyword(methode.getVisibility())).append(' ');
        if (!constructor) {
            builder.append(methode.getReturnType()).append(' ');
        }
        builder.append(methode.getName()).append('(');

        for (int i = 0; i < methode.getParameters().size(); i++) {
            JavaParamettre parameter = methode.getParameters().get(i);
            builder.append(parameter.getType()).append(' ').append(parameter.getName());
            if (i < methode.getParameters().size() - 1) {
                builder.append(", ");
            }
        }

        builder.append(") {\n");
        if (!constructor && !"void".equals(methode.getReturnType())) {
            builder.append("        return ").append(defaultValue(methode.getReturnType())).append(";\n");
        }
        builder.append("    }\n\n");
    }

    private String keyword(Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return "public";
            case PRIVATE:
                return "private";
            case PROTECTED:
                return "protected";
            default:
                return "";
        }
    }

    private Visibility visibility(String symbol) {
        if ("+".equals(symbol)) {
            return Visibility.PUBLIC;
        }
        if ("-".equals(symbol)) {
            return Visibility.PRIVATE;
        }
        if ("#".equals(symbol)) {
            return Visibility.PROTECTED;
        }
        return Visibility.PUBLIC;
    }

    private String defaultValue(String type) {
        if ("boolean".equals(type)) {
            return "false";
        }
        if ("byte".equals(type) || "short".equals(type) || "int".equals(type) || "long".equals(type)) {
            return "0";
        }
        if ("float".equals(type)) {
            return "0.0f";
        }
        if ("double".equals(type)) {
            return "0.0";
        }
        if ("char".equals(type)) {
            return "'\\0'";
        }
        return "null";
    }
}
