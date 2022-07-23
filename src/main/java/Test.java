import java.awt.*;

public class Test {
    public static void main(String[] args) {
        Color color = Color.CYAN;
        System.out.println(color.toString());
        System.out.println(colourToString(color));

        System.out.println(Color.decode("#00ffff"));
    }

    public static String colourToString(java.awt.Color c) {
        return "#" + Integer.toHexString(0xFF000000 | c.getRGB()).substring(2);
    }
}
