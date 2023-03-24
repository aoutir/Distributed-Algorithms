package cs451;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static MultiShotLattice lattice;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        lattice.logg();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        String configPath = parser.config();
        System.out.println(configPath + "\n");

        //first line is the same accross all processes
        int nbProposals = 0; //max number of proposals for the process
        int vs = 0;          //max number of elements in a proposal 
        int ds = 0;          //distinct number of elements accross all proposals of all processes 
        //ds needs to be verified in the Lattice agreement 
        LinkedList <Set<Integer>> proposals = new LinkedList<Set<Integer>>();

        
        if(configPath != null){
            try{
                File file = new File(configPath);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                int nbline = 0 ;
                String line ;
                
                while((line = br.readLine()) != null){
                    String[] words = line.split(" ");

                    if(nbline == 0){
                        nbProposals = Integer.parseInt(words[0]);
                        vs = Integer.parseInt(words[1]);
                        ds = Integer.parseInt(words[2]);

                    }else{
                         if(words.length > vs){
                            System.out.println("The number of elements in the proposal " + words.length + " exceeds the expected number " + vs);;
                            System.exit(1);
                         }else{
                            Set<Integer> proposal =  new HashSet<Integer> ();;
                            for(String word : words){
                                proposal.add(Integer.parseInt(word));
                            }
                            System.out.println(proposal);
                            proposals.add(proposal);
                         }
                    }
                    if(nbline > nbProposals ){
                        System.out.println("The number of proposals " + nbline +" exceeds the expected number " + nbProposals);;
                        System.exit(1);
                    }
                    ++nbline;

                }
                br.close();


            } catch(IOException e ){
                System.out.println("Failed to read configuration file ");
                
            }
        }


        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        //resolve info about my process 
        int myPid = parser.myId();
        Host myHost = null; 
        String outputPath = parser.output();
       
        Constants.hosts = new HashMap<Integer, Host>(); //informations about available hosts

        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
            if(myPid == host.getId()){
                myHost = host;
            }
          
          Constants.hosts.put(host.getId(), host);

        }
        
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

       

        System.out.println("Broadcasting and delivering messages...\n");
        //-----------------------------------------Best Effort BroadCast 
        /*
        beb = new BestEffortBroadcast(myHost, outputPath);
        Message msg = new Message(myHost.getId(),1,false);
        beb.broadcast(msg);
         */

        //-----------------------------------------Uniform reliable broadcast 
        
       /*
        *  urb = new UniformReliableBroadcast(myHost, outputPath);
        //broadcasting receiverId is null
        Message msg = new Message(myHost.getId(),0,myHost.getId(),false);
        msg.setOriginalSenderId(myHost.getId());
            urb.broadcast(msg);
        */
       
        //---------------------------------------------FIFO Broadcast
        /*
         *  fifo = new FiFoBroadcast(myHost, outputPath);
        Message msg = new Message(myHost.getId(), 0, 0, false);
        msg.setOriginalSenderId(myHost.getId());

        fifo.broadcast(msg);
         
         */
       
         
        //------------------------------------------Lattice agreement 

        lattice = new MultiShotLattice(myHost, ds, outputPath, proposals);
        lattice.run();
         
       
         
      
        
       
      
       

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
