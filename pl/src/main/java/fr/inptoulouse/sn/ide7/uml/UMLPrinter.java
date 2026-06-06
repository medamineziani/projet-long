package fr.inptoulouse.sn.ide7.uml;

import java.util.List;

/**
 * Représente un générateur d'affichage UML dans le terminal.
 *
 * Cette classe permet d'afficher une représentation textuelle
 * d'une classe Java sous forme de diagramme UML simplifié.
 *
 * Elle affiche :
 * - le nom de la classe
 * - les attributs avec leur visibilité
 * - les méthodes avec leurs paramètres et type de retour
 * - les relations UML détectées
 */
public class UMLPrinter {

    /**
     * Affiche plusieurs classes UML dans le terminal.
     *
     * @param classes classes à afficher
     */
    public void print(List<JavaClass> classes) {
        for (JavaClass javaClass : classes) {
            print(javaClass);
            System.out.println();
        }
    }

    /**
     * Affiche une classe UML dans le terminal.
     *
     * @param javaClass classe à afficher
     */
    public void print(JavaClass javaClass) {
        System.out.println("+------------------------------------------+");
        System.out.printf("| %-40s |\n", javaClass.getName());
        System.out.println("+------------------------------------------+");

        for (JavaAttribut attribut : javaClass.getAttributes()) {
            System.out.printf("| %-40s |\n", formatAttribut(attribut));
        }

        System.out.println("+------------------------------------------+");

        for (JavaMethode methode : javaClass.getMethods()) {
            System.out.printf("| %-40s |\n", formatMethode(methode));
        }

        System.out.println("+------------------------------------------+");

        printRelations(javaClass);
    }

    /**
     * Affiche les relations UML de la classe.
     *
     * @param javaClass classe contenant les relations
     */
    private void printRelations(JavaClass javaClass) {
        if (javaClass.getRelations().isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println("Relations UML :");

        for (JavaRelation relation : javaClass.getRelations()) {
            System.out.println(
                "- " + relation.getSource()
                + " --(" + relation.getType() + ")--> "
                + relation.getTarget()
            );
        }
    }

    /**
     * Formate un attribut pour l'affichage UML.
     *
     * @param attribut attribut à formater
     * @return chaîne formatée
     */
    private String formatAttribut(JavaAttribut attribut) {
        return symbol(attribut.getVisibility()) + " "
            + attribut.getName() + " : " + attribut.getType();
    }

    /**
     * Formate une méthode pour l'affichage UML.
     *
     * @param methode méthode à formater
     * @return chaîne formatée
     */
    private String formatMethode(JavaMethode methode) {
        StringBuilder sb = new StringBuilder();

        sb.append(symbol(methode.getVisibility())).append(" ");
        sb.append(methode.getName()).append("(");

        for (int i = 0; i < methode.getParameters().size(); i++) {
            JavaParamettre p = methode.getParameters().get(i);
            sb.append(p.getName()).append(" : ").append(p.getType());

            if (i < methode.getParameters().size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(")");

        if (methode.getReturnType() != null && !methode.getReturnType().isEmpty()) {
            sb.append(" : ").append(methode.getReturnType());
        }

        return sb.toString();
    }

    /**
     * Convertit une visibilité en symbole UML.
     *
     * @param visibility visibilité Java
     * @return symbole UML correspondant
     */
    private String symbol(Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return "+";
            case PRIVATE:
                return "-";
            case PROTECTED:
                return "#";
            default:
                return "?";
        }
    }
}
