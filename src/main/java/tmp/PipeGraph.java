package tmp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author braully
 */
public class PipeGraph {

    public static void main(String... args) {

        Option input = new Option("i", "input", true, "input file or directory");
        Options options = new Options();
        options.addOption(input);

        Option cont = new Option("c", "continue", false, "continue from last processing");
        cont.setRequired(false);
        options.addOption(cont);

        Option verb = new Option("v", "verbose", false, "verbose processing");
        options.addOption(verb);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            formatter.printHelp("PipeGraph", options);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("PipeGraph", options);
            System.exit(1);
            return;
        }
    }
}
