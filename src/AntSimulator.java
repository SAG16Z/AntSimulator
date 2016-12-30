import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import javax.swing.*;

public class AntSimulator extends JFrame{

    static ContainerController containerController;

    public AntSimulator(){
        setTitle("Ant simulator");
        setSize(1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WorldPanel panel = new WorldPanel();
        add(panel);
    }

    public void startAgents(String host, String port, String name) {
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, host);
        profile.setParameter(Profile.MAIN_PORT, port);
        profile.setParameter(Profile.PLATFORM_ID, name);

        containerController = runtime.createMainContainer(profile);

        try {
            AgentController ac = containerController.createNewAgent("a1", AntBuilder.class.getCanonicalName(), null);
            ac.start();
        } catch (StaleProxyException e) {
            System.err.println("error creating agent");
        }
    }

    public static void main(String[] args){

            AntSimulator frame = new AntSimulator();
            frame.setVisible(true);
            frame.startAgents("127.0.0.1", Profile.LOCAL_PORT, "AntsContainer");
    }
}
