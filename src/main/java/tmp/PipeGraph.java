package tmp;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author braully
 */
public class PipeGraph {
    
    static final IGenStrategy[] operations = new IGenStrategy[]{
        new StrategyEstagnacao(),
        new StrategyAvoidCollid(),
        new StrategyBlock()
    };
    
    public static void main(String... args) {
        Processamento processamento = new Processamento();
        
        Option input = new Option("i", "input", true, "input file graph");
        Options options = new Options();
        options.addOption(input);
        
        Option loadprocess = new Option("c", "continue", true, "continue process from comb state");
        loadprocess.setRequired(false);
        options.addOption(loadprocess);
        
        Option mergecontinue = new Option("mc", "merge-continue", true, "continue process from comb state");
        mergecontinue.setRequired(false);
        options.addOption(mergecontinue);
        
        Option loadstart = new Option("l", "load-start", false, "load start information");
        loadstart.setRequired(false);
        options.addOption(loadstart);
        
        Option verb = new Option("v", "verbose", false, "verbose processing");
        options.addOption(verb);
        
        Option verbrank = new Option("vr", "verbose-ranking", false, "verbose ranking");
        options.addOption(verbrank);
        
        Option verbinit = new Option("vi", "verbose-init", false, "verbose initial possibilities");
        options.addOption(verbinit);
        
        Option poss = new Option("cp", "check-possibility", false, "check possiblities");
        options.addOption(poss);
        
        Option sanitize = new Option("s", "sanitize", false, "sanitizar grafo");
        options.addOption(sanitize);
        
        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);
        
        Option commitfail = new Option("cf", "commit-fail", true, "Commit count fail");
        commitfail.setRequired(false);
        options.addOption(commitfail);
        
        Option ranking = new Option("r", "ranking", true, "Ranking profundidade");
        ranking.setRequired(false);
        options.addOption(ranking);
        
        Option rollbackfail = new Option("rf", "rollback-fail", true, "Rollback count fail");
        rollbackfail.setRequired(false);
        options.addOption(rollbackfail);
        
        Option parallel = new Option("p", "parallel", true, "Parallel process");
        parallel.setRequired(false);
        options.addOption(parallel);
        
        Option merge = new Option("mc", "merege-continue", true, "merge multiple continue process from comb state");
        loadprocess.setRequired(false);
        options.addOption(merge);
        
        OptionGroup exec = new OptionGroup();
        exec.setRequired(false);
        IGenStrategy[] opers = operations;
        Option[] execs = new Option[opers.length];
        for (int i = 0; i < opers.length; i++) {
            IGenStrategy oper = opers[i];
            execs[i] = new Option("" + i, false, oper.getName());
            options.addOption(execs[i]);
        }
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("PipeGraph", options);
            return;
        }
        
        if (cmd.hasOption("verbose")) {
            processamento.verbose = true;
        }
        if (cmd.hasOption("verbose-ranking")) {
            processamento.verboseRankingOption = true;
        }
        if (cmd.hasOption("verbose-init")) {
            processamento.vebosePossibilidadesIniciais = true;
        }
        if (cmd.hasOption("verbose-fim")) {
            processamento.veboseFimEtapa = true;
        }
        
        String strfailcom = cmd.getOptionValue("commit-fail");
        if (strfailcom != null && !strfailcom.isEmpty()) {
            processamento.falhaInCommitCount = true;
            processamento.falhaCommitCount = Integer.parseInt(strfailcom.trim());
        }
        
        String strrollbackfail = cmd.getOptionValue("rollback-fail");
        if (strrollbackfail != null && !strrollbackfail.isEmpty()) {
            processamento.falhaInRollBack = true;
            processamento.falhaRollbackCount = Integer.parseInt(strrollbackfail.trim());
        }
        
        String inputFilePath = cmd.getOptionValue("input");
        if (inputFilePath == null) {
            inputFilePath = "/home/strike/Workspace/Workspace-nuvem/maior-grafo-direto-striped.es";
        }
        
        if (inputFilePath == null || inputFilePath.isEmpty()) {
            formatter.printHelp("PipeGraph", options);
            System.out.println("input is requeried");
            return;
        }
        
        String stranking = cmd.getOptionValue("ranking");
        if (stranking != null && !stranking.isEmpty()) {
            processamento.rankearOpcoes = true;
            processamento.rankearOpcoesProfundidade = Integer.parseInt(stranking.replaceAll("\\D", "").trim());
        }
        
        processamento.loadGraph(inputFilePath);
        if (cmd.hasOption("load-start")) {
            System.out.print("Loading from cache");
            processamento.loadStartFromCache();
            System.out.print("...Ok");
        }
        processamento.prepareStart();
        
        String loadProcess = cmd.getOptionValue("continue");
        if (loadProcess != null) {
            processamento.loadCaminho(loadProcess);
            System.out.println("...Ok");
        }
        
        if (cmd.hasOption("check-possibility")) {
            processamento.recheckPossibilities();
            System.out.println("Graph...Ok");
        }
        
        List<IGenStrategy> operationsToExecute = new ArrayList<IGenStrategy>();
        for (int i = 0; i < opers.length; i++) {
            IGenStrategy oper = opers[i];
            String value = execs[i].getOpt();
            if (cmd.hasOption(value)) {
                operationsToExecute.add(oper);
                processamento.recheckPossibilities();
            }
        }
        
        String strparallel = cmd.getOptionValue("parallel");
        if (strparallel != null && !strparallel.isEmpty()) {
            processamento.ordenarTrabalhoPorCaminhosPossiveis();
            UtilTmp.printCurrentItme();
            /* */
            List<TrabalhoProcessamento> processos = new ArrayList<>();
            Integer numThreads = Integer.parseInt(strparallel.trim().replaceAll("\\D", ""));
            int size = processamento.trabalhoPorFazer.size();
            numThreads = Integer.min(size, numThreads);
            for (Integer i = 0; i < numThreads; i++) {
                processos.add(new TrabalhoProcessamento(i));
            }
            processos.parallelStream().forEach(p -> p.generateGraph(processamento.fork()));
        }
        
        String mc = cmd.getOptionValue("merge-continue");
        if (mc != null) {
            Processamento subprocessamento = processamento.fork();
        }

//        if (operationsToExecute.isEmpty()) {
//            operationsToExecute.add(opers[0]);
//        }
        for (IGenStrategy operation : operationsToExecute) {
            operation.generateGraph(processamento);
        }
    }
}
