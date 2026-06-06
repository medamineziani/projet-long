package fr.inptoulouse.sn.ide7.uml;

import java.util.List;

/**
 * Transforme le modele UML en texte exploitable dans l'editeur.
 */
public class UMLFormatter {

    public String format(List<JavaClass> classes) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < classes.size(); i++) {
            builder.append(format(classes.get(i)));
            if (i < classes.size() - 1) {
                builder.append('\n');
            }
        }

        appendRelations(builder, classes);
        return builder.toString();
    }

    public String format(JavaClass javaClass) {
        StringBuilder builder = new StringBuilder();
        builder.append(javaClass.getKind()).append(' ').append(javaClass.getName()).append(" {\n");

        for (JavaAttribut attribut : javaClass.getAttributes()) {
            builder.append("    ")
                .append(symbol(attribut.getVisibility()))
                .append(' ')
                .append(attribut.getName())
                .append(" : ")
                .append(attribut.getType())
                .append('\n');
        }

        if (!javaClass.getAttributes().isEmpty() && !javaClass.getMethods().isEmpty()) {
            builder.append('\n');
        }

        for (JavaMethode methode : javaClass.getMethods()) {
            builder.append("    ")
                .append(symbol(methode.getVisibility()))
                .append(' ')
                .append(methode.getName())
                .append('(');

            for (int i = 0; i < methode.getParameters().size(); i++) {
                JavaParamettre parameter = methode.getParameters().get(i);
                builder.append(parameter.getName()).append(" : ").append(parameter.getType());
                if (i < methode.getParameters().size() - 1) {
                    builder.append(", ");
                }
            }

            builder.append(')');
            if (methode.getReturnType() != null && !methode.getReturnType().isEmpty()) {
                builder.append(" : ").append(methode.getReturnType());
            }
            builder.append('\n');
        }

        builder.append("}\n");
        return builder.toString();
    }

    private void appendRelations(StringBuilder builder, List<JavaClass> classes) {
        boolean hasRelations = false;
        for (JavaClass javaClass : classes) {
            if (!javaClass.getRelations().isEmpty()) {
                hasRelations = true;
                break;
            }
        }

        if (!hasRelations) {
            return;
        }

        builder.append('\n').append("relations {\n");
        for (JavaClass javaClass : classes) {
            for (JavaRelation relation : javaClass.getRelations()) {
                builder.append("    ")
                    .append(relation.getSource())
                    .append(" --(")
                    .append(relation.getType())
                    .append(")--> ")
                    .append(relation.getTarget())
                    .append('\n');
            }
        }
        builder.append("}\n");
    }

    private String symbol(Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return "+";
            case PRIVATE:
                return "-";
            case PROTECTED:
                return "#";
            default:
                return "~";
        }
    }
}
