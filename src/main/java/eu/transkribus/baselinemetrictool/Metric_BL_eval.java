package eu.transkribus.baselinemetrictool;

////////////////////////////////////////////////
/// File:       Metric_BL_eval.java
/// Created:    22.04.2016  11:16:20
/// Encoding:   UTF-8
////////////////////////////////////////////////


import eu.transkribus.baselinemetrictool.util.MetricResult;
import eu.transkribus.baselinemetrictool.util.Util;
import java.awt.Point;
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

    public static MetricResult process(Polygon[][] polyPagesTruth, Polygon[][] polyPagesReco) {

        MetricResult res = new MetricResult();

        double avgOverPagesPrecision = 0.0;
        double avgOverPagesRecall = 0.0;

        double[] avgPerPagePrecision = new double[polyPagesTruth.length];
        double[] avgPerPageRecall = new double[polyPagesTruth.length];
        double[] avgPerPageFmeas = new double[polyPagesTruth.length];

        //Number of ticks for thresholded recall/precission evaluation
        int ticks = 101;
        double[] avgOverPagesRecallThr = new double[ticks];
        double[] avgOverPagesPrecisionThr = new double[ticks];

        for (int i = 0; i < polyPagesTruth.length; i++) {
            Polygon[] polysTruthA = polyPagesTruth[i];
            Polygon[] polysRecoA = polyPagesReco[i];
            double[] precision;
            double[] recall;
            if (polysTruthA == null || polysRecoA == null) {
                if (polysTruthA == null) {
                    if (polysRecoA == null) {
                        precision = new double[]{1.0};
                        recall = new double[]{1.0};
                    } else {
                        precision = new double[]{0.0};
                        recall = new double[]{1.0};
                    }
                } else {
                    precision = new double[]{1.0};
                    recall = new double[]{0.0};
                }

            } else {
                //Normalise the baselines, that poly points have a desired "distance"
                Polygon[] polysTruthBlownA = Util.normDesDist(polysTruthA, 5);
                Polygon[] polysRecoBlownA = Util.normDesDist(polysRecoA, 5);

                //Calc Tolerance Value
                double[] tol = calcTol(polysTruthA, polysTruthBlownA);

                recall = new double[polysTruthA.length];
                for (int j = 0; j < recall.length; j++) {
                    recall[j] = recall(polysRecoBlownA, polysTruthBlownA[j], tol[j]);
                }
                precision = calcPrecision(polysRecoBlownA, polysTruthBlownA, tol);
            }
            double avgRecall = 0.0;
            for (int j = 0; j < recall.length; j++) {
                avgRecall += recall[j];
            }
            avgRecall /= recall.length;
            avgOverPagesRecall += avgRecall;
            double avgPrecision = 0.0;
            for (int j = 0; j < precision.length; j++) {
                avgPrecision += precision[j];
            }
            avgPrecision /= precision.length;
            avgOverPagesPrecision += avgPrecision;
            for (int j = 0; j < ticks; j++) {
                double thr = (1.0 / (ticks - 1)) * j;
                double aVal = 0.0;
                for (int k = 0; k < recall.length; k++) {
                    if (recall[k] > thr) {
                        aVal++;
                    }
                }
                aVal /= recall.length;
                avgOverPagesRecallThr[j] += aVal;
                aVal = 0.0;
                for (int k = 0; k < precision.length; k++) {
                    if (precision[k] > thr) {
                        aVal++;
                    }
                }
                aVal /= precision.length;
                avgOverPagesPrecisionThr[j] += aVal;
            }

            avgPerPagePrecision[i] = avgPrecision;
            avgPerPageRecall[i] = avgRecall;
            avgPerPageFmeas[i] = fmeas(avgPrecision, avgRecall);
        }

        avgOverPagesRecall /= polyPagesTruth.length;
        avgOverPagesPrecision /= polyPagesTruth.length;
        double avgOverPagesFmeas = fmeas(avgOverPagesPrecision, avgOverPagesRecall);
        res.finRecall = avgOverPagesRecall;
        res.finPrecision = avgOverPagesPrecision;
        res.finFmeas = avgOverPagesFmeas;

        res.pageWiseRecall = avgPerPageRecall;
        res.pageWisePrecision = avgPerPagePrecision;
        res.pageWiseFmeas = avgPerPageFmeas;


        double[] xTick = new double[ticks];
        double[] fmeas = new double[ticks];
        for (int i = 0; i < ticks; i++) {
            xTick[i] = (1.0 / (ticks - 1)) * i;
            avgOverPagesRecallThr[i] /= polyPagesTruth.length;
            avgOverPagesPrecisionThr[i] /= polyPagesTruth.length;
            fmeas[i] = fmeas(avgOverPagesPrecisionThr[i], avgOverPagesRecallThr[i]);
        }
        double[][] vals = new double[3][];
        vals[0] = avgOverPagesRecallThr;
        vals[1] = avgOverPagesPrecisionThr;
        vals[2] = fmeas;
        
        res.xTickThr = xTick;
        res.valsThr = vals;
        return res;
    }

    private static double[] calcTol(Polygon[] polyTruthBlown, Polygon[] polyTruth) {
        double maxTol = 50;
        double[] tol = new double[polyTruth.length];
        for (int i = 0; i < polyTruth.length; i++) {
            Polygon aPoly = polyTruth[i];
            double sumDist = 0.0;
            for (int j = 0; j < aPoly.npoints; j++) {
                Point pA = new Point(aPoly.xpoints[j], aPoly.ypoints[j]);
                double minDist = getMinDist(pA, i, polyTruthBlown);
                sumDist += Math.min(maxTol, minDist / 7.5);
            }
            tol[i] = sumDist / aPoly.npoints;
        }
        return tol;
    }

    private static double getMinDist(Point pA, int ignore, Polygon[] polyTruth) {
        double dist = Double.MAX_VALUE;
        for (int i = 0; i < polyTruth.length; i++) {
            if (i != ignore) {
                Polygon polyC = polyTruth[i];
                for (int j = 0; j < polyC.npoints; j++) {
                    dist = Math.min(dist, Math.sqrt((polyC.xpoints[j] - pA.x) * (polyC.xpoints[j] - pA.x) + (polyC.ypoints[j] - pA.y) * (polyC.ypoints[j] - pA.y)));
                }

            }
        }
        return dist;
    }

    private static double recall(Polygon[] reco, Polygon truth, double tol) {
        return cntHitsList(truth, reco, tol) / truth.npoints;
    }

    private static double cntHitsList(Polygon toCnt, Polygon[] refL, double tol) {
        double cnt = 0;
        Rectangle toCntBB = toCnt.getBounds();
        for (int i = 0; i < toCnt.npoints; i++) {
            int xA = toCnt.xpoints[i];
            int yA = toCnt.ypoints[i];
            boolean match = false;
            double minDist = Double.MAX_VALUE;
            for (Polygon ref : refL) {
                Rectangle refBB = ref.getBounds();
                Rectangle inter = toCntBB.intersection(refBB);
                int maxI = Math.max(inter.width, inter.height);
                if (maxI < -3.0 * tol) {
                    continue;
                }
                for (int j = 0; j < ref.npoints; j++) {
                    int xC = ref.xpoints[j];
                    int yC = ref.ypoints[j];
//                    minDist = Math.min(Math.abs(xC-xA)+Math.abs(yC-yA), minDist);
                    minDist = Math.min(Math.sqrt((xC - xA) * (xC - xA) + (yC - yA) * (yC - yA)), minDist);
                    if (minDist <= tol) {
                        cnt += 1.0;
                        match = true;
                        break;
                    }
                }
                if (match) {
                    break;
                }
            }
            if (minDist > tol && minDist < 3.0 * tol) {
                cnt += (3.0 * tol - minDist) / (2.0 * tol);
            }
        }
        return cnt;
    }

    private static int[] getMaxIdx(double[][] C) {
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

    private static double cntHits(Polygon toCnt, Polygon ref, double tol) {
        Rectangle toCntBB = toCnt.getBounds();
        Rectangle refBB = ref.getBounds();
        Rectangle inter = toCntBB.intersection(refBB);
        int maxI = Math.max(inter.width, inter.height);
        if (maxI < -3.0 * tol) {
            return 0;
        }
        double cnt = 0;
        for (int i = 0; i < toCnt.npoints; i++) {
            int xA = toCnt.xpoints[i];
            int yA = toCnt.ypoints[i];
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < ref.npoints; j++) {
                int xC = ref.xpoints[j];
                int yC = ref.ypoints[j];
//                minDist = Math.min(Math.abs(xC-xA)+Math.abs(yC-yA), minDist);
                minDist = Math.min(Math.sqrt((xC - xA) * (xC - xA) + (yC - yA) * (yC - yA)), minDist);
                if (minDist <= tol) {
                    cnt += 1.0;
                    break;
                }
            }
            if (minDist > tol && minDist < 3.0 * tol) {
                cnt += (3.0 * tol - minDist) / (2.0 * tol);
            }
        }
        return cnt;
    }

    private static double precision(Polygon reco, Polygon truth, double tol) {
        return cntHits(reco, truth, tol) / reco.npoints;
    }

    private static double[] calcPrecision(Polygon[] polyReco, Polygon[] polyTruth, double[] tol) {
        double[] prec = new double[polyReco.length];
        double[][] C = new double[polyReco.length][polyTruth.length];
        for (int i = 0; i < C.length; i++) {
            double[] aC = C[i];
            for (int j = 0; j < aC.length; j++) {
                aC[j] = precision(polyReco[i], polyTruth[j], tol[j]);
            }
        }
        //Calculation of the BEST Alignment
        while (true) {
            int[] maxIdx = getMaxIdx(C);
            if (maxIdx[0] < 0) {
                break;
            }
            prec[maxIdx[0]] = C[maxIdx[0]][maxIdx[1]];

            for (int i = 0; i < C.length; i++) {
                C[i][maxIdx[1]] = 0.0;
            }
            for (int i = 0; i < C[0].length; i++) {
                C[maxIdx[0]][i] = 0.0;
            }
        }
        return prec;
    }

    private static double fmeas(double prec, double rec) {
        return 2.0 * rec * prec / (rec + prec);
    }

}
