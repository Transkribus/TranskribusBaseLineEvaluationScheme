package eu.transkribus.baselineevaluationscheme;

////////////////////////////////////////////////
/// File:       Metric_BL_eval.java
/// Created:    22.04.2016  11:16:20
/// Encoding:   UTF-8
////////////////////////////////////////////////
import eu.transkribus.baselineevaluationscheme.util.BaseLineMetricResult;
import eu.transkribus.baselineevaluationscheme.util.Util;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * Desciption of Metric_BL_eval
 *
 *
 * Since 22.04.2016
 *
 * @author Tobi <tobias.gruening.hro@gmail.com>
 */
public class Metric_BL_eval {

    private double[] maxTols;
    private int desPolyTickDist;
    private BaseLineMetricResult res;
    
    
    private double[][] truthLineTols;
    private double relTol = 0.25;
    

    /**
     * Default constructor with minTol = 10, maxTol = 30 and desPolyTickDist = 5
     * .
     */
    public Metric_BL_eval() {
        maxTols = new double[30 - 10 + 1];
        for (int i = 0; i < maxTols.length; i++) {
            maxTols[i] = 10 + i;
        }
        res = new BaseLineMetricResult();
        desPolyTickDist = 5;
    }

    /**
     *
     * @param minTol - MINIMUM distance tolerance which is not penalized
     * @param maxTol - MAXIMUM distance tolerance which is not penalized
     */
    public Metric_BL_eval(int minTol, int maxTol) {
        maxTols = new double[maxTol - minTol + 1];
        for (int i = 0; i < maxTols.length; i++) {
            maxTols[i] = minTol + i;
        }
        res = new BaseLineMetricResult();
        desPolyTickDist = 5;
    }

    /**
     *
     * @param minTol - MINIMUM distance tolerance which is not penalized
     * @param maxTol - MAXIMUM distance tolerance which is not penalized
     * @param desPolyTickDist - Desired distance of points of the baseline
     * polygons
     */
    public Metric_BL_eval(int minTol, int maxTol, int desPolyTickDist) {
        this(minTol, maxTol);
        this.desPolyTickDist = desPolyTickDist;
    }

    public BaseLineMetricResult getRes() {
        return res;
    }

    /**
     * Calculates the BaseLineMetric Stats for truth and reco polygons of a
     * single page, and adds the result to the MetricResult Structure
     *
     * @param polyTruth Array of TRUTH Polygons corresponding to a single page
     * @param polyReco Array of RECO Polygons corresponding to a single page
     */
    public void calcMetricForPageBaseLinePolys(Polygon[] polyTruth, Polygon[] polyReco) {

        
        
        
        double[][] precision = new double[maxTols.length][];
        double[][] recall = new double[maxTols.length][];
        //Take care of degenerated scenarios
        if (polyTruth == null || polyTruth.length == 0 || polyReco == null || polyReco.length == 0) {
            if (polyTruth == null || polyTruth.length == 0) {
                if (polyReco == null || polyReco.length == 0) {
                    for (int i = 0; i < maxTols.length; i++) {
                        precision[i] = null;
                        recall[i] = null;
                    }
                } else {
                    for (int i = 0; i < maxTols.length; i++) {
                        precision[i] = new double[polyReco.length];
                        recall[i] = null;
                    }
                }
            } else {
                for (int i = 0; i < maxTols.length; i++) {
                    precision[i] = null;
                    recall[i] = new double[polyTruth.length];
                }
            }

        } else {
            //Normalise the baselines, that poly points have a desired "distance"
            Polygon[] polysTruthNorm = Util.normDesDist(polyTruth, desPolyTickDist);
            Polygon[] polysRecoNorm = Util.normDesDist(polyReco, desPolyTickDist);

            truthLineTols = new double[polysTruthNorm.length][];
            if (maxTols[0] < 0) {
                double[] tolsCalc = Util.calcTols(polysTruthNorm, desPolyTickDist, 250, relTol);
                for (int i = 0; i < truthLineTols.length; i++) {
                    truthLineTols[i] = new double[1];
                    truthLineTols[i][0] = tolsCalc[i];
                }
            }else{
                for (int i = 0; i < polysTruthNorm.length; i++) {
                    truthLineTols[i] = maxTols;
                }
            }

            //for each truthPoly calculate the recall values for all tolerances
            recall = calcRecall(recall, polysRecoNorm, polysTruthNorm);
            //for each recoPoly calculate the precission values for all tolerances
            precision = calcPrecision(precision, polysRecoNorm, polysTruthNorm);
        }
        res.addPerDistTolTickPerLinePrecision(precision);
        res.addPerDistTolTickPerLineRecall(recall);
        truthLineTols = null;
    }

