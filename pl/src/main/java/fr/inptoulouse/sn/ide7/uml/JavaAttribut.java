package fr.inptoulouse.sn.ide7.uml;

/**
 * Représente un attribut d'une classe Java dans le modèle UML.
 * 
 * Cette classe permet de décrire les caractéristiques d'un attribut :
 * - son nom
 * - son type
 * - sa visibilité (public, private, etc.)
 * 
 * Elle est utilisée dans la classe JavaClass pour représenter
 * les attributs d'une classe dans un diagramme UML.
 */
public class JavaAttribut {

    /**
     * Nom de l'attribut.
     */
    private String name;

    /**
     * Type de l'attribut (ex : int, String, etc.).
     */
    private String type;

    /**
     * Visibilité de l'attribut.
     */
    private Visibility visibility;

    /**
     * Constructeur de la classe JavaAttribute.
     * @param name nom de l'attribut
     * @param type type de l'attribut
     * @param visibility visibilité de l'attribut
     */
    public JavaAttribut(String name, String type, Visibility visibility) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
    }

    /**
     * Retourne le nom de l'attribut.
     * @return le nom de l'attribut
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne le type de l'attribut.
     * @return le type de l'attribut
     */
    public String getType() {
        return type;
    }

    /**
     * Retourne la visibilité de l'attribut.
     * @return la visibilité de l'attribut
     */
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return visibility + " " + name + " : " + type;
    }
}
