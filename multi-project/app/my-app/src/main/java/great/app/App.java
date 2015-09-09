package great.app;

import some.base.lib.BaseA;
import some.lib.LibA;

public class App {
    public static void main(String[] args) {
        LibA libA = new LibA();
        BaseA baseA = new BaseA();
        System.out.println("App is located in " + getJar(App.class));
        System.out.println("Lib is located in " + getJar(LibA.class));
    }

    public static String getJar(Class clazz) {
        String s = "" + clazz.getProtectionDomain().getCodeSource().getLocation();
        return s.substring(s.lastIndexOf("/")+1);
    }
}
