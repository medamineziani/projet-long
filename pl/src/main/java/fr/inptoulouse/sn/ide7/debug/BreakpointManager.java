package fr.inptoulouse.sn.ide7.debug;

import java.util.HashSet;
import java.util.Set;

public class BreakpointManager {

    private final Set<Integer> breakpoints;

    public BreakpointManager() {
        this.breakpoints = new HashSet<>();
    }

    public void addBreakpoint(int line) {
        breakpoints.add(line);
    }

    public void removeBreakpoint(int line) {
        breakpoints.remove(line);
    }

    public boolean hasBreakpoint(int line) {
        return breakpoints.contains(line);
    }

    public void displayBreakpoints() {
        System.out.println("Breakpoints actifs :");

        for (Integer line : breakpoints) {
            System.out.println("Ligne " + line);
        }
    }
}