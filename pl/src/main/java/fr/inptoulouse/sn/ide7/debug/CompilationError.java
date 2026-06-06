package fr.inptoulouse.sn.ide7.debug;

public class CompilationError {
    private final int line;
    private final String message;

    public CompilationError(int line, String message) {
        this.line = line;
        this.message = message;
    }

    public int getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (line > 0) {
            return "Ligne " + line + " : " + message;
        }
        return message;
    }
}
