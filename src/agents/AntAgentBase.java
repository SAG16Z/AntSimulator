package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public abstract class AntAgentBase extends Agent {

    /**
     * Builds reply message of given ACL type and language JSON,
     * with current agent as sender and msg sender as receiver.
     * @param msg
     *      Message to reply to
     * @param perf
     *      ACL type of reply
     * @return
     *      Prepared message
     */
    public ACLMessage prepareReply(ACLMessage msg, int perf){
        ACLMessage reply = new ACLMessage(perf);
        reply.setSender(getAID());
        reply.setLanguage("json");
        reply.addReceiver(msg.getSender());
        return reply;
    }

    /**
     * Builds message of given ACL type and language JSON,
     * with current agent as sender and msg sender as receiver.
     * @param receiver
     *      Receiver of the message
     * @param perf
     *      ACL type of reply
     * @return
     *      Prepared message
     */
    public ACLMessage prepareMesage(AID receiver, int perf){
        ACLMessage reply = new ACLMessage(perf);
        reply.setSender(getAID());
        reply.setLanguage("json");
        reply.addReceiver(receiver);
        return reply;
    }
}
