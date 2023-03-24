package cs451;

import java.rmi.dgc.VMID;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanFeatureInfo;
import javax.swing.plaf.synth.SynthSplitPaneUI;

/**
 * Uniform (Regular) Reliable Broadcast 
 * Validity: If a correct process p broadcasts a message m, then p eventually delivers m 
 * Agreement: If a message m is delivered by any (correct) process then m is eventually delivered by every correct
 * process. 
 * Since we don't have a perfect failure detector this algorithm will implement the majority ack uniform 
 * reliable broadcast 
 * Assume a majority of correct proccesses 
 * On dirait qu'il envoie les msgs une fois et qu'il ressaye pas de les renvoyer et
 * du coup si le receiver host n'est pas allumé bah il recevera pas les msgs 
 * et du coup le host 3 et 4 ne recois pas le msg du 2 
 * mais normalement
 */

public class UniformReliableBroadcast extends Abstraction{
    private final BestEffortBroadcast beb; 
    //create a map of key as message and as a value the set of a process that acked this message 
    private ConcurrentHashMap<MessageURB,Set<Integer>> ack ; 
    private Map<MessageURB,Message> pending ; //sent messages 
    private Set<MessageURB> delivered;
    private Abstraction app;



    public UniformReliableBroadcast(Host process,Abstraction app){
        super(process);
        this.ack = new ConcurrentHashMap<>();
        this.pending = new ConcurrentHashMap<>();
        this.delivered = ConcurrentHashMap.newKeySet();
        this.beb = new BestEffortBroadcast(process,this); 
        this.app = app;

       
    }


    public void broadcast(Message m) {
        //add m to pending 
        MessageURB msgURB = new MessageURB(process.getId(),m.getSeqNum());
        pending.put(msgURB,m);
        beb.broadcast(m);

    }

   
    /**
     * My function deliver is called once because its already contained in the delivered 
     * its not the same message 
     * why does the contain return true 
     * the received msg will have as a receiver my host and isAck = True and the sender will be one of
     * available hosts and the seq num
     * what is relevant here is the receiver, the seq num and the ack 
     * bah normalement ca c'est fait dans le perfect links si on recoit pas 
     * de ack on retransmit le msg 
     */
    @Override
    public void deliver(Message m ){
        System.out.println("DELIVERING URB : "  + m);

        MessageURB msgURB = new MessageURB(m.getOriginalSenderId(),m.getSeqNum());

        ack.putIfAbsent(msgURB, ConcurrentHashMap.newKeySet());     //first time delivering this message
        ack.get(msgURB).add(m.getSenderId()); //recheck hash 
        
      
        if(!pending.containsKey(msgURB)){ 

            pending.put(msgURB,m);//because we can't deliver it yet it has to pend 
            
            m.setSenderId(process.getId());
            beb.broadcast(m);
            
        }

        Iterator<Map.Entry<MessageURB, Message>> iter = pending.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<MessageURB, Message> curr = iter.next();
            MessageURB msgURBcurr = curr.getKey();

            if(!delivered.contains(msgURBcurr) && canDeliver(msgURBcurr)){
                app.deliver(curr.getValue());
                delivered.add(msgURB);
                iter.remove();
                //System.out.println("I am removing elements" + msgURB);
               // System.out.println("PENDING: "  + pending);
        }

        }

       

       // System.out.println("PENDING" + pending);

        
       
    }

    public boolean canDeliver(MessageURB m){
       
        //System.out.println("I am checking can deliver " + ack.getOrDefault(msgURB,new HashSet<>()));
        //System.out.println("My set of acks are " + ack);


        int N = Constants.hosts.size();
        return ack.getOrDefault(m, ConcurrentHashMap.newKeySet()).size() > (N /2) ;
    }


    
    
}
