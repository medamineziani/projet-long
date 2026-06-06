package fr.inptoulouse.sn.ide7.uml;

/**
 * Représente un paramètre d'une méthode Java dans le modèle UML.
 *
 * Cette classe permet de décrire les caractéristiques d'un paramètre :
 * - son nom
 * - son type
 *
 * Elle est utilisée dans la classe JavaMethode pour représenter
 * les paramètres d'une méthode dans un diagramme UML.
 */
public class JavaParamettre {

    /**
     * Nom du paramètre.
     */
    private String name;

    /**
     * Type du paramètre (ex : int, String, etc.).
     */
    private String type;

    /**
     * Constructeur de la classe JavaParamettre.
     * @param name nom du paramètre
     * @param type type du paramètre
     */
    public JavaParamettre(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Retourne le nom du paramètre.
     * @return le nom du paramètre
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne le type du paramètre.
     * @return le type du paramètre
     */
    public String getType() {
        return type;
    }
}
