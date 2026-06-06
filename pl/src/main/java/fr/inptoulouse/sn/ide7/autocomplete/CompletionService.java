package fr.inptoulouse.sn.ide7.autocomplete;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class CompletionService {
    private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
        "null", "record", "var"
    ));
    private static final String SPLIT_REGEX = "[^A-Za-z0-9_]+";

    private final Map<String, Integer> frequencies;

    public CompletionService() {
        this.frequencies = new HashMap<String, Integer>();
        for (String keyword : JAVA_KEYWORDS) {
            frequencies.put(keyword, 10);
        }
    }

    public void indexProject(Path projectPath) {
        if (projectPath == null || !Files.isDirectory(projectPath)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(projectPath)) {
            java.util.Iterator<Path> iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                if (Files.isRegularFile(path) && path.toString().endsWith(".java")) {
                    indexFile(path);
                }
            }
        } catch (IOException e) {
            // Autocompletion should not block the editor when indexing fails.
        }
    }

    public void indexText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        String[] tokens = text.split(SPLIT_REGEX);
        for (String token : tokens) {
            if (isCandidate(token)) {
                frequencies.put(token, frequencies.getOrDefault(token, 0) + 1);
            }
        }
    }

    public List<String> suggestions(String prefix, int limit) {
        if (prefix == null || prefix.trim().isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        String normalizedPrefix = prefix.trim();
        List<Map.Entry<String, Integer>> matches = new ArrayList<Map.Entry<String, Integer>>();
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            if (entry.getKey().startsWith(normalizedPrefix) && !entry.getKey().equals(normalizedPrefix)) {
                matches.add(entry);
            }
        }

        matches.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> left, Map.Entry<String, Integer> right) {
                int byFrequency = Integer.compare(right.getValue(), left.getValue());
                if (byFrequency != 0) {
                    return byFrequency;
                }
                return left.getKey().compareTo(right.getKey());
            }
        });

        List<String> result = snippetSuggestions(normalizedPrefix);
        for (int i = 0; i < matches.size() && i < limit; i++) {
            String value = matches.get(i).getKey();
            if (!result.contains(value)) {
                result.add(value);
            }
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    public boolean isSnippet(String completion) {
        return "if".equals(completion)
            || "if else".equals(completion)
            || "else".equals(completion)
            || "while".equals(completion)
            || "for".equals(completion)
            || "try catch".equals(completion)
            || "switch".equals(completion);
    }

    public String snippet(String completion, String indent) {
        String i = indent == null ? "" : indent;
        if ("if".equals(completion)) {
            return "if () {\n" + i + "    \n" + i + "}";
        }
        if ("if else".equals(completion)) {
            return "if () {\n" + i + "    \n" + i + "} else {\n" + i + "    \n" + i + "}";
        }
        if ("else".equals(completion)) {
            return "else {\n" + i + "    \n" + i + "}";
        }
        if ("while".equals(completion)) {
            return "while () {\n" + i + "    \n" + i + "}";
        }
        if ("for".equals(completion)) {
            return "for (int i = 0; i < ; i++) {\n" + i + "    \n" + i + "}";
        }
        if ("try catch".equals(completion)) {
            return "try {\n" + i + "    \n" + i + "} catch (Exception e) {\n" + i + "    e.printStackTrace();\n" + i + "}";
        }
        if ("switch".equals(completion)) {
            return "switch () {\n" + i + "    case :\n" + i + "        break;\n" + i + "    default:\n" + i + "        break;\n" + i + "}";
        }
        return completion;
    }

    private List<String> snippetSuggestions(String prefix) {
        List<String> snippets = new ArrayList<String>();
        addSnippetIfMatches(snippets, "if", prefix);
        addSnippetIfMatches(snippets, "if else", prefix);
        addSnippetIfMatches(snippets, "else", prefix);
        addSnippetIfMatches(snippets, "while", prefix);
        addSnippetIfMatches(snippets, "for", prefix);
        addSnippetIfMatches(snippets, "try catch", prefix);
        addSnippetIfMatches(snippets, "switch", prefix);
        return snippets;
    }

    private void addSnippetIfMatches(List<String> snippets, String snippet, String prefix) {
        if (snippet.startsWith(prefix) && !snippets.contains(snippet)) {
            snippets.add(snippet);
        }
    }

    public List<String> memberSuggestions(String type, int limit) {
        if (type == null || type.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> suggestions = new ArrayList<String>();
        if ("String".equals(type)) {
            suggestions.add("toLowerCase()");
            suggestions.add("toUpperCase()");
            suggestions.add("trim()");
            suggestions.add("length()");
            suggestions.add("isEmpty()");
            suggestions.add("charAt()");
            suggestions.add("substring()");
            suggestions.add("contains()");
            suggestions.add("startsWith()");
            suggestions.add("endsWith()");
            suggestions.add("replace()");
            suggestions.add("split()");
            suggestions.add("equals()");
            suggestions.add("equalsIgnoreCase()");
        } else if ("List".equals(type) || type.endsWith("List") || "ArrayList".equals(type)) {
            suggestions.add("add()");
            suggestions.add("get()");
            suggestions.add("size()");
            suggestions.add("isEmpty()");
            suggestions.add("remove()");
            suggestions.add("clear()");
            suggestions.add("contains()");
        } else if ("Map".equals(type) || type.endsWith("Map") || "HashMap".equals(type)) {
            suggestions.add("put()");
            suggestions.add("get()");
            suggestions.add("containsKey()");
            suggestions.add("remove()");
            suggestions.add("keySet()");
            suggestions.add("values()");
            suggestions.add("size()");
        } else {
            suggestions.add("toString()");
            suggestions.add("equals()");
            suggestions.add("hashCode()");
        }

        if (suggestions.size() <= limit) {
            return suggestions;
        }
        return new ArrayList<String>(suggestions.subList(0, limit));
    }

    private void indexFile(Path path) {
        try {
            indexText(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        } catch (IOException e) {
            // Ignore unreadable files.
        }
    }

    private boolean isCandidate(String token) {
        if (token == null || token.length() < 2) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(token.charAt(0))) {
            return false;
        }
        for (int i = 1; i < token.length(); i++) {
            if (!Character.isJavaIdentifierPart(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
