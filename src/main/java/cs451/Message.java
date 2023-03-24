package cs451;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set; 
/**
 * Classe representing a message 
 * @senderId: Id of the sender
 * @seqNum: Id of the message  
 * @isAck: Acknowledgment to specify that the message is received 
 */

public class Message implements Serializable {
    private int senderId;
    private int receiverId;
    private int seqNum; 
    private Boolean isAck; 
    private LinkedList<Integer> data;
    private int originalSenderId;

    //at creation the senderId is the same as the original senderId and then if we broadcast the message 
    //in urb we change the sender id because we want to receive the acks to the host who is broadcasting 

    public Message(int senderId, int receiverId, int seqNum, boolean isAck, LinkedList<Integer> data){
        this.senderId = senderId; 
        this.receiverId = receiverId;
        this.seqNum = seqNum; 
        this.isAck = isAck; 
        this.data = data;

    }

    @Override
    public int hashCode(){
        return Objects.hash(seqNum);
    }
    public int getSenderId(){
        return senderId; 
    }
  
    public int getSeqNum(){
        return seqNum; 
    }
    public boolean isAck(){
        return isAck;
    }
    public int getReceiverId(){
        return receiverId;
    }
    public LinkedList<Integer> getData(){
        return data;
    }
    public void setSenderId(int id) {
        this.senderId = id;
    }
    public void setSeqNum(int i ){
        this.seqNum =  i; 
    }
    public void setOriginalSenderId(int id ){
         this.originalSenderId = id ;
    }
  
    public int getOriginalSenderId(){
        return this.originalSenderId;
    }

    public void setReceiver(int id ){
        this.receiverId = id;
    }
   

   
    /**
     * Converts a message to array of bytes 
     * @return array of bytes
     */
    public byte[] getBytes() {
        byte [] bytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            bytes = baos.toByteArray(); 
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Failed to convert message to bytes ");
        }
        return bytes;
    }
    /**
     * Converts a buffer of bytes to a message
     * @param bytes
     * @return the message corresponding to the argument
     */
    public static Message getMessage(byte[] bytes) {
        Message msg = null;
        if(bytes != null){
            try{
                ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
                ObjectInputStream oos = new ObjectInputStream(baos);
                msg = (Message)(oos.readObject());
            }catch(Exception e){
                //e.printStackTrace();
                System.out.println("My data looks like "+bytes);
                System.out.println("Failed to convert uzgqgsuz bytes to a message ");
            }
    }

      return msg;
    }
    
    private boolean compareData(LinkedList<Integer> dataOther){
        if(dataOther.size() == data.size()){
          
            for(int i = 0; i < data.size() ; i++){
                if(dataOther.get(i) != data.get(i) ){
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }
     
  
    /*
     * Override this method to be able to use method contains of set 
     */
    @Override
    public boolean equals(Object o ){
        if (o == this){
            return true;
        }
        if(o != null && this.getClass() == o.getClass()){ //o instanceof Message
            Message msg = (Message)o;
            Boolean sameMessage = this.senderId == msg.senderId 
            && this.receiverId == msg.receiverId && this.seqNum == msg.seqNum && this.data.getFirst().intValue() == msg.data.getFirst().intValue();
            /**
             *   System.out.println("Comparing " + this + "and " + msg);
            System.out.println("Result " + sameMessage);
            System.out.println(this.data.getFirst().intValue() == msg.data.getFirst().intValue());
            System.out.println(this.isAck == msg.isAck);
             */
            
          

            return sameMessage ; 
        }
        return false ;
    }

    @Override
    public String toString(){
        if(!isAck){
            return "The (DATA) of message with seqNum " + this.seqNum + " is sent by " + this.senderId + " to " + this.receiverId + " of type: " + data;
        }else{
            return "The (ACK) of message with seqNum " + this.seqNum + " is sent by " + this.senderId + " SRC:" + this.originalSenderId + " to " + this.receiverId ;
 
        }
    }


    /**
     * Checks if the given argument is the ack of the message 
     */
    public boolean isAckMessage(Message msg){
        Boolean check = msg.receiverId == this.senderId && this.receiverId == msg.senderId 
        && msg.seqNum == this.seqNum && msg.isAck == true && this.data.getFirst() == msg.data.getFirst() ;
        return check;
    }

    public boolean isPending(Message msg){
        Boolean pending = msg.receiverId == this.senderId && this.seqNum == msg.seqNum && isAck == true;
        return pending;
    }

    
    public Message getCopy(){
        Message copy = new Message( this.senderId, this.receiverId, this.seqNum, this.isAck,this.data);
        copy.setOriginalSenderId(originalSenderId);
        return copy;
    }



   
}
