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

    private static BFSTmp bfsalg = null;

    public static void main(String... args) {
        Processamento processamento = new Processamento();

        Option input = new Option("i", "input", true, "input file graph");
        Options options = new Options();
        options.addOption(input);

        Option loadprocess = new Option("l", "load", true, "load state process");
        options.addOption(loadprocess);

        Option cont = new Option("c", "continue", false, "continue from last processing");
        cont.setRequired(false);
        options.addOption(cont);

        Option verb = new Option("v", "verbose", false, "verbose processing");
        options.addOption(verb);

        Option poss = new Option("p", "possibility", false, "check possiblities");
        options.addOption(poss);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("PipeGraph", options);
            System.exit(1);
            return;
        }

        String inputFilePath = cmd.getOptionValue("input");
        if (inputFilePath == null) {
            inputFilePath = "/home/strike/Nuvem/nextcloud/Workspace-nuvem/maior-grafo-direto-striped.es";
        }

        processamento.loadGraph(inputFilePath);

        String loadProcess = cmd.getOptionValue("load");
        if (loadProcess != null) {
            processamento.loadCaminho(loadProcess);
        }

        processamento.prepareStart();

        if (cmd.hasOption("continue")) {

        }

        if (cmd.hasOption("possibility")) {

        }

        if (cmd.hasOption("verbose")) {
            processamento.verbose = true;
        }

    }

    ComparatorMap comparatorProfundidade = new ComparatorMap(2);
}
