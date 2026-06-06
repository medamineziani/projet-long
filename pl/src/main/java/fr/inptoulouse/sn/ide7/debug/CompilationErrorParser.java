package fr.inptoulouse.sn.ide7.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilationErrorParser {

    private static final Pattern ERROR_PATTERN =
            Pattern.compile(".*\\.java:(\\d+): error: (.*)");

    public List<CompilationError> parse(String compilerOutput) {
        List<CompilationError> errors = new ArrayList<>();

        String[] lines = compilerOutput.split("\\R");

        for (String line : lines) {
            Matcher matcher = ERROR_PATTERN.matcher(line);

            if (matcher.matches()) {
                int lineNumber = Integer.parseInt(matcher.group(1));
                String message = matcher.group(2);
                errors.add(new CompilationError(lineNumber, message));
            }
        }

        return errors;
    }
}