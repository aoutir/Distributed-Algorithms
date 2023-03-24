package cs451;

import java.util.Objects;

public class MessageURB {
    private int sender; 
    private int seqNum; 


    public MessageURB(int sender, int seqNum ){
        this.sender = sender; 
        this.seqNum = seqNum;
    }
    
    public int getSender(){
        return sender;
    }

    public int seqNum(){
        return seqNum;
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(sender,seqNum);
    }

    @Override 
    public boolean equals(Object obj){
        if (obj == this){
            return true;
        }
        if(obj instanceof MessageURB){
            MessageURB msg = (MessageURB)obj;
            Boolean sameMessage = this.sender == msg.sender && this.seqNum == msg.seqNum;


            return sameMessage ; 
        }
        return false;
    }

    @Override 
    public String toString(){
        return "Ack message from " + sender + " for SEQNUM " + seqNum ;
    }
}
