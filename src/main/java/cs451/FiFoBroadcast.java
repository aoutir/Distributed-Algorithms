package cs451;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * FiFo order ensures that messages broadcast by the same sender process 
 * are delivered in the order in which they were sent.
 * Can be implemented by delaying the delivery of a message
 * from a given sender until all messages that the sender
 * has broadcast before m has been delivered. 
 * How to detect the messages sent before m, maybe with the sequence 
 * number ?  
 * FRB: FIFO delivery if some process broadcasts message m1 before it broadcasts 
 * message m2 then no correct process delivers m2 unless it has already
 * delivered m1.
 * 
 * array next: contains an entry for every process with the 
 * sequence number of the next message to be delivered by the 
 * process. 
 * pending: buffer all the received msgs 
 */
public class FiFoBroadcast extends Abstraction{
    private UniformReliableBroadcast urb; 
    private int lsn; //sequence number to send with the messages 
    private Map<MessageURB, Message> pending; //messages that need to be delivered
    private int[] next; //for me it doesn't make sense 
    private final Abstraction app;
    //to use an atomic array because operations are executed 
    //squentially 


    public FiFoBroadcast(Host process, Abstraction app){
        super(process);
        this.next = new int[Constants.hosts.size()];
        this.urb = new UniformReliableBroadcast(process,this);
        Arrays.fill(this.next,1);
        lsn = 0;
        pending =  new HashMap<>();
        this.app = app;
    }

    public void broadcast(Message m){
        lsn = lsn + 1 ;
        //send the lsn along with the message 
        m.setSeqNum(lsn);
        urb.broadcast(m);
    }

    @Override 
    public void deliver(Message m ){
        System.out.println("DELIVERING FIFO " + m );
        int originalSender = m.getOriginalSenderId();
        int seqNum = m.getSeqNum();
        MessageURB msgURB = new MessageURB(originalSender,seqNum);
        if(m.getSeqNum() >= next[originalSender - 1 ]){ 
            pending.put(msgURB, m);
            //check if there exist a pending message that we can deliver 
            Iterator<Map.Entry<MessageURB, Message>> itr = pending.entrySet().iterator();
            while(itr.hasNext()){
                MessageURB nextMessage = itr.next().getKey();
                int iSender = nextMessage.getSender() ;
                
                if(nextMessage.seqNum() == next[iSender- 1 ]){
                    next[iSender -1 ]= next[iSender - 1 ]  + 1;
                    //deliver the corresponding msg
                    app.deliver(m);
                    itr.remove();
                }
            }

        }
        
    }


}