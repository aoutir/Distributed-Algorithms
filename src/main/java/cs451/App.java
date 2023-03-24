package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.naming.directory.InvalidAttributesException;
import javax.swing.plaf.synth.SynthOptionPaneUI;

import cs451.PerfectLinks;
import cs451.FiFoBroadcast;


public class App extends Abstraction{


    private PerfectLinks link = null;
    private FiFoBroadcast fifo = null;
    private BestEffortBroadcast beb = null;
    private UniformReliableBroadcast urb = null;
    private String outputPath = null;
    private LatticeAgreement singleShotLattice = null;

    public App(Host process,  String outputPath){
        super(process);
        this.outputPath = outputPath;
        //link = new PerfectLinks(process,this);

        //beb = new BestEffortBroadcast(process, this);
       // singleShotLattice = new LatticeAgreement(process,this);

        //urb = new UniformReliableBroadcast(process, this);
        //fifo = new FiFoBroadcast(process, this);

    }
    /**
     * nbProposals: number of proposals for each process
     * vs: maximum number of elements in a proposal 
     * ds: maximum number of distinct elements across all proposals and all processes 
     * proposed_sets: list of proposals 
     */

    public void run_lattice(int nbProposals, int vs, int ds){

    }

 
    public void run_ppl(Host receiver, int nbMessages){
        //call setReceiver before running 
        if(receiver.getId() != process.getId()){

            List<Integer> listofsqNum = IntStream.rangeClosed(1, nbMessages).boxed().collect(Collectors.toList());
            listofsqNum.stream().forEach(s -> {
                Message msg = new Message(process.getId(),receiver.getId(),s,false,null);
                msg.setOriginalSenderId(process.getId());
                link.send(msg,receiver); //operations on variables outside the loop are not allowed sauf s'ils sont finales
                lines.add(String.format("b %d\n" ,s));

            });
        }else{
            
            System.out.println("Process can't send messages to itself");
            throw new Error("Please provide a valid receiver host");            
        }


    }


    public void run_beb(int nbMessages){
        List<Integer> listofsqNum = IntStream.rangeClosed(1, nbMessages).boxed().collect(Collectors.toList());
        listofsqNum.stream().forEach(s -> {
            //at this point the receiver is unknown 
            Message msg = new Message(process.getId(),0,s,false,null);
            msg.setOriginalSenderId(process.getId());
            beb.broadcast(msg);
            lines.add(String.format("b %d\n" ,s));

        });
    }

    public void run_urb(int nbMessages){
        List<Integer> listofsqNum = IntStream.rangeClosed(1, nbMessages).boxed().collect(Collectors.toList());
        listofsqNum.stream().forEach(s -> {
            //at this point the receiver is unknown 
            Message msg = new Message(process.getId(),0,s,false,null);
            msg.setOriginalSenderId(process.getId());
            urb.broadcast(msg);
            lines.add(String.format("b %d\n" ,s));

        });
    }

    public void run_fifo(int nbMessages){
        List<Integer> listofsqNum = IntStream.rangeClosed(1, nbMessages).boxed().collect(Collectors.toList());
        listofsqNum.stream().forEach(s -> {
            //at this point the receiver is unknown 
            Message msg = new Message(process.getId(),0,s,false,null);
            msg.setOriginalSenderId(process.getId());
            fifo.broadcast(msg);
            lines.add(String.format("b %d\n" ,s));

        });
    }
   
    

    /*
     * Write the operations done into the output file called when a process crashes 
     */

    public void logg(){
        //System.out.println("I have to logg this "+super.lines);
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

    public void finish_ppl(){
        link.close();
    }


}
