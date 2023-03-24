package cs451;
/*
 * Weak form of reliability 
 * Ensuring reliability only on the sender 
 * Validity: If a correct process broadcasts a message m, then every correct process eventualy delivers m 
 */

public class BestEffortBroadcast extends Abstraction {
    private Host myHost; //current host
    public final PerfectLinks ppl;  
    private Message message;
    public Abstraction app; 


    public BestEffortBroadcast(Host myHost, Abstraction app){
        super(myHost);
        this.myHost = myHost;
        this.app = app;
        this.ppl = new PerfectLinks(myHost, this);
    }

    public void broadcast(Message m ){
        if(m != null){


            for(Host host: Constants.hosts.values()){
                Message toSend = m.getCopy();
                toSend.setReceiver(host.getId());
                System.out.println("Broadcasting " + toSend);
                ppl.send(toSend, host);

            }
        }else{
            throw new Error("Please provide the message you want to broadcast");
        }
    }

    @Override
    public void deliver(Message m){
        app.deliver(m);
      
    }

    

    
    
}
