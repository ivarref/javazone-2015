package great.app;

import some.base.lib.BaseA;
import some.lib.LibA;

public class AppB {
    public static void main(String[] args) {
        LibA libA = new LibA();
        BaseA baseA = new BaseA();
        System.out.println(AppB.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println(libA.getClass().getProtectionDomain().getCodeSource().getLocation());
        System.out.println(baseA.getClass().getProtectionDomain().getCodeSource().getLocation());
    }
}
