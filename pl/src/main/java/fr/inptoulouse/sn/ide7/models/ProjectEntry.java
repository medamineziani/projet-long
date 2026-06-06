package fr.inptoulouse.sn.ide7.models;

import java.nio.file.Path;

public class ProjectEntry {
    private final String name;
    private final Path path;

    public ProjectEntry(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProjectEntry)) {
            return false;
        }
        ProjectEntry other = (ProjectEntry) obj;
        return path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
