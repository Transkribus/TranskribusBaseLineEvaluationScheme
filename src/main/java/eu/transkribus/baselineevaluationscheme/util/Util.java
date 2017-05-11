package eu.transkribus.baselineevaluationscheme.util;

////////////////////////////////////////////////
/// File:       LoadTextFile.java
/// Created:    21.04.2016  13:25:07
/// Encoding:   UTF-8
////////////////////////////////////////////////
import de.erichseifert.vectorgraphics2d.SVGGraphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.primaresearch.dla.page.Page;
import org.primaresearch.dla.page.io.xml.PageXmlInputOutput;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.io.UnsupportedFormatVersionException;
import org.primaresearch.io.xml.XmlModelAndValidatorProvider;
import org.primaresearch.maths.geometry.Point;

/**
 * Desciption of LoadTextFile
 *
 *
 * Since 21.04.2016
 *
 * @author Tobi <tobias.gruening.hro@gmail.com>
 */
public class Util {

    public static ArrayList<String> loadTextFile(String fileName) throws FileNotFoundException, IOException {
        File f = new File(fileName);
        if (f.exists()) {
            InputStream is = new java.io.FileInputStream(fileName);
            ArrayList<String> list = new ArrayList<String>();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        list.add(line);
                    }
                }
            } finally {
                if (br != null) {
                    br.close();
                }
            }
            return list;
        } else {
            throw new FileNotFoundException("file or resource not found: " + fileName);
        }
    }

    private static Polygon parseString(String stringPoly) {
        Polygon res = new Polygon();
        String[] splitted = stringPoly.split(";");
        if (splitted.length < 2) {
            throw new IllegalArgumentException("Wrong Polygon String Format");
        }
        for (String aCoordPair : splitted) {
            String[] splitted2 = aCoordPair.split(",");
            if (splitted2.length != 2) {
                throw new IllegalArgumentException("Wrong Polygon String Format");
            }
            int coordX = Integer.valueOf(splitted2[0]);
            int coordY = Integer.valueOf(splitted2[1]);
            res.addPoint(coordX, coordY);
        }
        return res;
    }

    public static String poly2string(Polygon poly) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < poly.npoints; i++) {
            if (i > 0) {
                stringBuilder.append(";");
            }
            stringBuilder.append(poly.xpoints[i]);
            stringBuilder.append(",");
            stringBuilder.append(poly.ypoints[i]);
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static List<List<Polygon>> getPolysFromPageFile(String fileName) throws XmlModelAndValidatorProvider.NoSchemasException {
        if (fileName.endsWith(".xml")) {
            Page aPage;
            try {
                List<List<Polygon>> res = new ArrayList<>();
//                aPage = reader.read(new FileInput(new File(fileName)));
                aPage = PageXmlInputOutput.readPage(fileName);
                if (aPage == null) {
                    System.out.println("Error while parsing xml-File.");
                    return null;
                }
                List<Region> regionsSorted = aPage.getLayout().getRegionsSorted();
                for (Region reg : regionsSorted) {
                    if (reg instanceof TextRegion) {
                        List<Polygon> aReg = new ArrayList<>();
                        org.primaresearch.maths.geometry.Polygon regPoly = ((TextRegion) reg).getCoords();
                        if (regPoly != null) {
                            Polygon aPoly = new Polygon();
                            for (int j = 0; j < regPoly.getSize(); j++) {
                                Point aPt = regPoly.getPoint(j);
                                aPoly.addPoint(aPt.x, aPt.y);
                            }
                            aReg.add(aPoly);
                        } else {
                            aReg.add(new Polygon(new int[]{0, 0}, new int[]{0, 0}, 2));
                        }

                        for (LowLevelTextObject tObj : ((TextRegion) reg).getTextObjectsSorted()) {
                            if (tObj instanceof TextLine) {
                                org.primaresearch.maths.geometry.Polygon aBL = ((TextLine) tObj).getBaseline();
                                if (aBL != null) {
                                    Polygon aPoly = new Polygon();
                                    for (int j = 0; j < aBL.getSize(); j++) {
                                        Point aPt = aBL.getPoint(j);
                                        aPoly.addPoint(aPt.x, aPt.y);
                                    }
                                    aReg.add(aPoly);
                                }
                            }
                        }
                        res.add(aReg);
                    }
                }
                return res;
            } catch (UnsupportedFormatVersionException ex) {
                System.out.println(ex);
                System.out.println("Error while parsing xml-File.");
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static List<Polygon> getRegionPolysFromFile(String fileName) throws XmlModelAndValidatorProvider.NoSchemasException {
        if (fileName.endsWith(".xml")) {
            Page aPage;
            try {
                List<Polygon> res = new ArrayList<>();
//                aPage = reader.read(new FileInput(new File(fileName)));
                aPage = PageXmlInputOutput.readPage(fileName);
                if (aPage == null) {
                    System.out.println("Error while parsing xml-File.");
                    return null;
                }
                List<Region> regionsSorted = aPage.getLayout().getRegionsSorted();
                for (Region reg : regionsSorted) {
                    if (reg instanceof TextRegion) {
                        org.primaresearch.maths.geometry.Polygon regPoly = ((TextRegion) reg).getCoords();
                        if (regPoly != null) {
                            Polygon aPoly = new Polygon();
                            for (int j = 0; j < regPoly.getSize(); j++) {
                                Point aPt = regPoly.getPoint(j);
                                aPoly.addPoint(aPt.x, aPt.y);
                            }
                            res.add(aPoly);
                        }
                    }
                }
                return res;
            } catch (UnsupportedFormatVersionException ex) {
                System.out.println(ex);
                System.out.println("Error while parsing xml-File.");
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static LoadResult getPolysFromFile(String polyFileName, List<Polygon> regionPolys) throws IOException, XmlModelAndValidatorProvider.NoSchemasException {
        if (polyFileName.endsWith(".txt")) {
            ArrayList<String> polyString = Util.loadTextFile(polyFileName);
            if (polyString == null || polyString.size() == 0 || polyString.get(0).isEmpty()) {
                return null;
            }

            List<Polygon> res = new ArrayList<Polygon>();
            for (int i = 0; i < polyString.size(); i++) {
                String polyStringA = polyString.get(i);
                try {
                    Polygon aPoly = parseString(polyStringA);
                    if (isContained(aPoly, regionPolys)) {
                        res.add(aPoly);
                    }
                } catch (IllegalArgumentException ex) {
                    return new LoadResult(null, true);
                }
            }
            return new LoadResult(asArray(res), false);
        }
        if (polyFileName.endsWith(".xml")) {
            ArrayList<org.primaresearch.maths.geometry.Polygon> baselines = new ArrayList<org.primaresearch.maths.geometry.Polygon>();
            Page aPage;
            try {
//                System.out.println(polyFileName);
//                aPage = reader.read(new FileInput(new File(polyFileName)));
                aPage = PageXmlInputOutput.readPage(polyFileName);
                if (aPage == null) {
                    System.out.println(polyFileName);
                    System.out.println("Error while parsing xml-File.");
                    return new LoadResult(null, true);
                }
                List<Region> regionsSorted = aPage.getLayout().getRegionsSorted();
                for (Region reg : regionsSorted) {
                    if (reg instanceof TextRegion) {
                        for (LowLevelTextObject tObj : ((TextRegion) reg).getTextObjectsSorted()) {
                            if (tObj instanceof TextLine) {
                                org.primaresearch.maths.geometry.Polygon aBL = ((TextLine) tObj).getBaseline();
                                if (aBL != null) {
                                    baselines.add(aBL);
                                }
                            }
                        }
                    }
                }
                List<Polygon> res = new ArrayList<Polygon>();
                for (int i = 0; i < baselines.size(); i++) {
                    org.primaresearch.maths.geometry.Polygon aPoly = baselines.get(i);
                    Polygon aPolyAWT = new Polygon();
                    for (int j = 0; j < aPoly.getSize(); j++) {
                        Point aPt = aPoly.getPoint(j);
                        aPolyAWT.addPoint(aPt.x, aPt.y);
                    }
                    if (isContained(aPolyAWT, regionPolys)) {
                        res.add(aPolyAWT);
                    }
                }
                return new LoadResult(asArray(res), false);

            } catch (UnsupportedFormatVersionException ex) {
                System.out.println(ex);
                System.out.println("Error while parsing xml-File.");
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new LoadResult(null, true);
    }

    public static Polygon[] normDesDist(Polygon[] polyIn, int desDist) {
        Polygon[] res = new Polygon[polyIn.length];
        for (int i = 0; i < res.length; i++) {
            Rectangle bb = polyIn[i].getBounds();
            if (bb.width > 100000 || bb.height > 100000) {
                Polygon nPoly = new Polygon();
                nPoly.addPoint(0,0);
                polyIn[i] = nPoly;
            }
            res[i] = normDesDist(polyIn[i], desDist);
            res[i].getBounds();
        }
        return res;
    }

    public static Polygon normDesDist(Polygon polyIn, int desDist) {
        Polygon polyBlown = blowUp(polyIn);
        return thinOut(polyBlown, desDist);
    }

    private static Polygon blowUp(Polygon inPoly) {
        Polygon res = new Polygon();
        for (int i = 1; i < inPoly.npoints; i++) {
            int x1 = inPoly.xpoints[i - 1];
            int y1 = inPoly.ypoints[i - 1];
            int x2 = inPoly.xpoints[i];
            int y2 = inPoly.ypoints[i];
            int diffX = Math.abs(x2 - x1);
            int diffY = Math.abs(y2 - y1);
            if (Math.max(diffX, diffY) < 1) {
                if (i == inPoly.npoints - 1) {
                    res.addPoint(x2, y2);
                }
                continue;
            }
            res.addPoint(x1, y1);
            if (diffX >= diffY) {
                for (int j = 1; j < diffX; j++) {
                    int xN;
                    if (x1 < x2) {
                        xN = x1 + j;
                    } else {
                        xN = x1 - j;
                    }
                    int yN = (int) (Math.round(y1 + (double) (xN - x1) * (y2 - y1) / (x2 - x1)));
                    res.addPoint(xN, yN);
                }
            } else {
                for (int j = 1; j < diffY; j++) {
                    int yN;
                    if (y1 < y2) {
                        yN = y1 + j;
                    } else {
                        yN = y1 - j;
                    }
                    int xN = (int) (Math.round(x1 + (double) (yN - y1) * (x2 - x1) / (y2 - y1)));
                    res.addPoint(xN, yN);
                }
            }
            if (i == inPoly.npoints - 1) {
                res.addPoint(x2, y2);
            }
        }
        return res;
    }

    private static Polygon thinOut(Polygon polyBlown, int desDist) {
        Polygon res = new Polygon();
        if (polyBlown.npoints <= 20) {
            return polyBlown;
        }
        int dist = polyBlown.npoints - 1;
        int minPts = 20;
        int desPts = Math.max(minPts, dist / desDist + 1);
        double step = (double) dist / (desPts - 1);
        for (int i = 0; i < desPts - 1; i++) {
            int aIdx = (int) (i * step);
            res.addPoint(polyBlown.xpoints[aIdx], polyBlown.ypoints[aIdx]);
        }
        res.addPoint(polyBlown.xpoints[polyBlown.npoints - 1], polyBlown.ypoints[polyBlown.npoints - 1]);
        return res;
    }

    public static void plotPlausi(String pathToImg, Polygon[] polysTruth, Polygon[] polysReco, BaseLineMetricResult res, double thresTP, boolean save) throws IOException {

        boolean[][] specificPageTrueFalseConstellation;
        if (thresTP > 0) {
            specificPageTrueFalseConstellation = res.getSpecificPageTrueFalseConstellation(0, thresTP);
        } else {
            specificPageTrueFalseConstellation = new boolean[2][];
            specificPageTrueFalseConstellation[0] = new boolean[polysReco.length];
            Arrays.fill(specificPageTrueFalseConstellation[0], true);
            specificPageTrueFalseConstellation[1] = new boolean[polysTruth.length];
            Arrays.fill(specificPageTrueFalseConstellation[1], true);
        }

        BufferedImage imgT = ImageIO.read(new File(pathToImg));
        BufferedImage img = new BufferedImage(
                imgT.getWidth(), imgT.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.drawImage(imgT, 0, 0, null);
        graphics.setColor(new Color(0F / 255, 0F / 255, 255F / 255));
        if (polysTruth != null) {
            boolean[] hypConst = specificPageTrueFalseConstellation[1];
            int cnt = 0;
            for (Polygon truth : polysTruth) {
                if (hypConst[cnt]) {
                    graphics.setStroke(new BasicStroke(3f));
                } else {
                    graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
                }
                graphics.drawPolyline(truth.xpoints, truth.ypoints, truth.npoints);
                for (int i = 0; i < truth.npoints; i++) {
                    graphics.fillRect(truth.xpoints[i] - 3, truth.ypoints[i] - 3, 7, 7);
                }
                cnt++;
            }
        }
        graphics.setColor(new Color(255F / 255, 0F / 255, 0F / 255));
        if (polysReco != null) {
            boolean[] hypGT = specificPageTrueFalseConstellation[0];
            int cnt = 0;
            for (Polygon reco : polysReco) {
                if (hypGT[cnt]) {
                    graphics.setStroke(new BasicStroke(3f));
                } else {
                    graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
                }
                graphics.drawPolyline(reco.xpoints, reco.ypoints, reco.npoints);
                for (int i = 0; i < reco.npoints; i++) {
                    graphics.fillRect(reco.xpoints[i] - 3, reco.ypoints[i] - 3, 7, 7);
                }
                cnt++;
            }
        }

        if (save) {
            SVGGraphics2D g = new SVGGraphics2D(0.0, 0.0, img.getWidth(), img.getHeight());
            g.drawImage(img, null, 0, 0);
            g.setColor(new Color(0F / 255, 0F / 255, 255F / 255));
            if (polysTruth != null) {
                boolean[] hypConst = specificPageTrueFalseConstellation[1];
                int cnt = 0;
                for (Polygon truth : polysTruth) {
                    if (hypConst[cnt]) {
                        g.setStroke(new BasicStroke(3f));
                    } else {
                        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
                    }
                    g.drawPolyline(truth.xpoints, truth.ypoints, truth.npoints);
                    for (int i = 0; i < truth.npoints; i++) {
                        g.fillRect(truth.xpoints[i] - 3, truth.ypoints[i] - 3, 7, 7);
                    }
                    cnt++;
                }
            }
            g.setColor(new Color(255F / 255, 0F / 255, 0F / 255));
            if (polysReco != null) {
                boolean[] hypGT = specificPageTrueFalseConstellation[0];
                int cnt = 0;
                for (Polygon reco : polysReco) {
                    if (hypGT[cnt]) {
                        g.setStroke(new BasicStroke(3f));
                    } else {
                        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
                    }
                    g.drawPolyline(reco.xpoints, reco.ypoints, reco.npoints);
                    for (int i = 0; i < reco.npoints; i++) {
                        g.fillRect(reco.xpoints[i] - 3, reco.ypoints[i] - 3, 7, 7);
                    }
                    cnt++;
                }
            }
            FileOutputStream file = new FileOutputStream("plausi.svg");
            try {
                file.write(g.getBytes());
            } finally {
                file.close();
            }
        }

        JFrame f = new JFrame("Plausi-Check");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new BuffPanel(img));
        f.pack();
//        f.setLocationRelativeTo(null);
        f.setVisible(true);

    }

    private static Polygon[] asArray(List<Polygon> in) {
        if (in == null) {
            return null;
        }
        Polygon[] res = new Polygon[in.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = in.get(i);
        }
        return res;
    }

    private static boolean isContained(Polygon bL, List<Polygon> regionPolys) {
        if (regionPolys == null) {
            return true;
        }
        Polygon normDesDist = normDesDist(bL, 10);
        for (int i = 0; i < normDesDist.npoints; i++) {
            int aX = normDesDist.xpoints[i];
            int aY = normDesDist.ypoints[i];
            for (Polygon aRP : regionPolys) {
                if (aRP.contains(aX, aY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double[] calcTols(Polygon[] polyTruthNorm, int tickDist, int maxD, double relTol) {
        double[] tols = new double[polyTruthNorm.length];

        int lineCnt = 0;
        for (Polygon aPoly : polyTruthNorm) {
            double angle = calcRegLineStats(aPoly)[0];
            double orVecY = Math.sin(angle);
            double orVecX = Math.cos(angle);
            double aDist = maxD;
            double[] ptA1 = new double[]{aPoly.xpoints[0], aPoly.ypoints[0]};
            double[] ptA2 = new double[]{aPoly.xpoints[aPoly.npoints - 1], aPoly.ypoints[aPoly.npoints - 1]};
            for (int i = 0; i < aPoly.npoints; i++) {
                double[] pA = new double[]{aPoly.xpoints[i], aPoly.ypoints[i]};
                for (Polygon cPoly : polyTruthNorm) {
                    if (cPoly != aPoly) {
                        if (getDistFast(pA, cPoly.getBounds()) > aDist) {
                            continue;
                        }
                        double[] ptC1 = new double[]{cPoly.xpoints[0], cPoly.ypoints[0]};
                        double[] ptC2 = new double[]{cPoly.xpoints[cPoly.npoints - 1], cPoly.ypoints[cPoly.npoints - 1]};
                        double inD1 = getInDist(ptA1, ptC1, orVecX, orVecY);
                        double inD2 = getInDist(ptA1, ptC2, orVecX, orVecY);
                        double inD3 = getInDist(ptA2, ptC1, orVecX, orVecY);
                        double inD4 = getInDist(ptA2, ptC2, orVecX, orVecY);
                        if ((inD1 < 0 && inD2 < 0 && inD3 < 0 && inD4 < 0) || (inD1 > 0 && inD2 > 0 && inD3 > 0 && inD4 > 0)) {
                            continue;
                        }

                        for (int j = 0; j < cPoly.npoints; j++) {
                            double[] pC = new double[]{cPoly.xpoints[j], cPoly.ypoints[j]};
                            if (Math.abs(getInDist(pA, pC, orVecX, orVecY)) <= 2 * tickDist) {
                                aDist = Math.min(aDist, Math.abs(getOffDist(pA, pC, orVecX, orVecY)));
                            }
                        }
                    }
                }
            }
//            System.out.println("Line " + lineCnt + " has min dist of: " + aDist);
//            System.out.println("Line " + lineCnt + " has startX: " + aPoly.xpoints[0] + " and startY: " + aPoly.ypoints[0]);
            if (aDist < maxD) {
                tols[lineCnt] = aDist;
            }
            lineCnt++;
        }
        double sumVal = 0.0;
        int cnt = 0;
        for (int i = 0; i < tols.length; i++) {
            double aTol = tols[i];
            if (aTol != 0) {
                sumVal += aTol;
                cnt++;
            }
        }
        double meanVal = maxD;
        if (cnt != 0) {
            meanVal = sumVal / cnt;
        }

        for (int i = 0; i < tols.length; i++) {
            if (tols[i] == 0) {
                tols[i] = meanVal;
            }
            tols[i] = Math.min(tols[i], meanVal);
            tols[i] *= relTol;
        }

        return tols;
    }

    private static double getOffDist(double[] aPt, double[] cPt, double orVecX, double orVecY) {
        double diffX = aPt[0] - cPt[0];
        double diffY = -aPt[1] + cPt[1];
        //Since orVec has length 1 calculate the cross product, which is 
        //the orthogonal distance from diff to orVec, take into account 
        //the z-Value to decide whether its a positive or negative distance!
        //double dotProdX = 0;
        //double dotProdY = 0;
        return diffX * orVecY - diffY * orVecX;
    }

    public static double getInDist(double[] aPt, double[] cPt, double orVecX, double orVecY) {
        double diffX = aPt[0] - cPt[0];
        double diffY = -aPt[1] + cPt[1];
        //Parallel component of (diffX, diffY) is lambda * (orVecX, orVecY) with
        double lambda = diffX * orVecX + orVecY * diffY;

        return lambda;
    }

    public static double getDistFast(double[] aPt, double[] bPt) {
        return Math.abs(aPt[0] - bPt[0]) + Math.abs(aPt[1] - bPt[1]);
    }

    public static double getDistFast(double[] aPt, Rectangle bb) {
        double dist = 0.0;
        if (aPt[0] < bb.x) {
            dist += bb.x - aPt[0];
        }
        if (aPt[0] > bb.x + bb.width) {
            dist += aPt[0] - bb.x - bb.width;
        }
        if (aPt[1] < bb.y) {
            dist += bb.y - aPt[1];
        }
        if (aPt[1] > bb.y + bb.height) {
            dist += aPt[1] - bb.y - bb.height;
        }
        return dist;
    }

    /**
     *
     * @param p
     * @return #0 - angle #1 - absVal
     */
    private static double[] calcRegLineStats(Polygon p) {
        if (p.npoints <= 1) {
            return new double[]{0.0, 0.0};
        }
        double m = 0.0;
        double n = Double.POSITIVE_INFINITY;
        if (p.npoints > 2) {
            int xMax = 0;
            int xMin = Integer.MAX_VALUE;
            for (int i = 0; i < p.npoints; i++) {
                int xVal = p.xpoints[i];
                xMax = Math.max(xMax, xVal);
                xMin = Math.min(xMin, xVal);
            }
            if (xMax == xMin) {
                m = Double.POSITIVE_INFINITY;
            } else {
                int[] xPs = new int[p.npoints];
                int[] yPs = new int[p.npoints];
                for (int i = 0; i < p.npoints; i++) {
                    xPs[i] = p.xpoints[i];
                    yPs[i] = -p.ypoints[i];
                }
                double[] calcLine = LinRegression.calcLine(xPs, yPs);
                m = calcLine[1];
                n = calcLine[0];
            }
        } else {
            int x1 = p.xpoints[0];
            int x2 = p.xpoints[1];
            int y1 = -p.ypoints[0];
            int y2 = -p.ypoints[1];
            if (x1 == x2) {
                m = Double.POSITIVE_INFINITY;
            } else {
                m = (double) (y2 - y1) / (x2 - x1);
                n = y2 - m * x2;
            }
        }
        double angle = 0.0;
        if (Double.isInfinite(m)) {
            angle = Math.PI / 2.0;
        } else {
            angle = Math.atan(m);
        }

        int fP = 0;
        int lP = p.npoints - 1;

        if (angle > -Math.PI / 2.0 && angle <= -Math.PI / 4.0) {
            if (p.ypoints[fP] > p.ypoints[lP]) {
                angle += Math.PI;
            }
        }
        if (angle > -Math.PI / 4.0 && angle <= Math.PI / 4.0) {
            if (p.xpoints[fP] > p.xpoints[lP]) {
                angle += Math.PI;
            }
        }
        if (angle > Math.PI / 4.0 && angle <= Math.PI / 2.0) {
            if (p.ypoints[fP] < p.ypoints[lP]) {
                angle += Math.PI;
            }
        }

        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return new double[]{angle, n};
    }

    private static class BuffPanel extends JPanel {

        private BufferedImage image;

        BuffPanel(BufferedImage image) {
            this.image = image;
        }

        @Override
        public Dimension getPreferredSize() {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int width = gd.getDisplayMode().getWidth();
            int height = gd.getDisplayMode().getHeight();
            int widthImg = image.getWidth(this);
            int heightImg = image.getHeight(this);

            double sW = (double) widthImg / width;
            double sH = (double) heightImg / height;

            double scale = Math.max(sH, sW);
            scale = Math.max(scale, 1.0);

            return new Dimension((int) (widthImg / scale), (int) (heightImg / scale));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    public static double fmeas(double prec, double rec) {
        if (prec == 0 && rec == 0) {
            return 0.0;
        }
        return 2.0 * rec * prec / (rec + prec);
    }

}
