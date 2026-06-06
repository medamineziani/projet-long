package fr.inptoulouse.sn.ide7.uml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Représente un analyseur de code Java permettant de générer
 * un modèle UML à partir d'un fichier source Java.
 *
 * Cette classe extrait :
 * - le nom de la classe
 * - les attributs
 * - les méthodes
 * - les constructeurs
 * - les paramètres
 * - les relations UML simples
 *
 * Dans cette itération, l'héritage avec le mot-clé extends
 * est également détecté.
 */
public class JavaParser {

    /**
     * Analyse un code source Java et construit une représentation UML.
     *
     * @param source code source Java sous forme de texte
     * @return objet JavaClass représentant la classe analysée
     */
    public JavaClass parse(String source) {
        List<JavaClass> classes = parseAll(source);
        if (classes.isEmpty()) {
            throw new IllegalArgumentException(
                "Aucune classe trouvée dans le code source."
            );
        }
        return classes.get(0);
    }

    /**
     * Analyse un code source Java et construit une representation UML
     * pour chaque classe detectee dans le fichier.
     *
     * @param source code source Java sous forme de texte
     * @return liste des classes detectees
     */
    public List<JavaClass> parseAll(String source) {
        List<JavaClass> classes = new ArrayList<JavaClass>();

        Pattern classPattern = Pattern.compile("\\b(class|interface|enum)\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(source);

        while (matcher.find()) {
            String kind = matcher.group(1);
            String className = matcher.group(2);
            String classSource = extractClassSource(source, matcher.start());

            JavaClass javaClass = new JavaClass(className);
            javaClass.setKind(kind);
            parseAttributes(classSource, javaClass);
            parseMethodsAndConstructors(classSource, javaClass);
            parseRelations(classSource, javaClass);

            classes.add(javaClass);
        }

        return classes;
    }

    /**
     * Extrait le nom de la classe principale.
     *
     * Exemple :
     * public class PointNomme extends Point
     *
     * Résultat :
     * PointNomme
     *
     * @param source code source Java
     * @return nom de la classe détectée
     */
    private String extractClassName(String source) {

        Pattern pattern =
            Pattern.compile("\\bclass\\s+(\\w+)");

        Matcher matcher = pattern.matcher(source);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new IllegalArgumentException(
            "Aucune classe trouvée dans le code source."
        );
    }

    private String extractClassSource(String source, int classStart) {
        int openingBrace = source.indexOf('{', classStart);
        if (openingBrace < 0) {
            return source.substring(classStart);
        }

        int depth = 0;
        for (int i = openingBrace; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(classStart, i + 1);
                }
            }
        }

