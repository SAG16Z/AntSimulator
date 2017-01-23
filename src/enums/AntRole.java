package enums;

/**
 * Created by Michał on 2017-01-19.
 */
public enum AntRole {
    WORKER, BUILDER, WARRIOR, QUEEN, THIEF;

    public String getName() {
        switch(this)
        {
            case WORKER:
                return "ant.Worker";
            case BUILDER:
                return "ant.Builder";
            case QUEEN:
                return "ant.Queen";
            case WARRIOR:
                return "ant.Warrior";
            case THIEF:
                return "ant.Thief";
            default:
                return "";
        }
    }
}