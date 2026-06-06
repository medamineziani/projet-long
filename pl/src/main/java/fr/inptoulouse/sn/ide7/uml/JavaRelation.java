package fr.inptoulouse.sn.ide7.uml;

/**
 * Représente une relation UML entre deux classes Java.
 *
 * Une relation permet de montrer le lien entre une classe source
 * et une classe cible.
 *
 * Exemples :
 * - Héritage : class Etudiant extends Personne
 * - Implémentation : class Etudiant implements Comparable
 * - Association : une classe possède un attribut d'un autre type
 * - Dépendance : une méthode utilise une autre classe en paramètre
 */
public class JavaRelation {

    private String source;
    private String target;
    private String type;

    /**
     * Constructeur d'une relation UML.
     *
     * @param source classe de départ de la relation
     * @param target classe d'arrivée de la relation
     * @param type type de relation UML
     */
    public JavaRelation(String source, String target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    /**
     * Retourne la classe source.
     *
     * @return nom de la classe source
     */
    public String getSource() {
        return source;
    }

    /**
     * Retourne la classe cible.
     *
     * @return nom de la classe cible
     */
    public String getTarget() {
        return target;
    }

    /**
     * Retourne le type de relation.
     *
     * @return type de relation UML
     */
    public String getType() {
        return type;
    }

    /**
     * Retourne une représentation textuelle de la relation.
     *
     * @return relation sous forme de texte
     */
    @Override
    public String toString() {
        return source + " --(" + type + ")--> " + target;
    }
}
