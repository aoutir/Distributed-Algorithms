package cs451;

import java.util.Map;

/**
 * Class that contains constants of the project 
 */

public class Constants {
    public static final int ARG_LIMIT_CONFIG = 7;
    public static int test = 0 ;

    // indexes for id
    public static final int ID_KEY = 0;
    public static final int ID_VALUE = 1;

    // indexes for hosts
    public static final int HOSTS_KEY = 2;
    public static final int HOSTS_VALUE = 3;

    // indexes for output
    public static final int OUTPUT_KEY = 4;
    public static final int OUTPUT_VALUE = 5;

    // indexes for config
    public static final int CONFIG_VALUE = 6;

    //ids to host intialized at run time 
    public static Map<Integer,Host> hosts ;
}
