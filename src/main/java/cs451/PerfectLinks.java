package cs451;
import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.plaf.synth.SynthOptionPaneUI;

import cs451.App;
import cs451.Message;


/**
 * This class implements Perfect Links 
 * Every message sent by a correct process is delivered by the receiver excatly once, if the receiver is also correct 
 * 
 */

public class PerfectLinks {
    private Host myHost; //current host
    private Integer iddstHost; //receiver host 
    private DatagramSocket socket; //udp socket
    private Set<Message> delivered;//should be put in stable storage when stopped
    private ConcurrentHashMap<Integer,Set<Message>> notAcked = new ConcurrentHashMap<>(); 
    private boolean isConnected; 
    private int maxNbSenderTh = 6; 
    //SenderThread sender; 
    ReceiverThread receiver;
    RetransmitterThread retransmitter; 
    Abstraction application;





    //public PerfectLinks(int pid, String srcIp, int srcPort){
        public PerfectLinks(Host myHost, Abstraction application){
            this.myHost = myHost;
            delivered = new HashSet<Message>();
            isConnected = false;
            
            try{
                InetAddress ipAddress = InetAddress.getByName(myHost.getIp());
                this.socket = new DatagramSocket(myHost.getPort(),ipAddress);
            }catch(Exception e){
                e.printStackTrace();
            }
           // sender = new SenderThread();
           // sender.start();

            receiver = new ReceiverThread();
            receiver.start();
            retransmitter = new RetransmitterThread();
            retransmitter.start();
            this.application = application;

       
    }
    /**
     * Connects to a specific host, call it before sending a message to dstHost
     * @param dstAddress
     * @param dstPort
     */
    public void connect(Integer iddstHost){
        isConnected = true;
        this.iddstHost = iddstHost; 
    }
    /**
     * Returns the connection state 
     * @return
     */
    public boolean isConnected(){
        return isConnected;
    }
    /**
     * Sends message to the destination host
     * @param message
     */
    public void send(Message message, Host dstHost){
        //System.out.println(" I am sending msg " + message);
        if(dstHost == null){
            System.out.println("Failed to send a messag no destination host specified");
     
        }else{
            sendViaUdp(message, dstHost);

        }
        
    }

    public void sendViaUdp(Message message, Host dstHost){
        //System.out.println("Sending message with Seq Num " + message.getSeqNum()+ " to host "+  dstHost.getId());
        if(!message.isAck()){
            //add it to nonAcked map 
            int dstId = dstHost.getId();
            notAcked.putIfAbsent(dstId, ConcurrentHashMap.newKeySet());     //first time sending to this destination host 
            notAcked.get(dstId).add(message);
        }
        //else its an ack message and we should still send it  
        //sending the message in a udp packet
        try{
            InetAddress destIp = InetAddress.getByName(dstHost.getIp());
            Integer destPort = dstHost.getPort();
            byte[] buf = message.getBytes();
        
            DatagramPacket packet = new DatagramPacket(buf,buf.length,destIp,destPort);
            socket.send(packet);
        }catch(IOException e) {
            e.printStackTrace();
            System.out.println("Error: message could not be sent ");
        }
    }
    
    /*
     * Called to shut down the connection at interrupt signal 
     */
    public void close(){
        if(socket != null && receiver != null && retransmitter != null ){
            receiver.stop();;
            retransmitter.stop();
            socket.close();
            System.out.println("Finished INTERRUPT SIGNAL");
        }
    }


    /**
     * Receiver thread 
     */

    public class ReceiverThread extends Thread {
     
        
        @Override 
        public void run(){
            byte[] buffer = new byte[1024]; 
            while(true){
                DatagramPacket receivedPacket = new DatagramPacket(buffer,1024);    //get the message received using udp 

                try{
                    socket.receive(receivedPacket);
                    byte[] data = receivedPacket.getData(); 
                    Message msg = Message.getMessage(data);
                
                    if(msg != null){ 
                        if(msg.isAck()){ 
                          
                            Set<Message> set = notAcked.get(msg.getSenderId());
                            
                            Iterator itr = set.iterator();
                            while(itr.hasNext()){
                                Message itMsg = (Message)itr.next();
                                if(itMsg.isAckMessage(msg)){
                                  
                                    itr.remove();
                                }
                                
                            }
                        
                        }else{
                            int dstHostId = msg.getSenderId();
                            int senderHostId = myHost.getId();
                            Host dstHostforAck = Constants.hosts.get(dstHostId); //get the destination host
                            Message ack = new Message(senderHostId,dstHostId, msg.getSeqNum(),true,msg.getData());
                            ack.setOriginalSenderId(msg.getOriginalSenderId()); 
                          
                            send(ack,dstHostforAck);

                            if(!delivered.contains(msg)){
                                delivered.add(msg);
                               // System.out.println("Delivering PPL " + delivered);
                                application.deliver(msg);
                            }



                        }
                    }
                    
                }catch(IOException e){
                    System.out.println("Error: receiving packets ");
                    e.printStackTrace();
                }

            }

            }
        }

        
    
    



    /**Message retransmission given that we want to ensure that the receivers get the message 
    * Every period the retransmitter sends the non acks messages to the senders of the current host 
    * Periodically checks for acks 
    **/
    public class RetransmitterThread extends Thread {
        @Override 
        public void run(){
            while(true){
                
                Iterator<Map.Entry<Integer,Set<Message>>> iter = notAcked.entrySet().iterator();
            
                while(iter.hasNext()){
                    Map.Entry<Integer,Set<Message>> entry = iter.next();
                    Integer idHost = entry.getKey();
                    Set<Message> messages = entry.getValue();
                    if(!messages.isEmpty()){
                    //System.out.println("Retransmission for host with id" + idHost + " the message " + messages);
                    //Retransmit all the non acks messages of the past dst hosts 
                    
                    messages.forEach(msg -> {
                       // System.out.println("Retransmission: " + msg);

                        send(msg, Constants.hosts.get(idHost)); 
                    });
                    }
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                }catch(Exception e){
                    System.exit(1);
                    e.printStackTrace();
                    
                }
               
               
        }
    }
}
}







