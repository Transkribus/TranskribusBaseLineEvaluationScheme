package eu.transkribus.baselinemetrictool;

////////////////////////////////////////////////
/// File:       Metric_run.java
/// Created:    19.04.2016  16:50:06
/// Encoding:   UTF-8
////////////////////////////////////////////////
import eu.transkribus.baselinemetrictool.util.BaseLineMetricResult;
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
        options.addOption("p", "pagewise", false, "pagewise results are shown");
        options.addOption("tol", "tolerance", false, "graphical output of values for different tolerance thresholds");
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
            //Display tolerance dependent values?
            boolean toleranceDisplay = cmd.hasOption("tol");
            //Image to display for plausi check
            String imagePath = "";
            if (cmd.hasOption('i')) {
                imagePath = cmd.getOptionValue('i');
            }

            //Parsing the input to create reco and truth baseline polygon lists
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

            //Evaluate the metric for the given reco and truth baseline polygons
            //Creation of an instance of the metric eval tool
            Metric_BL_eval m_bl = new Metric_BL_eval();

            //For each page the metric is evaluated
            for (int i = 0; i < polyPagesReco.length; i++) {
                Polygon[] recoA = polyPagesReco[i];
                Polygon[] truthA = polyPagesTruth[i];
                m_bl.calcMetricForPageBaseLinePolys(truthA, recoA);
            }

            //Getting the result structure
            BaseLineMetricResult res = m_bl.getRes();

            //Show pagewise results if desired
            if (pagewise) {
                ArrayList<Double> pageWiseRecall = res.getPageWiseRecall();
                ArrayList<Double> pageWisePrecision = res.getPageWisePrecision();
                for (int i = 0; i < pageWiseRecall.size(); i++) {
                    double pageRecall = pageWiseRecall.get(i);
                    double pagePrecision = pageWisePrecision.get(i);
                    double pageFmeas = Util.fmeas(pagePrecision, pageRecall);
                    System.out.println("Page " + i);
                    System.out.println("Avg Precision: " + pagePrecision);
                    System.out.println("Avg Recall: " + pageRecall);
                    System.out.println("Avg F-Measure: " + pageFmeas);
                    System.out.println("");
                }
            }

            System.out.println("");
            System.out.println("#######Final Evaluation#######");
            System.out.println("Avg (over Pages) Avg Precision: " + res.getPrecision());
            System.out.println("Avg (over Pages) Avg Recall: " + res.getRecall());
            System.out.println("Avg (over Pages) Avg F-Measure: " + Util.fmeas(res.getPrecision(), res.getRecall()));

            //Display of values for different tolerances
            if (toleranceDisplay) {
                double[] tolWiseRecall = new double[m_bl.maxTolTicks.length];
                double[] tolWisePrecision = new double[m_bl.maxTolTicks.length];
                double[] tolWiseFmeas = new double[m_bl.maxTolTicks.length];

                ArrayList<double[]> pageWisePerDistTolTickPrecision = res.getPageWisePerDistTolTickPrecision();
                for (double[] aVec : pageWisePerDistTolTickPrecision) {
                    for (int i = 0; i < aVec.length; i++) {
                        tolWisePrecision[i] += aVec[i];
                    }
                }
                for (int i = 0; i < tolWisePrecision.length; i++) {
                    tolWisePrecision[i] /= pageWisePerDistTolTickPrecision.size();
                }

                ArrayList<double[]> pageWisePerDistTolTickRecall = res.getPageWisePerDistTolTickRecall();
                for (double[] aVec : pageWisePerDistTolTickRecall) {
                    for (int i = 0; i < aVec.length; i++) {
                        tolWiseRecall[i] += aVec[i];
                    }
                }
                for (int i = 0; i < tolWiseRecall.length; i++) {
                    tolWiseRecall[i] /= pageWisePerDistTolTickRecall.size();
                    tolWiseFmeas[i] = Util.fmeas(tolWisePrecision[i], tolWiseRecall[i]);
                }

                double[][] valsT = new double[3][];
                valsT[0] = tolWiseRecall;
                valsT[1] = tolWisePrecision;
                valsT[2] = tolWiseFmeas;
                String[] ser = new String[3];
                ser[0] = "Recall";
                ser[1] = "Precision";
                ser[2] = "F-Measure";
                Chart chart = QuickChart.getChart("Tolerance-Chart", "Tolerance Value", "Metric Value", ser, m_bl.maxTolTicks, valsT);
                // Show it
                new SwingWrapper(chart).displayChart("TranskribusBaseLineMetricTool");
            }

            //Display of the plausi image
            if (!"".equals(imagePath)) {
                Util.plotPlausi(imagePath, polyPagesTruth[0], polyPagesReco[0]);
            }
            
            //Display of avg for thresholded values
            if(thresholdDisplay){
                //ToDo
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
        args[2] = "-p";
        args[3] = "-tol";
        args[4] = "-i";
        args[5] = "src/test/resources/metrEx.png";
//        args = ("--help").split(" ");
        Metric_BL_run erp = new Metric_BL_run();
        erp.run(args);
    }

}
