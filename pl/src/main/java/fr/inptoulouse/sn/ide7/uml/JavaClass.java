package fr.inptoulouse.sn.ide7.uml;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une classe Java dans le modèle UML.
 *
 * Cette classe stocke :
 * - le nom de la classe
 * - ses attributs
 * - ses méthodes
 * - ses relations UML
 *
 * Elle sert de structure principale pour représenter
 * une classe analysée par JavaParser.
 */
public class JavaClass {

    /**
     * Nom de la classe Java.
     */
    private String name;

    /**
     * Type Java detecte : class, interface ou enum.
     */
    private String kind;

    /**
     * Liste des attributs de la classe.
     */
    private List<JavaAttribut> attributes;

    /**
     * Liste des méthodes de la classe.
     */
    private List<JavaMethode> methods;

    /**
     * Liste des relations UML de la classe.
     */
    private List<JavaRelation> relations;

    /**
     * Constructeur de la classe JavaClass.
     *
     * @param name nom de la classe Java
     */
    public JavaClass(String name) {
        this.name = name;
        this.kind = "class";
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    /**
     * Retourne le nom de la classe.
     *
     * @return nom de la classe
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne le type Java detecte.
     *
     * @return class, interface ou enum
     */
    public String getKind() {
        return kind;
    }

    /**
     * Definit le type Java detecte.
     *
     * @param kind class, interface ou enum
     */
    public void setKind(String kind) {
        if (kind == null || kind.trim().isEmpty()) {
            this.kind = "class";
            return;
        }
        this.kind = kind;
    }

    /**
     * Retourne la liste des attributs.
     *
     * @return liste des attributs
     */
    public List<JavaAttribut> getAttributes() {
        return attributes;
    }

    /**
     * Retourne la liste des méthodes.
     *
     * @return liste des méthodes
     */
    public List<JavaMethode> getMethods() {
        return methods;
    }

    /**
     * Retourne la liste des relations UML.
     *
     * @return liste des relations
     */
    public List<JavaRelation> getRelations() {
        return relations;
    }

    /**
     * Ajoute un attribut à la classe.
     *
     * @param attribute attribut à ajouter
     */
    public void addAttribute(JavaAttribut attribute) {
        attributes.add(attribute);
    }

    /**
     * Ajoute une méthode à la classe.
     *
     * @param method méthode à ajouter
     */
    public void addMethod(JavaMethode method) {
        methods.add(method);
    }

    /**
     * Ajoute une relation UML à la classe.
     *
     * @param relation relation à ajouter
     */
    public void addRelation(JavaRelation relation) {
        relations.add(relation);
    }
}
