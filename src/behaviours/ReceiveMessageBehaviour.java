package behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.function.Consumer;

public class ReceiveMessageBehaviour extends CyclicBehaviour {
    private final MessageTemplate mt;
    private final Consumer<ACLMessage> onReceive;

    public ReceiveMessageBehaviour(MessageTemplate mt, Consumer<ACLMessage> onReceive) {
        this.mt = mt;
        this.onReceive = onReceive;
    }

    @Override
    public final void action() {
        ACLMessage msg = myAgent.receive(mt);
        if (msg == null) {
            block();
            return;
        }
        onReceive.accept(msg);
    }

}
