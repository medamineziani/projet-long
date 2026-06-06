package fr.inptoulouse.sn.ide7.debug;

import java.util.ArrayList;
import java.util.List;

public class MemorySnapshotManager {

    private final List<MemorySnapshot> snapshots = new ArrayList<>();

    public void addSnapshot(String step, String stateDescription) {
        snapshots.add(new MemorySnapshot(step, stateDescription));
    }

    public List<MemorySnapshot> getSnapshots() {
        return snapshots;
    }

    public void displaySnapshots() {
        System.out.println("Historique des états mémoire :");

        for (MemorySnapshot snapshot : snapshots) {
            System.out.println(snapshot.getStep() + " : " + snapshot.getStateDescription());
        }
    }
}