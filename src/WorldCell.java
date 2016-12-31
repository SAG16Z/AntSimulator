import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class WorldCell {
    private float gradient = 0;

    public WorldCell() {
        gradient = new Random().nextFloat() % 1;
    }

    public void paint(Graphics g, int x, int y, Dimension size) {
        //Insets insets = getInsets();
        int w = size.width;// - insets.left - insets.right;
        int h = size.height;// - insets.top - insets.bottom;
        g.setColor(Color.getHSBColor(0.5f, 0.5f, gradient));
        g.fillRect(x, y, w, h);
    }
}