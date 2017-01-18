package agents;

import behaviours.ReceiveMessageBehaviour;
import com.google.gson.Gson;
import enums.Actions;
import enums.CellType;
import gui.MainFrame;
import gui.MapPanel;
import gui.MapPanel2;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import map.Point;
import map.Point2;
import map.WorldCell;
import messages.*;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server2 extends AntAgentBase {

    public static String NICKNAME = "agents.World";
    public static String WORLD_SERVICE = "ant-world";

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final int ANT_CNT = 3;
    private MainFrame gui;
    private MapPanel2 mapPanel;
    private Map<Color, AID> hills = new HashMap<>();
    private Map<AID, Point2> ants = new HashMap<>();

    protected void setup() {
        // Register server in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(WORLD_SERVICE);
        sd.setName("JADE-ant-world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        gui = new MainFrame(this);
        mapPanel = new MapPanel2();
        gui.setVisible(true);

        AgentController ac;
        try {
            Object hill_args[] = new Object[2];
            hill_args[0] = Color.BLACK;

            Random r = new Random();
            int x = 2 + r.nextInt(MapPanel.CELL_H - 4);
            int y = 2 + r.nextInt(MapPanel.CELL_V - 4);
            Point2 pos = new Point2(x, y);
            hill_args[1] = pos;

            mapPanel.setAntHill(pos, Color.BLACK);

            ac = getContainerController().createNewAgent("agents.AntHill"+0, AntHill.class.getCanonicalName(), hill_args);
            ac.start();

            Object args[] = new Object[1];
            args[0] = Color.BLACK;

            for(int i = 0; i < ANT_CNT; i++) {
                ac = getContainerController().createNewAgent("agents.Ant"+i+"_"+args[0].toString(), Ant2.class.getCanonicalName(), args);
                ac.start();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        // add behaviour for INFORM message from antworld
        MessageTemplate mtAWRequest = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mtAWRequest, this::onAWRequest));
    }


    @Override
    protected void takeDown(){
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        LOG.debug("agents.Server agent "+getAID().getName()+" terminating.");
        // gracefully shutdown platform
        Codec codec = new SLCodec();
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getAMS());
        msg.setLanguage(codec.getName());
        msg.setOntology(JADEManagementOntology.getInstance().getName());
        try {
            LOG.debug("Shutting down platform.");
            getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
            send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        PropertyConfigurator.configure("src/resources/log4j.properties");

        LOG.debug("startAgent");
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "127.0.0.1");
        profile.setParameter(Profile.MAIN_PORT, Profile.LOCAL_PORT);
        profile.setParameter(Profile.PLATFORM_ID, "AntsContainer");
        profile.setParameter(Profile.GUI, "true");

        try {
            ContainerController container = runtime.createMainContainer(profile);
            container.createNewAgent(NICKNAME, Server2.class.getCanonicalName(), null).start();
        } catch (StaleProxyException e) {
            System.err.println("error creating server agent");
        }

    }

    /**
     * Handles all requests from ants - updates and sends correct
     * perception messages
     * @param msg
     *      Message of type REQUEST
     */
    private void onAWRequest(ACLMessage msg) {
        String content = msg.getContent();

        ACLMessage reply = prepareReply(msg, ACLMessage.INFORM);
        Gson g = new Gson();

        ActionMessage amsg = g.fromJson(content, ActionMessage.class);

        AntMessage2 antmsg = new AntMessage2();

        Point2 oldPoint, newPoint;

        switch(amsg.action) {
            case ANT_ACTION_LOGIN:
                Random r = new Random();
                newPoint = new Point2(r.nextInt(mapPanel.CELL_H), r.nextInt(mapPanel.CELL_V));

                antmsg.position = newPoint;
                antmsg.cellType = mapPanel.worldMap[newPoint.x][newPoint.y].type;
                reply.setContent(g.toJson(antmsg, AntMessage2.class));

                ants.put(msg.getSender(), newPoint);
                break;
            case ANT_ACTION_MOVE:
                oldPoint = ants.get(msg.getSender());
                newPoint = oldPoint.adjacent(amsg.direction);

                if(newPoint != null && mapPanel.isValidPosition(newPoint)){
                    ants.put(msg.getSender(), newPoint);
                    antmsg.position = newPoint;
                    antmsg.cellType = mapPanel.worldMap[newPoint.x][newPoint.y].type;
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }
                break;
            case ANT_ACTION_COLLECT:
                break;
            case ANT_ACTION_DROP:
                break;
        }
        reply.setContent(g.toJson(antmsg, AntMessage2.class));
        send(reply);
        gui.repaint();
    }
}

