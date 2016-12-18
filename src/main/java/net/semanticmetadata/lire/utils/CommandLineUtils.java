package net.semanticmetadata.lire.utils;

import java.util.Properties;

/**
 * Created by mlux on 12/16/16.
 */
public class CommandLineUtils {
    /**
     * Defines an argument switch without following value.
     */
    public enum ArgumentValue {
        None;
    }
    /**
     * Parses command line arguments and puts them into a Properties object.
     * @param arguments the arguments passed to the main method
     * @param helpMessage an optional help string, printed on input of "-help"
     * @param mandatoryArguments a list of mandatory arguments, checked for being there.
     * @return
     */
    public static Properties getProperties(String[] arguments, String helpMessage, String[] mandatoryArguments) {
        Properties result = new Properties();
        for (int i = 0; i < arguments.length; i++) {
            String arg = arguments[i];
            // in case of help request
            if (arg.startsWith("-help")) {
                System.out.println(helpMessage);
                System.exit(0);
            } else if (arg.startsWith("-")) { // every other case.
                if (i+1 < arguments.length) {
                    // there seems to be a value
                    if (!arguments[i+1].startsWith("-")) {
                        result.put(arg, arguments[i+1]);
                    } else {
                        result.put(arg, ArgumentValue.None);
                    }
                } else {
                    result.put(arg, ArgumentValue.None);
                }
            }
        }

        if (mandatoryArguments != null) {
            boolean allFine = true;
            StringBuilder sb = new StringBuilder(256);
            for (int i = 0; i < mandatoryArguments.length; i++) {
                String m = mandatoryArguments[i];
                if (!result.containsKey(m)) {
                    allFine = false;
                    sb.append("Mandatory argument " + m + " is not given.\n");
                }
            }
            if (!allFine) {
                System.err.println(sb.toString());
                System.out.println(helpMessage);
                System.exit(0);
            }
        }
        return result;
    }
}
