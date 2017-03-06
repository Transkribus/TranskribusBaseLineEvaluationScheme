package eu.transkribus.baselineevaluationscheme.util;

////////////////////////////////////////////////
/// File:       MetricResult.java
/// Created:    22.04.2016  11:21:17
/// Encoding:   UTF-8
////////////////////////////////////////////////
import java.util.ArrayList;

/**
 * Desciption of MetricResult
 *
 *
 * Since 22.04.2016
 *
 * @author Tobi <tobias.gruening.hro@gmail.com>
 */
public class BaseLineMetricResult {

    private final ArrayList<double[][]> pageWisePerDistTolTickPerLineRecall;
    private final ArrayList<double[]> pageWisePerDistTolTickRecall;
    private final ArrayList<Double> pageWiseRecall;
    private double recall;
    private final ArrayList<double[][]> pageWisePerDistTolTickPerLinePrecision;
    private final ArrayList<double[]> pageWisePerDistTolTickPrecision;
    private final ArrayList<Double> pageWisePrecision;
    private double precision;

    public BaseLineMetricResult() {
        pageWisePerDistTolTickPerLineRecall = new ArrayList<double[][]>();
        pageWisePerDistTolTickRecall = new ArrayList<double[]>();
        pageWiseRecall = new ArrayList<Double>();
        pageWisePerDistTolTickPerLinePrecision = new ArrayList<double[][]>();
        pageWisePerDistTolTickPrecision = new ArrayList<double[]>();
        pageWisePrecision = new ArrayList<Double>();
    }

    /**
     *
     * @param perDistTolTickPerLineRecall #distTolTicks x #truthBaseLines Matrix
     * of recalls
     */
    public void addPerDistTolTickPerLineRecall(double[][] perDistTolTickPerLineRecall) {
        pageWisePerDistTolTickPerLineRecall.add(perDistTolTickPerLineRecall);
        double[] aPageWisePerDistTolTickRecall = new double[perDistTolTickPerLineRecall.length];
        for (int i = 0; i < perDistTolTickPerLineRecall.length; i++) {
            double aVal = 0.0;
            double[] aDistTolTickPerLineRecall = perDistTolTickPerLineRecall[i];
            if (aDistTolTickPerLineRecall == null) {
                aVal = 1.0;
            } else {
                for (int j = 0; j < aDistTolTickPerLineRecall.length; j++) {
                    aVal += aDistTolTickPerLineRecall[j];
                }
                aVal /= aDistTolTickPerLineRecall.length;
            }
            aPageWisePerDistTolTickRecall[i] = aVal;
        }
        pageWisePerDistTolTickRecall.add(aPageWisePerDistTolTickRecall);
        double aVal = 0.0;
        for (int i = 0; i < aPageWisePerDistTolTickRecall.length; i++) {
            aVal += aPageWisePerDistTolTickRecall[i];
        }
        aVal /= aPageWisePerDistTolTickRecall.length;
        pageWiseRecall.add(aVal);
        calcRecall();
    }

    /**
     *
     * @param perDistTolTickPerLinePrecision #distTolTicks x #recoBaseLines
     * Matrix of precisions
     */
    public void addPerDistTolTickPerLinePrecision(double[][] perDistTolTickPerLinePrecision) {
        pageWisePerDistTolTickPerLinePrecision.add(perDistTolTickPerLinePrecision);
        double[] aPageWisePerDistTolTickPrecision = new double[perDistTolTickPerLinePrecision.length];
        for (int i = 0; i < perDistTolTickPerLinePrecision.length; i++) {
            double aVal = 0.0;
            double[] aDistTolTickPerLinePrecision = perDistTolTickPerLinePrecision[i];
            if(aDistTolTickPerLinePrecision == null){
                aVal = 1.0;
            }else{
                for (int j = 0; j < aDistTolTickPerLinePrecision.length; j++) {
                    aVal += aDistTolTickPerLinePrecision[j];
                }
                aVal /= aDistTolTickPerLinePrecision.length;
            }
            aPageWisePerDistTolTickPrecision[i] = aVal;
        }
        pageWisePerDistTolTickPrecision.add(aPageWisePerDistTolTickPrecision);
        double aVal = 0.0;
        for (int i = 0; i < aPageWisePerDistTolTickPrecision.length; i++) {
            aVal += aPageWisePerDistTolTickPrecision[i];
        }
        aVal /= aPageWisePerDistTolTickPrecision.length;
        pageWisePrecision.add(aVal);
        calcPrecision();
    }

    public ArrayList<double[][]> getPageWisePerDistTolTickPerLineRecall() {
        return pageWisePerDistTolTickPerLineRecall;
    }

    public ArrayList<double[]> getPageWisePerDistTolTickRecall() {
        return pageWisePerDistTolTickRecall;
    }

    public ArrayList<Double> getPageWiseRecall() {
        return pageWiseRecall;
    }

    public double getRecall() {
        return recall;
    }

    public ArrayList<double[][]> getPageWisePerDistTolTickPerLinePrecision() {
        return pageWisePerDistTolTickPerLinePrecision;
    }

    public ArrayList<double[]> getPageWisePerDistTolTickPrecision() {
        return pageWisePerDistTolTickPrecision;
    }

    public ArrayList<Double> getPageWisePrecision() {
        return pageWisePrecision;
    }

