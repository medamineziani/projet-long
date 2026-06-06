package fr.inptoulouse.sn.ide7.uml;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente le point d'entrée du programme UML.
 *
 * Cette classe permet de lancer l'application en ligne de commande.
 * Elle lit un ou plusieurs fichiers Java fournis par l'utilisateur,
 * analyse leur contenu et affiche le diagramme UML correspondant
 * dans le terminal.
 *
 * Elle utilise :
 * - JavaParser pour analyser le code source
 * - UMLPrinter pour afficher le diagramme UML
 */
public class Main {

    /**
     * Méthode principale du programme.
     *
     * @param args arguments de la ligne de commande (noms des fichiers Java)
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Usage : java Main <fichier1.java> <fichier2.java> ...");
                return;
            }

            List<JavaClass> classes = new ArrayList<>();

            JavaParser parser = new JavaParser();

            for (String filePath : args) {
                String source = Files.readString(Paths.get(filePath));
                JavaClass javaClass = parser.parse(source);
                classes.add(javaClass);
            }

            UMLPrinter printer = new UMLPrinter();
            printer.print(classes);

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
