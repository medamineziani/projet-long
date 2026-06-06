package fr.inptoulouse.sn.ide7.views;

import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SyntaxHighlightLayer extends TextFlow {
    private static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
        "null", "exports", "module", "non-sealed", "open", "opens", "permits", "provides",
        "record", "requires", "sealed", "to", "transitive", "uses", "var", "with", "yield"
    ));

    public SyntaxHighlightLayer() {
        setStyle(
            "-fx-font-family: 'JetBrains Mono', 'Menlo', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10 12 10 12;"
        );
        setMouseTransparent(true);
    }

    public void setText(String text) {
        getChildren().clear();
        if (text == null || text.isEmpty()) {
            getChildren().add(styledText("", "#0f172a"));
            return;
        }

        int index = 0;
        while (index < text.length()) {
            char current = text.charAt(index);

            if (current == '/' && index + 1 < text.length() && text.charAt(index + 1) == '/') {
                int end = findLineEnd(text, index + 2);
                append(text.substring(index, end), "#64748b");
                index = end;
                continue;
            }

            if (current == '/' && index + 1 < text.length() && text.charAt(index + 1) == '*') {
                int end = findBlockCommentEnd(text, index + 2);
                append(text.substring(index, end), "#64748b");
                index = end;
                continue;
            }

            if (current == '"') {
                int end = findStringEnd(text, index + 1, '"');
                append(text.substring(index, end), "#16a34a");
                index = end;
                continue;
            }

            if (current == '\'') {
                int end = findStringEnd(text, index + 1, '\'');
                append(text.substring(index, end), "#16a34a");
                index = end;
                continue;
            }

            if (current == '@') {
                int end = index + 1;
                while (end < text.length() && isIdentifierPart(text.charAt(end))) {
                    end++;
                }
                append(text.substring(index, end), "#b45309");
                index = end;
                continue;
            }

            if (isIdentifierStart(current)) {
                int end = index + 1;
                while (end < text.length() && isIdentifierPart(text.charAt(end))) {
                    end++;
                }
                String token = text.substring(index, end);
                append(token, KEYWORDS.contains(token) ? "#1d4ed8" : "#0f172a");
                index = end;
                continue;
            }

            if (Character.isDigit(current)) {
                int end = index + 1;
                while (end < text.length() && isNumberPart(text.charAt(end))) {
                    end++;
                }
                append(text.substring(index, end), "#9333ea");
                index = end;
                continue;
            }

            append(String.valueOf(current), "#0f172a");
            index++;
        }
    }

    private void append(String token, String color) {
        getChildren().add(styledText(token, color));
    }

    private Text styledText(String value, String color) {
        Text text = new Text(value);
        text.setFill(javafx.scene.paint.Color.web(color));
        return text;
    }

    private int findLineEnd(String text, int index) {
        int end = index;
        while (end < text.length() && text.charAt(end) != '\n') {
            end++;
        }
        return end;
    }

    private int findBlockCommentEnd(String text, int index) {
        int end = index;
        while (end + 1 < text.length()) {
            if (text.charAt(end) == '*' && text.charAt(end + 1) == '/') {
                return end + 2;
            }
            end++;
        }
        return text.length();
    }

    private int findStringEnd(String text, int index, char quote) {
        int end = index;
        boolean escaped = false;
        while (end < text.length()) {
            char current = text.charAt(end);
            if (!escaped && current == quote) {
                return end + 1;
            }
            escaped = !escaped && current == '\\';
            if (current != '\\') {
                escaped = false;
            }
            end++;
        }
        return text.length();
    }

    private boolean isIdentifierStart(char value) {
        return Character.isJavaIdentifierStart(value);
    }

    private boolean isIdentifierPart(char value) {
        return Character.isJavaIdentifierPart(value) || value == '-';
    }

    private boolean isNumberPart(char value) {
        return Character.isDigit(value)
            || value == '.'
            || value == '_'
            || value == 'x'
            || value == 'X'
            || value == 'b'
            || value == 'B'
            || value == 'l'
            || value == 'L'
            || value == 'f'
            || value == 'F'
            || value == 'd'
            || value == 'D';
    }
}
