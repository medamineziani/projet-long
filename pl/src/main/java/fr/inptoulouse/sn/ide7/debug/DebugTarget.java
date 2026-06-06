package fr.inptoulouse.sn.ide7.debug;

public class DebugTarget {
    public static void main(String[] args) throws Exception {
        System.out.println("Début DebugTarget");

        Thread.sleep(2000);

        int x = 5;
        int y = 10;
        int somme = x + y;

        System.out.println("Somme = " + somme);
    }
}
