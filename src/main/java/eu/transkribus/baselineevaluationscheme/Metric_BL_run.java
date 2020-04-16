package eu.transkribus.baselineevaluationscheme;

////////////////////////////////////////////////
/// File:       Metric_run.java
/// Created:    19.04.2016  16:50:06
/// Encoding:   UTF-8
////////////////////////////////////////////////
import eu.transkribus.baselineevaluationscheme.util.BaseLineMetricResult;
import eu.transkribus.baselineevaluationscheme.util.LoadResult;
import eu.transkribus.baselineevaluationscheme.util.Util;
import java.awt.Polygon;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * @author Tobias Gruening tobias.gruening.hro@gmail.com
 */
public class Metric_BL_run {

    private final Options options = new Options();
    BaseLineMetricResult res;

    public Metric_BL_run() {
        options.addOption("h", "help", false, "show this help");
        options.addOption("tol", "tolerance", false, "graphical output of values for different tolerance thresholds");
        options.addOption("t", "threshold", false, "graphical output of thresholded values");
        options.addOption("i", "imagepath", true, "displays truth and reco baselines of first page in this image");
        options.addOption("minT", true, "minimum tolerance value to be concidered default is -1 -> dynamic calculation");
        options.addOption("maxT", true, "maximum tolerance value to be concidered default is -1 -> dynamic calculation");
        options.addOption("tTF", true, "threshold for P and R value to make a decision concerning tp, fp, fn, tn; default is -1 (nothing is done); should be between 0 and 1");
        options.addOption("s", false, "save the plausi plot (if activated)");
        options.addOption("r", false, "only evaluate hypo polygons if they are (partly) contained in region polygon (if available)");
        options.addOption("no_s", false, "Don't save the evaluation results.");
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
                "java -jar TranskribusBaselineEvaluationScheme-X.X.X-jar-with-dependencies.jar <truth> <reco>",
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

            //Don't save results?
            boolean notSave = cmd.hasOption("no_s");
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

            boolean[] toSkipBecauseOfError = new boolean[polyPagesReco.length];
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss ");
            Date currentTime = new Date();
            String date = formatter.format(currentTime);
            sb.append("---TranskribusBaseLineEvaluationScheme--- \n");
            sb.append("\n");
            sb.append("Evaluation performed on " + date + "\n");
            sb.append("Evaluation performed for GT " + fileNameTruth + "\n");
            sb.append("Evaluation performed for HYPO " + fileNameReco + "\n");
            sb.append("Number of pages: " + listTruth.size() + "\n");
            sb.append("\n");
            sb.append("Loading protocol: " + "\n");

            for (int i = 0; i < polyPagesReco.length; i++) {
                toSkipBecauseOfError[i] = false;
                List<Polygon> regionPolys = null;
//                    polyPagesTruth[i] = null;
                try {
                    LoadResult polysFromFile = Util.getPolysFromFile(listTruth.get(i), null);
                    polyPagesTruth[i] = polysFromFile.getPolys();
                    if (polysFromFile.isError()) {
                        sb.append("   Error loading: " + listTruth.get(i) + "\n");
                        toSkipBecauseOfError[i] = true;
                    }
                } catch (IOException ex) {
                    sb.append("   Error loading: " + listTruth.get(i) + "\n");
                    toSkipBecauseOfError[i] = true;
                }

                if (useRegionPoly) {
                    regionPolys = Util.getRegionPolysFromFile(listTruth.get(i));
                }
                try {
                    LoadResult polysFromFile = Util.getPolysFromFile(listReco.get(i), regionPolys);
                    polyPagesReco[i] = polysFromFile.getPolys();
                    if (polysFromFile.isError()) {
                        sb.append("   Error loading: " + listReco.get(i) + "\n");
                        toSkipBecauseOfError[i] = true;
                    }
                } catch (IOException ex) {
                    sb.append("   Error loading: " + listReco.get(i) + "\n");
                    toSkipBecauseOfError[i] = true;
                }
                if (polyPagesTruth[i] != null && !toSkipBecauseOfError[i]) {
                    numPolyTruth += polyPagesTruth[i].length;
                }
                if (polyPagesReco[i] != null && !toSkipBecauseOfError[i]) {
                    numPolyReco += polyPagesReco[i].length;
                }
            }

            int errorPages = 0;
            for (boolean aErr : toSkipBecauseOfError) {
                if (aErr) {
                    errorPages++;
                }
            }

            if (errorPages == 0) {
                sb.append("   Everything loaded without errors." + "\n");
            }
            sb.append("\n");
            sb.append(listTruth.size() - errorPages + " out of " + listTruth.size() + " GT-HYPO pairs loaded without errors and used for evaluation." + "\n");
            sb.append("\n");
            sb.append("Number of groundtruth lines: " + numPolyTruth + "\n");
            sb.append("Number of hypothesis lines: " + numPolyReco + "\n");
            sb.append("\n");
            //Evaluate the metric for the given reco and truth baseline polygons
            //Creation of an instance of the metric eval tool
            Metric_BL_eval m_bl = new Metric_BL_eval(minT, maxT);

            //For each page the metric is evaluated
            for (int i = 0; i < polyPagesReco.length; i++) {
                if (toSkipBecauseOfError[i]) {
                    continue;
                }
                Polygon[] recoA = polyPagesReco[i];
                Polygon[] truthA = polyPagesTruth[i];
                m_bl.calcMetricForPageBaseLinePolys(truthA, recoA);
            }

            //Getting the result structure
//            BaseLineMetricResult res = m_bl.getRes();
            res = m_bl.getRes();

//            sb.append("Number of correctly loaded pages: " + );
            DecimalFormat df = new DecimalFormat("##.####");
            int[][] pageWiseTrueFalsePositives = null;
            int[][] pageWiseTrueFalseNegatives = null;
            if (thresTP > 0.0) {
                pageWiseTrueFalsePositives = res.getPageWiseTrueFalseCntyHypo(thresTP);
                pageWiseTrueFalseNegatives = res.getPageWiseTrueFalseCntsGT(thresTP);
            }
            //Show pagewise results if desired
            ArrayList<Double> pageWiseRecall = res.getPageWiseRecall();
            ArrayList<Double> pageWisePrecision = res.getPageWisePrecision();
            sb.append("Pagewise evaluation :" + "\n");
            sb.append("#P value, #R value, #F_1 value, #TruthFileName, #HypoFileName" + "\n");

            int resCnt = 0;
            for (int i = 0; i < polyPagesReco.length; i++) {
                if (toSkipBecauseOfError[i]) {
                    sb.append("      X            X           X  , " + listTruth.get(i) + ", " + listReco.get(i) + "\n");
                } else {
                    double pageRecall = pageWiseRecall.get(resCnt);
                    double pagePrecision = pageWisePrecision.get(resCnt);
                    double pageFmeas = Util.fmeas(pagePrecision, pageRecall);
                    sb.append(String.format("%10.4f", pagePrecision).replace(",", ".") + ", " + String.format("%10.4f", pageRecall).replace(",", ".") + ", " + String.format("%10.4f", pageFmeas).replace(",", ".") + ", " + listTruth.get(i) + ", " + listReco.get(i) + "\n");
                    resCnt++;
                }

            }
            sb.append("\n");
            sb.append("\n");
            sb.append("#######Final Evaluation#######" + "\n");
            sb.append("\n");
            sb.append("Avg (over pages) P value: " + df.format(res.getPrecision()).replace(",", ".") + "\n");
            sb.append("Avg (over pages) R value: " + df.format(res.getRecall()).replace(",", ".") + "\n");
            sb.append("Resulting F_1 value: " + df.format(Util.fmeas(res.getPrecision(), res.getRecall())).replace(",", ".") + "\n");
            sb.append("\n");
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
                sb.append("Number of true hypothesis lines for avg P value threshold of " + df.format(thresTP).replace(",", ".") + " is: " + tP + "\n");
                sb.append("Number of false hypothesis lines for avg P value threshold of " + df.format(thresTP).replace(",", ".") + " is: " + fP + "\n");
                sb.append("Number of true groundtruth lines for avg R value threshold of " + df.format(thresTP).replace(",", ".") + " is: " + tN + "\n");
                sb.append("Number of false groundtruth lines for avg R value threshold of " + df.format(thresTP).replace(",", ".") + " is: " + fN + "\n");
                sb.append("\n");
            }
            String finalEvaluation = sb.toString();

