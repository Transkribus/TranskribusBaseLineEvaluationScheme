/*
 * File: LinRegression 
 * Created: 10:04:10 05.12.2014
 * Encoding: UTF-8
 */

package eu.transkribus.baselinemetrictool.util;


/**
 * Describtion of LinRegression: 
 *
 * @author Tobias Grüning <tobias.gruening@uni-rostock.de>
 */
public class LinRegression {

    public static double[] calcLine(int[] xPoints, int[] yPoints) {
        int dimA = xPoints.length;
        double minX = 10000;
        double maxX = 0;
        double sumX = 0.0;
        double[][] A = new double[dimA][2];
        double[] Y = new double[dimA];
        for (int i = 0; i < dimA; i++) {
            double[] rowI = A[i];
            int actPx = xPoints[i];
            int actPy = yPoints[i];
            rowI[0] = 1.0;
            rowI[1] = actPx;
            minX = Math.min(minX, actPx);
            maxX = Math.max(maxX, actPx);
            sumX += actPx;
            Y[i] = actPy;
        }
        if (maxX - minX < 2) {
            return new double[]{sumX / dimA, Double.POSITIVE_INFINITY};
        }

        return solveLin(A, Y);
    }

    public static double[] solveLin(double[][] mat1, double[] Y) {
        double[][] mat1T = transpose(mat1);
        double[][] multLS = multiply(mat1T, mat1);
        double[] multRS = multiply(mat1T, Y);
        double[][] inv = null;
        if (multLS.length != 2) {
            System.out.println("LinRegression Error: Matrix not 2x2");
        } else {
            inv = new double[2][2];
            double n = (multLS[0][0] * multLS[1][1] - multLS[0][1] * multLS[1][0]);
            if (n < 1E-9) {
                System.out.println("LinRegression Error: Numerically unstable.");
                return new double[]{mat1[0][1], Double.POSITIVE_INFINITY};
            }
            double fac = 1.0 / n;
            inv[0][0] = fac * multLS[1][1];
            inv[1][1] = fac * multLS[0][0];
            inv[1][0] = -fac * multLS[1][0];
            inv[0][1] = -fac * multLS[0][1];
        }
        double[] res = multiply(inv, multRS);
        return res;
    }
    
    
    private static double[][] transpose(double[][] A){
        double[][] res = new double[A[0].length][A.length];
        for (int i = 0; i < A.length; i++) {
            double[] aA = A[i];
            for (int j = 0; j < aA.length; j++) {
                res[j][i] = aA[j];
            }
        }
        return res;
    }
    
    private static double[] multiply(double[][] A, double[] x){
        if(A[0].length != x.length){
            System.out.println("LinRegression Error: Matrix dimension mismatch.");
        }
        double[] res = new double[A.length];
        for (int i = 0; i < res.length; i++) {
            double[] aA = A[i];
            double tmp = 0.0;
            for (int j = 0; j < aA.length; j++) {
                tmp +=  x[j]*aA[j];
            }
            res[i] = tmp;
        }
        return res;
    }
    
    private static double[][] multiply(double[][] A, double[][] B){
        if(A[0].length != B.length){
            System.out.println("LinRegression Error: Matrix dimension mismatch.");
        }
        double[][] res = new double[A.length][B[0].length];
        for (int i = 0; i < A.length; i++) {
            double[] aA = A[i];
            for (int j = 0; j < B[0].length; j++) {
                double tmp = 0.0;
                for (int k = 0; k < B.length; k++) {
                    tmp += B[k][j]*aA[k];
                }
                res[i][j] = tmp;
            }
        }
        return res;
    }
    
    
} 