        return source.substring(classStart);
    }

    /**
     * Analyse les attributs de la classe.
     *
     * Exemple détecté :
     * private String nom;
     * protected double x;
     *
     * @param source code source Java
     * @param javaClass classe UML à compléter
     */
    private void parseAttributes(
        String source,
        JavaClass javaClass
    ) {

        Pattern pattern = Pattern.compile(
            "(public|private|protected)\\s+" +
            "([\\w<>\\[\\]]+)\\s+" +
            "(\\w+)\\s*;"
        );

        Matcher matcher = pattern.matcher(source);

        while (matcher.find()) {

            Visibility visibility =
                parseVisibility(matcher.group(1));

            String type = matcher.group(2);

            String name = matcher.group(3);

            JavaAttribut attribut =
                new JavaAttribut(name, type, visibility);

            javaClass.addAttribute(attribut);
        }
    }

    /**
     * Analyse les méthodes et les constructeurs.
     *
     * Les méthodes possèdent un type de retour :
     * public String getNom()
     *
     * Les constructeurs sont détectés séparément
     * car ils n'ont pas de type de retour.
     *
     * @param source code source Java
     * @param javaClass classe UML à compléter
     */
    private void parseMethodsAndConstructors(
        String source,
        JavaClass javaClass
    ) {

        String className = javaClass.getName();

        Pattern methodPattern = Pattern.compile(
            "(?:(public|private|protected)\\s+)?" +
            "([\\w<>\\[\\]]+)\\s+" +
            "(\\w+)\\s*" +
            "\\(([^)]*)\\)\\s*(?:\\{|;)"
        );

        Matcher methodMatcher =
            methodPattern.matcher(source);

        while (methodMatcher.find()) {

            Visibility visibility =
                parseVisibilityOrDefault(methodMatcher.group(1), Visibility.PUBLIC);

            String returnType =
                methodMatcher.group(2);

            String methodName =
                methodMatcher.group(3);

            if (isVisibilityKeyword(returnType)) {
                continue;
            }

            String params =
                methodMatcher.group(4);

            JavaMethode methode =
                new JavaMethode(
                    methodName,
                    returnType,
                    visibility
                );

            parseParameters(params, methode);

            javaClass.addMethod(methode);
        }

        Pattern constructorPattern = Pattern.compile(
            "(public|private|protected)\\s+" +
            className +
            "\\s*\\(([^)]*)\\)\\s*\\{"
        );

        Matcher constructorMatcher =
            constructorPattern.matcher(source);

        while (constructorMatcher.find()) {

            Visibility visibility =
                parseVisibility(
                    constructorMatcher.group(1)
                );

            String params =
                constructorMatcher.group(2);

            JavaMethode constructeur =
                new JavaMethode(
                    className,
                    "",
                    visibility
                );

            parseParameters(params, constructeur);

            javaClass.addMethod(constructeur);
        }
    }

    /**
     * Analyse les paramètres d'une méthode
     * ou d'un constructeur.
     *
     * Exemple :
     * double x, double y, String nom
     *
     * @param params paramètres sous forme de texte
     * @param methode méthode à compléter
     */
    private void parseParameters(
        String params,
        JavaMethode methode
    ) {

        if (params == null || params.trim().isEmpty()) {
            return;
        }

        String[] parts = params.split(",");

        for (String part : parts) {

            String p = part.trim();

            String[] tokens = p.split("\\s+");

            if (tokens.length >= 2) {

                String type = tokens[0];

                String name = tokens[1];

                methode.addParameter(
                    new JavaParamettre(name, type)
                );
            }
        }
    }

    /**
     * Analyse les relations UML de la classe.
     *
     * Dans cette itération :
     * - détection de l'héritage avec extends
     *
     * @param source code source Java
     * @param javaClass classe UML à compléter
     */
    private void parseRelations(
        String source,
        JavaClass javaClass
    ) {

        parseInheritance(source, javaClass);
        parseInterfaces(source, javaClass);
        parseAssociations(javaClass);
        parseDependencies(javaClass);
    }

    /**
     * Analyse une relation d'héritage.
     *
     * Exemple :
     * class PointNomme extends Point
     *
     * Relation créée :
     * PointNomme --(héritage)--> Point
     *
     * @param source code source Java
     * @param javaClass classe UML à compléter
     */
    private void parseInheritance(
        String source,
        JavaClass javaClass
    ) {

        Pattern pattern = Pattern.compile(
            "\\b(class|interface)\\s+\\w+\\s+extends\\s+([\\w\\s,]+?)(?:\\s+implements\\b|\\s*\\{|$)"
        );

        Matcher matcher = pattern.matcher(source);

        if (matcher.find()) {

            String[] parentClasses =
                matcher.group(2).split(",");

            for (int i = 0; i < parentClasses.length; i++) {
                String parentClass = parentClasses[i].trim();
                if (parentClass.isEmpty()) {
                    continue;
                }

                addRelationIfAbsent(
                    javaClass,
                    javaClass.getName(),
                    parentClass,
                    "héritage"
                );
            }
        }
    }

    /**
     * Analyse les relations d'implementation d'interfaces.
     *
     * Exemple :
     * class Service implements Runnable, AutoCloseable
     */
    private void parseInterfaces(
        String source,
        JavaClass javaClass
    ) {

        Pattern pattern = Pattern.compile(
            "\\bclass\\s+\\w+(?:\\s+extends\\s+\\w+)?\\s+implements\\s+([\\w\\s,]+?)(?:\\s*\\{|$)"
        );

        Matcher matcher = pattern.matcher(source);

        if (matcher.find()) {
            String[] interfaces = matcher.group(1).split(",");

            for (int i = 0; i < interfaces.length; i++) {
                String interfaceName = interfaces[i].trim();
                if (interfaceName.isEmpty()) {
                    continue;
                }

                addRelationIfAbsent(
                    javaClass,
                    javaClass.getName(),
                    interfaceName,
                    "implémentation"
                );
            }
        }
    }

    /**
     * Analyse les associations a partir des attributs.
     */
    private void parseAssociations(JavaClass javaClass) {
        for (JavaAttribut attribut : javaClass.getAttributes()) {
            List<String> types = extractRelationTypes(attribut.getType());
            for (String type : types) {
                addRelationIfAbsent(
                    javaClass,
                    javaClass.getName(),
                    type,
                    "association"
                );
            }
        }
    }

    /**
     * Analyse les dependances a partir des parametres et types de retour.
     */
    private void parseDependencies(JavaClass javaClass) {
        for (JavaMethode methode : javaClass.getMethods()) {
            for (JavaParamettre parameter : methode.getParameters()) {
                List<String> types = extractRelationTypes(parameter.getType());
                for (String type : types) {
                    addRelationIfAbsent(
                        javaClass,
                        javaClass.getName(),
                        type,
                        "dépendance"
                    );
                }
            }

            List<String> returnTypes = extractRelationTypes(methode.getReturnType());
            for (String type : returnTypes) {
                addRelationIfAbsent(
                    javaClass,
                    javaClass.getName(),
                    type,
                    "dépendance"
                );
            }
        }
    }

    private List<String> extractRelationTypes(String type) {
        List<String> types = new ArrayList<String>();
        if (type == null || type.trim().isEmpty()) {
            return types;
        }

        Matcher matcher = Pattern.compile("\\b[A-Z][A-Za-z0-9_]*\\b").matcher(type);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (isRelationCandidate(candidate) && !types.contains(candidate)) {
                types.add(candidate);
            }
        }
        return types;
    }

    private boolean isRelationCandidate(String type) {
        Set<String> ignoredTypes = new HashSet<String>();
        ignoredTypes.add("String");
        ignoredTypes.add("Object");
        ignoredTypes.add("Integer");
        ignoredTypes.add("Long");
        ignoredTypes.add("Double");
        ignoredTypes.add("Float");
        ignoredTypes.add("Boolean");
        ignoredTypes.add("Character");
        ignoredTypes.add("Byte");
        ignoredTypes.add("Short");
        ignoredTypes.add("Void");
        ignoredTypes.add("List");
        ignoredTypes.add("ArrayList");
        ignoredTypes.add("Set");
        ignoredTypes.add("HashSet");
        ignoredTypes.add("Map");
        ignoredTypes.add("HashMap");
        ignoredTypes.add("Collection");
        ignoredTypes.add("Optional");

        return !ignoredTypes.contains(type);
    }

    private void addRelationIfAbsent(
        JavaClass javaClass,
        String source,
        String target,
        String type
    ) {
        if (source.equals(target)) {
            return;
        }

        for (JavaRelation relation : javaClass.getRelations()) {
            if (relation.getSource().equals(source)
                && relation.getTarget().equals(target)
                && relation.getType().equals(type)) {
                return;
            }
        }

        javaClass.addRelation(new JavaRelation(source, target, type));
    }

    /**
     * Convertit une visibilité textuelle
     * en objet Visibility.
     *
     * @param value visibilité sous forme de texte
     * @return visibilité correspondante
     */
    private Visibility parseVisibility(String value) {

        switch (value) {

            case "public":
                return Visibility.PUBLIC;

            case "private":
                return Visibility.PRIVATE;

            case "protected":
                return Visibility.PROTECTED;

            default:
                throw new IllegalArgumentException(
                    "Visibilité inconnue : " + value
                );
        }
    }

    private Visibility parseVisibilityOrDefault(String value, Visibility defaultVisibility) {
        if (value == null || value.trim().isEmpty()) {
            return defaultVisibility;
        }
        return parseVisibility(value);
    }

    private boolean isVisibilityKeyword(String value) {
        return "public".equals(value)
            || "private".equals(value)
            || "protected".equals(value);
    }
}
