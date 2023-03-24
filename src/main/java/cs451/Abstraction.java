package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Abstraction {
    protected Host process; //running process
    protected LinkedList<String > lines ;



    public Abstraction(Host process){
        this.process = process;
        this.lines = new LinkedList<>();


    }
    
       /**
     * Delivers message and save it, this method is called when the message is succesfully deliverd 
     * by perfect links
     * 
     * @param m message to deliver 
     */
    public void deliver(Message m){
        System.out.println("Delivering for abstraction");

        int idSender = m.getOriginalSenderId();
        int seqNum = m.getSeqNum();
        System.out.println("Delivery of message " + seqNum);
        lines.add(String.format("d %d %d\n" ,idSender,seqNum));
    }


   

     

}
