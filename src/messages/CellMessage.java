package messages;

import com.google.gson.*;
import enums.Actions;
import enums.CellType;
import javafx.scene.control.Cell;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Vector;

public class CellMessage {

    private int x;
    private int y;
    private CellType type;
    private int food;
    private int material;
    private int color;
    private float gradientTotalValue;
    private float gradientValue;
    private Float[] adjacentGradient;
    private Actions[] adjacentGradientActions;
    private Float[] adjacentEnemyGradient;
    private Actions[] adjacentEnemyGradientActions;
    private Float[] adjacentPheromones;
    private Actions[] adjacentPheromonesActions;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public float getGradientTotalValue() {
        return gradientTotalValue;
    }

    public void setGradientTotalValue(float gradientTotalValue) {
        this.gradientTotalValue = gradientTotalValue;
    }

    public float getGradientValue() {
        return gradientValue;
    }

    public void setGradientValue(float gradientValue) {
        this.gradientValue = gradientValue;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getMaterial() {
        return material;
    }

    public void setMaterial(int material) {
        this.material = material;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Float[] getAdjacentGradient() {
        return adjacentGradient;
    }

    public void setAdjacentGradient(Float[] adjacentGradient) {
        this.adjacentGradient = adjacentGradient;
    }

    public Actions[] getAdjacentGradientActions() {
        return adjacentGradientActions;
    }

    public void setAdjacentGradientActions(Actions[] adjacentGradientActions) {
        this.adjacentGradientActions = adjacentGradientActions;
    }

    public Float[] getAdjacentEnemyGradient() {
        return adjacentEnemyGradient;
    }

    public void setAdjacentEnemyGradient(Float[] adjacentEnemyGradient) {
        this.adjacentEnemyGradient = adjacentEnemyGradient;
    }

    public Actions[] getAdjacentEnemyGradientActions() {
        return adjacentEnemyGradientActions;
    }

    public void setAdjacentEnemyGradientActions(Actions[] adjacentEnemyGradientActions) {
        this.adjacentEnemyGradientActions = adjacentEnemyGradientActions;
    }

    public Float[] getAdjacentPheromones() {
        return adjacentPheromones;
    }

    public void setAdjacentPheromones(Float[] adjacentPheromones) {
        this.adjacentPheromones = adjacentPheromones;
    }

    public Actions[] getAdjacentPheromonesActions() {
        return adjacentPheromonesActions;
    }

    public void setAdjacentPheromonesActions(Actions[] adjacentPheromonesActions) {
        this.adjacentPheromonesActions = adjacentPheromonesActions;
    }
}
