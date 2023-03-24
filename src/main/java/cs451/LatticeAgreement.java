package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



/**
 * n = 2f + 1 (we assume that at most f processes can fail)
 * proposed algo, processes propose and decide only once (single-shot)
 * Goal: processes decide on a sequence of proposals (run multiple slots of single-shot )
 *I encode the proposal messaging as follows: 
 *First element of data (Proposal: 0, ACK: 1, NACK:2)
 *Store the mapping between proposal number and the messages received for this proposal number (process them later when we arrive to this 
    proposal number) deals non ordering of messages processes don't run at the same speed
* Si un process i passe au second round j'incrémente the active_number_i et du coup si un process j m'envoie un propsal  
concernant le round précedent i, vu que je manipule mes accepted values en utilisant les active proposal numbers 
je vais répondre en utilisant the accepted value of round i + 1 ce qui n'est pas cohérent !  
je passe pas au second round que si ps >=  f + 1 ont decidé 
active number reviens à 0 quand je décide et j'incremente le round quand je décide 
active proposal number how many proposals are active ? 
 */
public class LatticeAgreement extends Abstraction {
   
    public boolean active;
    private int ack_count; 
    private int nack_count;
    private int act_prop_nb;
    private String outputPath;
    private Set<Integer> prop_value;
    //private Set<Integer> accepted_value;
    private final BestEffortBroadcast beb;
    //private ConcurrentHashMap<Integer,Set<Message>> pending = new ConcurrentHashMap<>(); 
    private ConcurrentHashMap<Integer,Set<Integer>> accepted_values = new ConcurrentHashMap<>(); 
    //private Set<Integer> accepted_value_i;
    private MultiShotLattice upper ;
    private int round;





    public LatticeAgreement(Host myHost, String outputPath, MultiShotLattice upper){
        super(myHost);
        active = false;
        prop_value = new HashSet<Integer>();
        //accepted_value_i = new HashSet<Integer>();
        this.round = 0;

        this.beb = new BestEffortBroadcast(myHost, this);
        this.upper = upper;
        this.outputPath = outputPath;
        
        


    }

    public void propose(Set<Integer> proposal){
        active = true;
        upper.next = false;
        ++round ;
        System.out.println("My proposal" + proposal);
       
        prop_value = new HashSet<Integer>(proposal);
        
        ++act_prop_nb; 
        ack_count = 0 ;
        nack_count = 0 ;
     
        int myId = super.process.getId();
     
        LinkedList<Integer> data = new LinkedList<Integer>(proposal);
        data.addFirst(0);
        data.add(1, act_prop_nb); //insert the active proposal number at position 1 
        Message PROPOSAL = new Message(myId,0,round,false,data);
        beb.broadcast(PROPOSAL);

    }
    /**
     * returns decision and the upper layer (multi-shot) will write it to the output file 
     */
    public void decide(){
        //System.out.println("I have to logg this "+super.lines);
        beb.ppl.close();
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
            if(!lines.isEmpty()){
                for(String line: lines){
                    writer.write(line);
                }
            }
         
            writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    /**
     * Here the ack of the message means that the proposal is valid 
     * At this point we receive a message either a nack or ack  
     */
    @Override
    public void deliver(Message m){
       //System.out.println("Delivering "+ m);
       //we can't receive the proposal of host 1 in host 2 
       processMessage(m);


    }

    public void processMessage(Message m ){

        int code_prop = m.getData().getFirst();
        int proposal_round = m.getSeqNum(); //the round 
        int active_prop_msg = m.getData().get(1); //proposal_number 

        LinkedList <Integer> value = new LinkedList<>(m.getData());
        value.removeFirst();
        value.removeFirst(); //get only the proposed_value  
      
    
            //----------------------------------------------UPON RECEPTION--------------------------------------------
            switch (code_prop){
                case 0:     
                //---------------------------Acceptor-------------------------
                System.out.println("Reception Proposal " + value );
        
                accepted_values.putIfAbsent(proposal_round, ConcurrentHashMap.newKeySet());
                Set<Integer> accepted_value_i= accepted_values.get(proposal_round);

                    if(value.containsAll(accepted_value_i)){ 
                        accepted_value_i = new HashSet<>(value);
                        accepted_values.put(proposal_round, accepted_value_i);
                        LinkedList<Integer> data = new LinkedList<>();
                        data.add(1);
                        data.add(active_prop_msg);

                        int idReceiver = m.getSenderId();
                        Message ACK = new Message(process.getId(),idReceiver, proposal_round, false, data);
                        beb.ppl.send(ACK, Constants.hosts.get(idReceiver));
                    }else{

                        accepted_value_i.addAll(value);
                        accepted_values.put(proposal_round, accepted_value_i);

                        LinkedList<Integer> data = new LinkedList<>();
                        data.add(2);
                        data.add(active_prop_msg);
                        data.addAll(accepted_value_i);
                        int idReceiver = m.getSenderId();
                        Message NACK = new Message(process.getId(),idReceiver, proposal_round,false,data);
                        //TO-DO: maybe set the original sender 
                        beb.ppl.send(NACK, Constants.hosts.get(idReceiver));
                    }
                    //check ds 
                    /**
                     * 
                     *   */
                    Set<Integer> check = new HashSet<>();
                    for(Set<Integer> s : accepted_values.values()){
                        check.addAll(s);
                    }
                   
                   
                    if(check.size() > upper.ds ){
                        System.out.println("The number of distinct of elements accross processes is greater then value ds");
                        System.exit(1);

                    }
                    
                    break;
                case 1:     //-------------------------------------receiving ACK
                if(proposal_round == round && active_prop_msg == act_prop_nb){
                        //System.out.println("Reception ACK ");
                        ++ack_count;
                }
                    break;

                case 2:     //-------------------------------------receiving NACK
                if(proposal_round == round && active_prop_msg == act_prop_nb){
                        //System.out.println("Reception NACK ");
                        prop_value.addAll(value);
                        ++nack_count;
                }else
                    break; 
            }
                int N = Constants.hosts.size();
                //-----------------------------------------------NEW PROPOSAL---------------------------------------------
                if(nack_count > 0 && ack_count + nack_count >= (N + 1) / 2 && active == true){
                    ++act_prop_nb;
                    ack_count = 0;
                    nack_count = 0; 
                    int idReceiver = m.getSenderId();
                    LinkedList <Integer> data = new LinkedList<>();
                    data.add(0);
                    data.add(act_prop_nb);
                    data.addAll(prop_value);
                    Message PROPOSAL = new Message(process.getId(),idReceiver,round,false,data);
                    beb.broadcast(PROPOSAL);
                }

                //-----------------------------------------------DECIDE---------------------------------------------
                if(ack_count >= (N + 1) / 2 && active == true){
                   String result = prop_value.stream()
                              .map(n -> String.valueOf(n))
                         .collect(Collectors.joining(" ", "", "\n"));
                    lines.add(result);
                    System.out.println("Deciding...: " + result );
                    act_prop_nb = 0;
                    upper.next = true;
                    active = false;
                }

      

    }


}
