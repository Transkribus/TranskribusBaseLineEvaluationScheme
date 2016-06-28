package eu.transkribus.baselinemetrictool;

////////////////////////////////////////////////
/// File:       Metric_run.java
/// Created:    19.04.2016  16:50:06
/// Encoding:   UTF-8
////////////////////////////////////////////////


import eu.transkribus.baselinemetrictool.util.MetricResult;
import eu.transkribus.baselinemetrictool.util.Util;
import java.awt.Polygon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

/**
 * Desciption of Metric_run
 *
 *
 * Since 19.04.2016
 *
 * @author Tobi <tobias.gruening.hro@gmail.com>
 */
public class Metric_BL_run {

    private final Options options = new Options();

    public Metric_BL_run() {
        options.addOption("h", "help", false, "show this help");
//        options.addOption("t", "truthpath", true, "path to truth file");
//        options.addOption("r", "recopath", true, "path to reco file");
        options.addOption("p", "pagewise", false, "pagewise results are shown");
        options.addOption("t", "threshold", false, "graphical output of thresholded values");
        options.addOption("i", "imagepath", true, "displays truth and reco baselines of first page in this image");
    }

    private void help() {
        help(null, null);
    }

    private void help(String suffix) {
        help(suffix, null);
    }

    private void help(String suffix, Throwable e) {
        // This prints out some help
        if (suffix != null && !suffix.isEmpty()) {
            suffix = "ERROR:\n" + suffix;
            if (e != null) {
                suffix += "\n" + e.getMessage();
            }
        }
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp(
                "java -jar BaseLineMetricToolURO.jar <truth> <reco>",
                "This method calculates the baseline errors in a precision/recall manner."
                + " As input it requires the truth and reco information."
                + " A basic truth (and reco) file corresponding to a page of document has to be a txt-file (or a page xml). In case of a text file every line in this txt-file corresponds to a baseline polygon and should look like: x1,y1;x2,y2;x3,y3;....;xn,yn."
                + " As arguments (truth, reco) such txt-files (xml-files) OR lst-files (containing per line a path to a basic txt-file [xml-file]) are required."
                + " The order of the truth/reco-files in both lists has to be the same.",
                options,
                
                suffix,
                true
        );
        System.exit(0);
    }

    public void run(String[] args) throws IOException {

        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse(options, args);

            //Help?
            if (cmd.hasOption("h")) {
                help();
            }

            //Pagewise display?
            boolean pagewise = cmd.hasOption('p');
            //Display thresholded values?
            boolean thresholdDisplay = cmd.hasOption('t');
            //Image to display for plausi check
            String imagePath = "";
            if (cmd.hasOption('i')) {
                imagePath = cmd.getOptionValue('i');
            }

            List<String> argList = cmd.getArgList();
            if (argList.size() != 2) {
                help("no arguments given, missing <truth> <reco>.");
            }

            String fileNameTruth = argList.get(0);
            String fileNameReco = argList.get(1);

            ArrayList<String> listTruth = null;
            ArrayList<String> listReco = null;

            if ((fileNameTruth.endsWith(".txt") && fileNameReco.endsWith(".txt")) || (fileNameTruth.endsWith(".xml") && fileNameReco.endsWith(".xml"))) {
                listTruth = new ArrayList<String>();
                listTruth.add(fileNameTruth);
                listReco = new ArrayList<String>();
                listReco.add(fileNameReco);
            }
            
            if (fileNameTruth.endsWith(".lst") && fileNameReco.endsWith(".lst")) {
                listTruth = Util.loadTextFile(fileNameTruth);
                listReco = Util.loadTextFile(fileNameReco);
            }
            
            
            if (listReco == null || listTruth == null) {
                throw new IllegalArgumentException("Reco and/or TruthFile Error.");
            }
            if (listTruth.size() != listReco.size()) {
                throw new IllegalArgumentException("Same Reco and TruthList length required.");
            }

            System.out.println("Number of pages: " + listTruth.size());

            Polygon[][] polyPagesTruth = new Polygon[listTruth.size()][];
            Polygon[][] polyPagesReco = new Polygon[listReco.size()][];

            int numPolyTruth = 0;
            int numPolyReco = 0;

            for (int i = 0; i < polyPagesReco.length; i++) {
                polyPagesTruth[i] = Util.getPolysFromFile(listTruth.get(i));
                if (polyPagesTruth[i] != null) {
                    numPolyTruth += polyPagesTruth[i].length;
                }
                polyPagesReco[i] = Util.getPolysFromFile(listReco.get(i));
                if (polyPagesReco[i] != null) {
                    numPolyReco += polyPagesReco[i].length;
                }
            }
            System.out.println("Number of TruthLines: " + numPolyTruth);
            System.out.println("Number of RecoLines: " + numPolyReco);
            System.out.println("");

            MetricResult blResult = Metric_BL_eval.process(polyPagesTruth, polyPagesReco);

            if (pagewise) {
                for (int i = 0; i < blResult.pageWiseFmeas.length; i++) {
                    double pageWiseRecall = blResult.pageWiseRecall[i];
                    double pageWisePrecision = blResult.pageWisePrecision[i];
                    double pageWiseFmeas = blResult.pageWiseFmeas[i];
                    System.out.println("Page " + i);
                    System.out.println("Avg Precision: " + pageWisePrecision);
                    System.out.println("Avg Recall: " + pageWiseRecall);
                    System.out.println("Avg F-Measure: " + pageWiseFmeas);
                    System.out.println("");
                }
            }

            System.out.println("");
            System.out.println("#######Final Evaluation#######");
            System.out.println("Avg (over Pages) Precision: " + blResult.finPrecision);
            System.out.println("Avg (over Pages) Recall: " + blResult.finRecall);
            System.out.println("Avg (over Pages) F-Measure: " + blResult.finFmeas);

            if (thresholdDisplay) {
                String[] ser = new String[3];
                ser[0] = "Recall";
                ser[1] = "Precision";
                ser[2] = "F-Measure";
                Chart chart = QuickChart.getChart("Sensitivity-Chart", "Threshold", "Value", ser, blResult.xTickThr, blResult.valsThr);
                // Show it
                new SwingWrapper(chart).displayChart("BaseLineMetricToolURO");
            }

            if (!"".equals(imagePath)) {
                Util.plotPlausi(imagePath, polyPagesTruth[0], polyPagesReco[0]);
            }

        } catch (ParseException e) {
            help("Failed to parse comand line properties", e);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        args = new String[6];
        args[0] = "src/test/resources/truth.lst";
        args[1] = "src/test/resources/reco.lst";
//        args[0] = "lineTruth.txt";
//        args[1] = "lineReco8.txt";
        args[2] = "-p";
        args[3] = "-t";
        args[4] = "-i";
        args[5] = "src/test/resources/metrEx.png";
//        args = ("--help").split(" ");
        Metric_BL_run erp = new Metric_BL_run();
//        for (int i = 0; i < 100; i++) {
        erp.run(args);
            
//        }
    }

}