    public double getPrecision() {
        return precision;
    }

    private void calcRecall() {
        double aVal = 0.0;
        for (Double aRec : pageWiseRecall) {
            aVal += aRec.doubleValue();
        }
        aVal /= pageWiseRecall.size();
        recall = aVal;
    }

    private void calcPrecision() {
        double aVal = 0.0;
        for (Double aPrec : pageWisePrecision) {
            aVal += aPrec.doubleValue();
        }
        aVal /= pageWisePrecision.size();
        precision = aVal;
    }

    public int[][] getPageWiseTrueFalseCntyHypo(double thres) {
        int[][] res = new int[2][pageWisePerDistTolTickPerLinePrecision.size()];
        for (int i = 0; i < pageWisePerDistTolTickPerLinePrecision.size(); i++) {
            int cntTP = 0;
            int cntFP = 0;
            double[][] perDistTolTickPerLinePrecision = pageWisePerDistTolTickPerLinePrecision.get(i);
            if(perDistTolTickPerLinePrecision[0] == null){
                continue;
            }
            double[] avgPerLinePrecision = new double[perDistTolTickPerLinePrecision[0].length];
            for (int j = 0; j < perDistTolTickPerLinePrecision.length; j++) {
                double[] perLinePrecision = perDistTolTickPerLinePrecision[j];
                for (int k = 0; k < perLinePrecision.length; k++) {
                    avgPerLinePrecision[k] += perLinePrecision[k]/perDistTolTickPerLinePrecision.length;
                }
            }
            for (int j = 0; j < avgPerLinePrecision.length; j++) {
                double aV = avgPerLinePrecision[j];
                if(aV >= thres){
                    cntTP++;
                }else{
                    cntFP++;
                }
            }
            res[0][i] = cntTP;
            res[1][i] = cntFP;
        }
        return res;
    }
    
    public int[][] getPageWiseTrueFalseCntsGT(double thres) {
        int[][] res = new int[2][pageWisePerDistTolTickPerLineRecall.size()];
        for (int i = 0; i < pageWisePerDistTolTickPerLineRecall.size(); i++) {
            int cntTN = 0;
            int cntFN = 0;
            double[][] perDistTolTickPerLineRecall = pageWisePerDistTolTickPerLineRecall.get(i);
            if(perDistTolTickPerLineRecall[0] == null){
                continue;
            }
            double[] avgPerLineRecall = new double[perDistTolTickPerLineRecall[0].length];
            for (int j = 0; j < perDistTolTickPerLineRecall.length; j++) {
                double[] perLineRecall = perDistTolTickPerLineRecall[j];
                for (int k = 0; k < perLineRecall.length; k++) {
                    avgPerLineRecall[k] += perLineRecall[k]/perDistTolTickPerLineRecall.length;
                }
            }
            for (int j = 0; j < avgPerLineRecall.length; j++) {
                double aV = avgPerLineRecall[j];
                if(aV >= thres){
                    cntTN++;
                }else{
                    cntFN++;
                }
            }
            res[0][i] = cntTN;
            res[1][i] = cntFN;
        }

        return res;
    }
    
    public boolean[][] getSpecificPageTrueFalseConstellation(int pageNum, double thres) {
        boolean[][] res = new boolean[2][];
        double[][] perDistTolTickPerLineRecall = pageWisePerDistTolTickPerLineRecall.get(pageNum);
        if(perDistTolTickPerLineRecall[0] != null){
            boolean[] gtC = new boolean[perDistTolTickPerLineRecall[0].length];
            res[1] = gtC;
            double[] avgPerLineRecall = new double[perDistTolTickPerLineRecall[0].length];
            for (int j = 0; j < perDistTolTickPerLineRecall.length; j++) {
                double[] perLineRecall = perDistTolTickPerLineRecall[j];
                for (int k = 0; k < perLineRecall.length; k++) {
                    avgPerLineRecall[k] += perLineRecall[k]/perDistTolTickPerLineRecall.length;
                }
            }
            for (int j = 0; j < avgPerLineRecall.length; j++) {
                double aV = avgPerLineRecall[j];
                if(aV >= thres){
                    gtC[j] = true;
                }else{
                    gtC[j] = false;
                }
            }
        }
        double[][] perDistTolTickPerLinePrecision = pageWisePerDistTolTickPerLinePrecision.get(pageNum);
        if(perDistTolTickPerLinePrecision[0] != null){
            boolean[] hyC = new boolean[perDistTolTickPerLinePrecision[0].length];
            res[0] = hyC;
            double[] avgPerLinePrecision = new double[perDistTolTickPerLinePrecision[0].length];
            for (int j = 0; j < perDistTolTickPerLinePrecision.length; j++) {
                double[] perLinePrecision = perDistTolTickPerLinePrecision[j];
                for (int k = 0; k < perLinePrecision.length; k++) {
                    avgPerLinePrecision[k] += perLinePrecision[k]/perDistTolTickPerLinePrecision.length;
                }
            }
            for (int j = 0; j < avgPerLinePrecision.length; j++) {
                double aV = avgPerLinePrecision[j];
                if(aV >= thres){
                    hyC[j] = true;
                }else{
                    hyC[j] = false;
                }
            }
        }
        return res;
    }
    
}