    private double[][] calcPrecision(double[][] precision, Polygon[] polyRecoNorm, Polygon[] polyTruthNorm) {
        //initialize precision values
        for (int i = 0; i < maxTols.length; i++) {
            precision[i] = new double[polyRecoNorm.length];
        }

        double[][][] C = new double[maxTols.length][polyRecoNorm.length][polyTruthNorm.length];
        for (int i = 0; i < polyRecoNorm.length; i++) {
            for (int j = 0; j < polyTruthNorm.length; j++) {
                double[] cntRelHits = cntRelHits(polyRecoNorm[i], polyTruthNorm[j], truthLineTols[j]);
                for (int k = 0; k < cntRelHits.length; k++) {
                    double cntRelHit = cntRelHits[k];
                    C[k][i][j] = cntRelHit;
                }
            }
        }
        //Calculation of the Alignment
        for (int i = 0; i < C.length; i++) {
            double[][] aC = C[i];
            while (true) {
                int[] maxIdx = getMaxIdx(aC);
                if (maxIdx[0] < 0) {
                    break;
                }
                precision[i][maxIdx[0]] = aC[maxIdx[0]][maxIdx[1]];

                for (int j = 0; j < aC.length; j++) {
                    aC[j][maxIdx[1]] = 0.0;
                }
                for (int j = 0; j < aC[0].length; j++) {
                    aC[maxIdx[0]][j] = 0.0;
                }
            }
        }
        return precision;
    }

    private int[] getMaxIdx(double[][] C) {
        double mV = 0.0;
        int maxRow = -1;
        int maxCol = -1;

        for (int i = 0; i < C.length; i++) {
            double[] aC = C[i];
            for (int j = 0; j < aC.length; j++) {
                double aV = aC[j];
                if (aV > mV) {
                    mV = aV;
                    maxRow = i;
                    maxCol = j;
                }
            }
        }
        return new int[]{maxRow, maxCol};
    }

    private double[] cntRelHits(Polygon toCnt, Polygon ref, double[] tols) {
        double[] cnt = new double[tols.length];
        Rectangle toCntBB = toCnt.getBounds();
        Rectangle refBB = ref.getBounds();
        Rectangle inter = toCntBB.intersection(refBB);
        int minI = Math.min(inter.width, inter.height);
        //Early stopping criterion
        if (minI < -3.0 * tols[tols.length - 1]) {
            return cnt;
        }
        for (int i = 0; i < toCnt.npoints; i++) {
            int xA = toCnt.xpoints[i];
            int yA = toCnt.ypoints[i];
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < ref.npoints; j++) {
                int xC = ref.xpoints[j];
                int yC = ref.ypoints[j];
//                minDist = Math.min(Math.sqrt((xC - xA) * (xC - xA) + (yC - yA) * (yC - yA)), minDist);
                minDist = Math.min(Math.abs(xA - xC) + Math.abs(yA - yC), minDist);
                if (minDist <= tols[0]) {
                    break;
                }
            }
            for (int j = 0; j < cnt.length; j++) {
                double tol = tols[j];
                if (minDist <= tol) {
                    cnt[j]++;
                }
                if (minDist > tol && minDist < 3.0 * tol) {
                    cnt[j] += (3.0 * tol - minDist) / (2.0 * tol);
                }
            }
        }
        for (int i = 0; i < cnt.length; i++) {
            cnt[i] /= toCnt.npoints;
        }
        return cnt;
    }

    private double[][] calcRecall(double[][] recall, Polygon[] polysRecoNorm, Polygon[] polysTruthNorm) {
        for (int i = 0; i < maxTols.length; i++) {
            recall[i] = new double[polysTruthNorm.length];
        }
        for (int i = 0; i < polysTruthNorm.length; i++) {
            Polygon polyTruthNormA = polysTruthNorm[i];
            double[] cntHitsList = cntRelHitsList(polyTruthNormA, polysRecoNorm, truthLineTols[i]);
            for (int j = 0; j < recall.length; j++) {
                recall[j][i] = cntHitsList[j];
            }
        }

        return recall;
    }

    private double[] cntRelHitsList(Polygon toCnt, Polygon[] refL, double[] tols) {
        double[] cnt = new double[tols.length];
        Rectangle toCntBB = toCnt.getBounds();
        for (int i = 0; i < toCnt.npoints; i++) {
            int xA = toCnt.xpoints[i];
            int yA = toCnt.ypoints[i];
            boolean match = false;
            double minDist = Double.MAX_VALUE;
            for (Polygon ref : refL) {
                Rectangle refBB = ref.getBounds();
                Rectangle inter = toCntBB.intersection(refBB);
                int minI = Math.min(inter.width, inter.height);
                //Early stopping criterion
                if (minI < -3.0 * tols[tols.length - 1]) {
                    continue;
                }
                for (int j = 0; j < ref.npoints; j++) {
                    int xC = ref.xpoints[j];
                    int yC = ref.ypoints[j];
                    minDist = Math.min(Math.abs(xA - xC) + Math.abs(yA - yC), minDist);
//                    minDist = Math.min(Math.sqrt((xC - xA) * (xC - xA) + (yC - yA) * (yC - yA)), minDist);
                    if (minDist <= tols[0]) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    break;
                }
            }

            for (int j = 0; j < cnt.length; j++) {
                double tol = tols[j];
                if (minDist <= tol) {
                    cnt[j]++;
                }
                if (minDist > tol && minDist < 3.0 * tol) {
                    cnt[j] += (3.0 * tol - minDist) / (2.0 * tol);
                }
            }
        }
        for (int i = 0; i < cnt.length; i++) {
            cnt[i] /= toCnt.npoints;
        }
        return cnt;
    }

    public double[] getMaxTols() {
        return maxTols;
    }
    
}
