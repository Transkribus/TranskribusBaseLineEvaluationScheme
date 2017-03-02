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
import java.text.DecimalFormat;
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
import org.primaresearch.io.xml.XmlModelAndValidatorProvider;

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
        options.addOption("minT", true, "minimum tolerance value to be soncidered default is 10");
        options.addOption("maxT", true, "maximum tolerance value to be soncidered default is 30");
        options.addOption("tTF", true, "threshold for precision and recall to make a decission concerning tp, fp, fn, tn; default is -1 (nothing is done); should be between 0 and 1");
        options.addOption("s", false, "save the plausi plot (if activated)");
        options.addOption("r", false, "only evaluate hypo polygons if they are (partly) contained in region polygon (if available)");
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

    public void run(String[] args) throws XmlModelAndValidatorProvider.NoSchemasException {

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
//            int minT = 10;
            int minT = -1;
            if (cmd.hasOption("minT")) {
                minT = Integer.valueOf(cmd.getOptionValue("minT"));
            }
//            int maxT = 30;
            int maxT = -1;
            if (cmd.hasOption("maxT")) {
                maxT = Integer.valueOf(cmd.getOptionValue("maxT"));
            }
            double thresTP = -1;
            if (cmd.hasOption("tTF")) {
                thresTP = Double.valueOf(cmd.getOptionValue("tTF"));
            }
            boolean savePlot = cmd.hasOption("s");
            boolean useRegionPoly = cmd.hasOption("r");

            //Parsing the input to create reco and truth baseline polygon lists
            List<String> argList = cmd.getArgList();
            if (argList.size() != 2) {
                help("no arguments given, missing <truth> <reco>.");
            }

            String fileNameTruth = argList.get(0);
            String fileNameReco = argList.get(1);

            ArrayList<String> listTruth = null;
            ArrayList<String> listReco = null;

            if (fileNameTruth.endsWith(".txt") || fileNameTruth.endsWith(".xml")) {
                listTruth = new ArrayList<String>();
                listTruth.add(fileNameTruth);
            }
            if (fileNameReco.endsWith(".txt") || fileNameReco.endsWith(".xml")) {
                listReco = new ArrayList<String>();
                listReco.add(fileNameReco);
            }

            if (fileNameTruth.endsWith(".lst") && fileNameReco.endsWith(".lst")) {
                try {
                    listTruth = Util.loadTextFile(fileNameTruth);
                    listReco = Util.loadTextFile(fileNameReco);
                } catch (IOException ex) {
                    throw new IllegalArgumentException("Reco and/or TruthFile Error.");
                }
            }

            if (listReco == null || listTruth == null) {
                throw new IllegalArgumentException("Reco and/or TruthFile Error.");
            }
            if (listTruth.size() != listReco.size()) {
                throw new IllegalArgumentException("Same Reco and TruthList length required.");
            }

            Polygon[][] polyPagesTruth = new Polygon[listTruth.size()][];
            Polygon[][] polyPagesReco = new Polygon[listReco.size()][];

            int numPolyTruth = 0;
            int numPolyReco = 0;

            for (int i = 0; i < polyPagesReco.length; i++) {
                List<Polygon> regionPolys = null;

//                    polyPagesTruth[i] = null;
                try {
                    polyPagesTruth[i] = Util.getPolysFromFile(listTruth.get(i), null);
                } catch (IOException ex) {
                    System.out.println(ex + "  " + listTruth.get(i));
                }

                if (useRegionPoly) {
                    regionPolys = Util.getRegionPolysFromFile(listTruth.get(i));
                }

                if (polyPagesTruth[i] != null) {
                    numPolyTruth += polyPagesTruth[i].length;
                }
//                polyPagesReco[i] = null;
                try {
                    polyPagesReco[i] = Util.getPolysFromFile(listReco.get(i), regionPolys);
                } catch (IOException ex) {
                    System.out.println(ex + "  " + listReco.get(i));
                }
                if (polyPagesReco[i] != null) {
                    numPolyReco += polyPagesReco[i].length;
                }
            }

            //Evaluate the metric for the given reco and truth baseline polygons
            //Creation of an instance of the metric eval tool
            Metric_BL_eval m_bl = new Metric_BL_eval(minT, maxT);

            //For each page the metric is evaluated
            for (int i = 0; i < polyPagesReco.length; i++) {
                Polygon[] recoA = polyPagesReco[i];
                Polygon[] truthA = polyPagesTruth[i];
                m_bl.calcMetricForPageBaseLinePolys(truthA, recoA);
            }

            //Getting the result structure
            BaseLineMetricResult res = m_bl.getRes();

            DecimalFormat df = new DecimalFormat("##.####");
            int[][] pageWiseTrueFalsePositives = null;
            int[][] pageWiseTrueFalseNegatives = null;
            if (thresTP > 0.0) {
                pageWiseTrueFalsePositives = res.getPageWiseTrueFalseCntyHypo(thresTP);
                pageWiseTrueFalseNegatives = res.getPageWiseTrueFalseCntsGT(thresTP);
            }
            //Show pagewise results if desired
            if (pagewise) {
                ArrayList<Double> pageWiseRecall = res.getPageWiseRecall();
                ArrayList<Double> pageWisePrecision = res.getPageWisePrecision();
                for (int i = 0; i < pageWiseRecall.size(); i++) {
                    double pageRecall = pageWiseRecall.get(i);
                    double pagePrecision = pageWisePrecision.get(i);
                    double pageFmeas = Util.fmeas(pagePrecision, pageRecall);
                    System.out.println("Page " + i);
                    System.out.println("Avg precision: " + df.format(pagePrecision));
                    System.out.println("Avg recall: " + df.format(pageRecall));
                    System.out.println("Avg f-measure: " + df.format(pageFmeas));
                    if (thresTP > 0.0) {
                        System.out.println("Number of true hypothesis lines for avg precision threshold of " + df.format(thresTP) + " is: " + pageWiseTrueFalsePositives[0][i]);
                        System.out.println("Number of false hypothesis lines for avg precision threshold of " + df.format(thresTP) + " is: " + pageWiseTrueFalsePositives[1][i]);
                        System.out.println("Number of true groundtruth lines for avg recall threshold of " + df.format(thresTP) + " is: " + pageWiseTrueFalseNegatives[0][i]);
                        System.out.println("Number of false groundtruth lines for avg recall threshold of " + df.format(thresTP) + " is: " + pageWiseTrueFalseNegatives[1][i]);
                    }
                    System.out.println("");
                }
            }

            System.out.println("");
            System.out.println("#######Final Evaluation#######");
            System.out.println("Number of pages: " + listTruth.size());
            System.out.println("Number of groundtruth lines: " + numPolyTruth);
            System.out.println("Number of hypothesis lines: " + numPolyReco);
            System.out.println("");
            System.out.println("Avg (over pages) avg precision: " + df.format(res.getPrecision()));
            System.out.println("Avg (over pages) avg recall: " + df.format(res.getRecall()));
            System.out.println("Avg (over pages) avg f-measure: " + df.format(Util.fmeas(res.getPrecision(), res.getRecall())));
            System.out.println("");
            if (thresTP > 0.0) {
                //Get global tP, fP
                int tP = 0;
                int fP = 0;
                int tN = 0;
                int fN = 0;
                for (int i = 0; i < pageWiseTrueFalsePositives[0].length; i++) {
                    tP += pageWiseTrueFalsePositives[0][i];
                    fP += pageWiseTrueFalsePositives[1][i];
                    tN += pageWiseTrueFalseNegatives[0][i];
                    fN += pageWiseTrueFalseNegatives[1][i];
                }
                System.out.println("Number of true hypothesis lines for avg precision threshold of " + df.format(thresTP) + " is: " + tP);
                System.out.println("Number of false hypothesis lines for avg precision threshold of " + df.format(thresTP) + " is: " + fP);
                System.out.println("Number of true groundtruth lines for avg recall threshold of " + df.format(thresTP) + " is: " + tN);
                System.out.println("Number of false groundtruth lines for avg recall threshold of " + df.format(thresTP) + " is: " + fN);
            }

            //Display of values for different tolerances
            if (toleranceDisplay) {
                if(minT >= 0){
                    double[] tolWiseRecall = new double[m_bl.getMaxTols().length];
                    double[] tolWisePrecision = new double[m_bl.getMaxTols().length];
                    double[] tolWiseFmeas = new double[m_bl.getMaxTols().length];

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
                    Chart chart = QuickChart.getChart("Tolerance-Chart", "Tolerance Value", "Metric Value (avg over pages)", ser, m_bl.getMaxTols(), valsT);
                    // Show it
                    new SwingWrapper(chart).displayChart("TranskribusBaseLineMetricTool - ToleranceValue");
                }else{
                    System.out.println("The Tolerance-Chart is not available for dynamic tolerance value computation! ");
                }
                
            }

            //Display of the plausi image
            if (!"".equals(imagePath)) {
                try {
                    Util.plotPlausi(imagePath, polyPagesTruth[0], polyPagesReco[0], res, thresTP, savePlot);
                } catch (IOException ex) {
                    System.out.println(ex + " " + imagePath);
                }
            }

            //Display of avg for thresholded values
            if (thresholdDisplay) {
                double[] thTicks = new double[101];
                for (int i = 0; i < thTicks.length; i++) {
                    thTicks[i] = 0.01 * i;
                }
                ArrayList<double[][]> pageWisePerDistTolTickPerLinePrecision = res.getPageWisePerDistTolTickPerLinePrecision();
                ArrayList<double[][]> pageWisePerDistTolTickPerLineRecall = res.getPageWisePerDistTolTickPerLineRecall();

                double[] tickPrec = new double[101];
                double[] tickRec = new double[101];
                double[] tickFmeas = new double[101];

                for (int i = 0; i < thTicks.length; i++) {
                    double tick = thTicks[i];

                    for (double[][] aV : pageWisePerDistTolTickPerLinePrecision) {
                        double pageTickPrec = 0.0;
                        int cnt = 0;
                        for (int j = 0; j < aV.length; j++) {
                            double[] aVa = aV[j];
                            if (aVa != null) {
                                for (int k = 0; k < aVa.length; k++) {
                                    cnt++;
                                    double aVal = aVa[k];
                                    if (aVal > tick || aVal == 1.0) {
                                        pageTickPrec++;
                                    }
                                }
                            } else {
                                pageTickPrec++;
                                cnt++;
                            }
                        }
                        pageTickPrec /= cnt;
                        tickPrec[i] += pageTickPrec;
                    }
                    tickPrec[i] /= pageWisePerDistTolTickPerLinePrecision.size();

                    for (double[][] aV : pageWisePerDistTolTickPerLineRecall) {
                        double pageTickRec = 0.0;
                        int cnt = 0;
                        for (int j = 0; j < aV.length; j++) {
                            double[] aVa = aV[j];
                            if (aVa != null) {
                                for (int k = 0; k < aVa.length; k++) {
                                    cnt++;
                                    double aVal = aVa[k];
                                    if (aVal > tick || aVal == 1.0) {
                                        pageTickRec++;
                                    }
                                }
                            } else {
                                pageTickRec++;
                                cnt++;
                            }
                        }
                        pageTickRec /= cnt;
                        tickRec[i] += pageTickRec;
                    }
                    tickRec[i] /= pageWisePerDistTolTickPerLineRecall.size();

                    tickFmeas[i] = Util.fmeas(tickPrec[i], tickRec[i]);

                }

                double[][] serVals = new double[3][];
                serVals[0] = tickRec;
                serVals[1] = tickPrec;
                serVals[2] = tickFmeas;

                String[] ser = new String[3];
                ser[0] = "Recall";
                ser[1] = "Precision";
                ser[2] = "F-Measure";
                Chart chart = QuickChart.getChart("Thresholded-Chart", "Threshold Value", "Metric Value (avg over pages)", ser, thTicks, serVals);
                // Show it
                new SwingWrapper(chart).displayChart("TranskribusBaseLineMetricTool - Thresholded");
            }
        } catch (ParseException e) {
            help("Failed to parse comand line properties", e);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, XmlModelAndValidatorProvider.NoSchemasException {

//        args = new String[10];
//        args[0] = "src/test/resources/truth.lst";
//        args[1] = "src/test/resources/reco.lst";
//        args[2] = "-tol";
//        args[3] = "-i";
//        args[4] = "src/test/resources/metrEx.png";
//        args[5] = "-t";
//        args[6] = "-p";
//        args[7] = "-tTF";
//        args[8] = "0.8";
//        args[9] = "-r";

        Metric_BL_run erp = new Metric_BL_run();
        erp.run(args);
    }
}
