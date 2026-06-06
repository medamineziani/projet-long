package fr.inptoulouse.sn.ide7.models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectStore {
    private final Path dataFile;

    public ProjectStore() {
        this(Paths.get(System.getProperty("user.home"), ".ide7", "projects.json"));
    }

    public ProjectStore(Path dataFile) {
        this.dataFile = dataFile;
    }

    public List<ProjectEntry> loadProjects() {
        List<ProjectEntry> projects = new ArrayList<ProjectEntry>();
        if (!Files.exists(dataFile)) {
            return projects;
        }

        try {
            String json = new String(Files.readAllBytes(dataFile), StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*,\\s*\"path\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"\\s*\\}");
            Matcher matcher = pattern.matcher(json);
            while (matcher.find()) {
                String name = unescape(matcher.group(1));
                String path = unescape(matcher.group(2));
                projects.add(new ProjectEntry(name, Paths.get(path).toAbsolutePath().normalize()));
            }
        } catch (IOException ignored) {
            return new ArrayList<ProjectEntry>();
        }

        return projects;
    }

    public void saveProjects(List<ProjectEntry> projects) throws IOException {
        Path parent = dataFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"projects\": [\n");
        for (int i = 0; i < projects.size(); i++) {
            ProjectEntry project = projects.get(i);
            builder.append("    {\"name\":\"")
                    .append(escape(project.getName()))
                    .append("\",\"path\":\"")
                    .append(escape(project.getPath().toString()))
                    .append("\"}");
            if (i < projects.size() - 1) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("  ]\n");
        builder.append("}\n");

        Files.write(dataFile, builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescape(String value) {
        StringBuilder out = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaping) {
                if (c == 'n') {
                    out.append('\n');
                } else if (c == 'r') {
                    out.append('\r');
                } else if (c == 't') {
                    out.append('\t');
                } else {
                    out.append(c);
                }
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else {
                out.append(c);
            }
        }
        if (escaping) {
            out.append('\\');
        }
        return out.toString();
    }
}
