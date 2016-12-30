import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class WorldCell extends JComponent {
    private float gradient = 0;

    public WorldCell() {
        gradient = new Random().nextFloat() % 1;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Dimension size = getSize();
        Insets insets = getInsets();
        int w = size.width - insets.left - insets.right;
        int h = size.height - insets.top - insets.bottom;
        g.setColor(Color.getHSBColor(0.5f, 0.5f, gradient));
        g.fillRect(0, 0, w, h);
    }
}