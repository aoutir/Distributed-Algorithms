package cs451;

import java.util.LinkedList;
import java.util.Set;

import javax.swing.UIDefaults.ActiveValue;

public class MultiShotLattice {
    public int ds;
    private LatticeAgreement singleShotLattice = null;
    private LinkedList<Set<Integer>> proposals;
    public boolean next ;


    public MultiShotLattice(Host process,int ds,String outputPath, LinkedList<Set<Integer>> proposals){
        this.ds = ds;
        this.proposals = proposals;
        next = true;
        singleShotLattice = new LatticeAgreement(process, outputPath,this);

    }

    public void run(){
        int i = 0 ;
        while(!proposals.isEmpty()){
            System.out.println("Next.... " + next);
            if(next){ //next if more than half of the processes agreed on its value 
                System.out.println("Proposing....");
                System.out.println(proposals);
                singleShotLattice.propose(proposals.removeFirst());
                ++i;
            }
        }
           
    }
    
    public void logg(){
        singleShotLattice.decide();
    }



}
