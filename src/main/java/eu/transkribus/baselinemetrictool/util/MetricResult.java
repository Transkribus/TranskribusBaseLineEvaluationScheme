package eu.transkribus.baselinemetrictool.util;

////////////////////////////////////////////////
/// File:       MetricResult.java
/// Created:    22.04.2016  11:21:17
/// Encoding:   UTF-8
////////////////////////////////////////////////





/**
 *  Desciption of MetricResult
 *
 *
 *   Since 22.04.2016
 *
 * @author Tobi <tobias.gruening.hro@gmail.com>
 */
public class MetricResult {
    public double[] pageWiseRecall;
    public double[] pageWisePrecision;
    public double[] pageWiseFmeas;
    public double finRecall;
    public double finPrecision;
    public double finFmeas;
    public double[] xTickThr;
    public double[][] valsThr;
} 