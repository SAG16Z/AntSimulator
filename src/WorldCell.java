import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class WorldCell {
    private float gradient = 0;
    private int ant = 0;
    private boolean food = false;
    private boolean anthill = false;
    private static float FOOD_PROB = 0.05f;

    public WorldCell(float _gradient, boolean _anthill) {
        //gradient = new Random().nextFloat() % 1;
        gradient = _gradient;
        if(_anthill) anthill = true;
        else {
            float prob = new Random().nextFloat() % 1;
            if (prob < FOOD_PROB) food = true;
        }
    }

    public void paint(Graphics g, int x, int y, Dimension size) {
        //Insets insets = getInsets();
        int w = size.width;// - insets.left - insets.right;
        int h = size.height;// - insets.top - insets.bottom;
        if(ant == 1) g.setColor(new Color(1.0f, 1.0f, 1.0f));
        else if(ant == 2) g.setColor(Color.MAGENTA);
        else if(food) g.setColor(new Color(1.0f, 0.0f, 0.0f));
        else if(anthill) g.setColor(new Color(0.0f, 0.0f, 1.0f));
        else g.setColor(Color.getHSBColor(0.5f, 0.5f, gradient));
        g.fillRect(x, y, w, h);
    }

    public void setAnt(int _ant) {
        ant = _ant;
    }
    public void setFood(boolean _food) {
        food = _food;
    }
    public boolean isFood() {return food;}
    public void setAnthill(boolean _anthill) {anthill = _anthill;}
    public boolean isAnthill() {return anthill;}
    public float getGradient() {return gradient;}
}