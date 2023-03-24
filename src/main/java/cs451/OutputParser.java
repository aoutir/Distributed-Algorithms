package cs451;

import java.io.File;
/**
 * Class that reads the output field, 
 * Gets the path of the output file 
 */

public class OutputParser {

    private static final String OUTPUT_KEY = "--output";

    private String path;

    public boolean populate(String key, String value) {
        if (!key.equals(OUTPUT_KEY)) {
            return false;
        }

        File file = new File(value);
        path = file.getPath();
        return true;
    }

    public String getPath() {
        return path;
    }

}