            System.out.println(finalEvaluation);

            if (!notSave) {
                //Write final evaluation in a *.txt-file
                formatter = new SimpleDateFormat("yyyy_MM_dd_'at'_HH_mm_ss");
                date = formatter.format(currentTime);
                String fileName = "evaluation_" + date + ".txt";
                BufferedWriter writer = null;
                try {
                    //create a temporary file
                    File logFile = new File(fileName);

                    writer = new BufferedWriter(new FileWriter(logFile));
                    writer.write(finalEvaluation);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        // Close the writer regardless of what happens...
                        writer.close();
                    } catch (Exception e) {
                    }
                }
            }

            //Display of values for different tolerances
            if (toleranceDisplay) {
                if (minT >= 0) {
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
                    ser[0] = "R value";
                    ser[1] = "P value";
                    ser[2] = "F_1 value";
                    Chart chart = QuickChart.getChart("Tolerance-Chart", "Tolerance Value", "Evaluation Value (avg over pages)", ser, m_bl.getMaxTols(), valsT);
                    // Show it
                    new SwingWrapper(chart).displayChart("TranskribusBaseLineEvaluationScheme - ToleranceValue");
                } else {
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
                ser[0] = "R value";
                ser[1] = "P value";
                ser[2] = "F_1 value";
                Chart chart = QuickChart.getChart("Thresholded-Chart", "Threshold Value", "Evaluation Value (avg over pages)", ser, thTicks, serVals);
                // Show it
                new SwingWrapper(chart).displayChart("TranskribusBaseLineEvaluationScheme - Thresholded");
            }
        } catch (ParseException e) {
            help("Failed to parse comand line properties", e);
        }
    }
    
    public BaseLineMetricResult getRes() { 
    	return res;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, XmlModelAndValidatorProvider.NoSchemasException {

//        args = new String[7];
//        args[0] = "src/test/resources/truth.lst";
//        args[1] = "src/test/resources/reco.lst";
//        args[2] = "-i";
//        args[3] = "src/test/resources/metrEx.png";
//        args[4] = "-t";
//        args[5] = "-r";
//        args[6] = "-no_s";
//
        Metric_BL_run erp = new Metric_BL_run();
        erp.run(args);
    }
}
