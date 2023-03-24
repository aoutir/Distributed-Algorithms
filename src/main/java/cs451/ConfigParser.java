package cs451;
/**
 * Class that gets the path of the configuration file
 */

import java.io.File;

public class ConfigParser {

    private String path;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        return true;
    }

    public String getPath() {
        return path;
    }

}
