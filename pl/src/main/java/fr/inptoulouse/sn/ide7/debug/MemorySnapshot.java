package fr.inptoulouse.sn.ide7.debug;
public class MemorySnapshot {
    private final String step;
    private final String stateDescription;

    public MemorySnapshot(String step, String stateDescription) {
        this.step = step;
        this.stateDescription = stateDescription;
    }

    public String getStep() {
        return step;
    }

    public String getStateDescription() {
        return stateDescription;
    }
}