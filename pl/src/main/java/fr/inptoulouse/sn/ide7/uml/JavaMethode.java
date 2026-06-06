package fr.inptoulouse.sn.ide7.uml;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une méthode d'une classe Java dans le modèle UML.
 *
 * Cette classe permet de décrire les caractéristiques d'une méthode :
 * - son nom
 * - son type de retour
 * - sa visibilité
 * - la liste de ses paramètres
 *
 * Elle est utilisée dans la classe JavaClass pour représenter
 * les méthodes d'une classe dans un diagramme UML.
 */
public class JavaMethode {

    /**
     * Nom de la méthode.
     */
    private String name;

    /**
     * Type de retour de la méthode (ex : void, int, String, etc.).
     */
    private String returnType;

    /**
     * Visibilité de la méthode.
     */
    private Visibility visibility;

    /**
     * Liste des paramètres de la méthode.
     */
    private List<JavaParamettre> parameters;

    /**
     * Constructeur de la classe JavaMethode.
     * @param name nom de la méthode
     * @param returnType type de retour de la méthode
     * @param visibility visibilité de la méthode
     */
    public JavaMethode(String name, String returnType, Visibility visibility) {
        this.name = name;
        this.returnType = returnType;
        this.visibility = visibility;
        this.parameters = new ArrayList<>();
    }

    /**
     * Retourne le nom de la méthode.
     * @return le nom de la méthode
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne le type de retour de la méthode.
     * @return le type de retour
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Retourne la visibilité de la méthode.
     * @return la visibilité
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Retourne la liste des paramètres de la méthode.
     * @return la liste des paramètres
     */
    public List<JavaParamettre> getParameters() {
        return parameters;
    }

    /**
     * Ajoute un paramètre à la méthode.
     * 
     * @param parameter paramètre à ajouter
     */
    public void addParameter(JavaParamettre parameter) {
        parameters.add(parameter);
    }
}
		
