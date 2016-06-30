package eu.transkribus.baselinemetrictool.util;

////////////////////////////////////////////////
/// File:       LoadTextFile.java
/// Created:    21.04.2016  13:25:07
/// Encoding:   UTF-8
////////////////////////////////////////////////
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.primaresearch.dla.page.Page;
import org.primaresearch.dla.page.io.xml.XmlInputOutput;
import org.primaresearch.dla.page.layout.physical.Region;
import org.primaresearch.dla.page.layout.physical.text.LowLevelTextObject;
import org.primaresearch.dla.page.layout.physical.text.impl.TextLine;
import org.primaresearch.dla.page.layout.physical.text.impl.TextRegion;
import org.primaresearch.io.UnsupportedFormatVersionException;
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

    public static Polygon[] getPolysFromFile(String polyFileName) throws IOException {

        if (polyFileName.endsWith(".txt")) {
            ArrayList<String> polyString = Util.loadTextFile(polyFileName);
            if (polyString == null || polyString.size() == 0 || polyString.get(0).isEmpty()) {
                return null;
            }

            Polygon[] res = new Polygon[polyString.size()];
            for (int i = 0; i < polyString.size(); i++) {
                String polyStringA = polyString.get(i);
                res[i] = parseString(polyStringA);
            }
            return res;
        }
        if (polyFileName.endsWith(".xml")) {
            ArrayList<org.primaresearch.maths.geometry.Polygon> baselines = new ArrayList<org.primaresearch.maths.geometry.Polygon>();
            Page aPage;
            try {
                aPage = XmlInputOutput.readPage(polyFileName);
                if (aPage == null) {
                    System.out.println("Error while parsing xml-File.");
                }
                List<Region> regionsSorted = aPage.getLayout().getRegionsSorted();
                for (Region reg : regionsSorted) {
                    if (reg instanceof TextRegion) {
                        for (LowLevelTextObject tObj : ((TextRegion) reg).getTextObjectsSorted()) {
                            if (tObj instanceof TextLine) {
                                baselines.add(((TextLine) tObj).getBaseline());
                            }
                        }
                    }
                }
                Polygon[] res = new Polygon[baselines.size()];
                for (int i = 0; i < res.length; i++) {
                    org.primaresearch.maths.geometry.Polygon aPoly = baselines.get(i);
                    Polygon aPolyAWT = new Polygon();
                    for (int j = 0; j < aPoly.getSize(); j++) {
                        Point aPt = aPoly.getPoint(j);
                        aPolyAWT.addPoint(aPt.x, aPt.y);
                    }
                    res[i] = aPolyAWT;
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

    public static Polygon[] normDesDist(Polygon[] polyIn, int desDist) {
        Polygon[] res = new Polygon[polyIn.length];
        for (int i = 0; i < res.length; i++) {
            polyIn[i].getBounds();
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
            res.addPoint(x1, y1);
            int diffX = Math.abs(x2 - x1);
            int diffY = Math.abs(y2 - y1);
            if (Math.max(diffX, diffY) < 1) {
                throw new IllegalArgumentException("Irregular Baseline");
            }
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

    public static void plotPlausi(String pathToImg, Polygon[] polysTruth, Polygon[] polysReco) throws IOException {
        BufferedImage img = ImageIO.read(new File(pathToImg));
        Graphics2D graphics = img.createGraphics();

        graphics.setColor(new Color(0F / 255, 0F / 255, 255F / 255));
        graphics.setStroke(new BasicStroke(2f));
        if (polysTruth != null) {
            for (Polygon truth : polysTruth) {
                graphics.drawPolyline(truth.xpoints, truth.ypoints, truth.npoints);
                for (int i = 0; i < truth.npoints; i++) {
                    graphics.fillRect(truth.xpoints[i] - 3, truth.ypoints[i] - 3, 7, 7);
                }
            }
        }
        graphics.setColor(new Color(255F / 255, 0F / 255, 0F / 255));
        graphics.setStroke(new BasicStroke(2f));
        if (polysReco != null) {
            for (Polygon reco : polysReco) {
                graphics.drawPolyline(reco.xpoints, reco.ypoints, reco.npoints);
                for (int i = 0; i < reco.npoints; i++) {
                    graphics.fillRect(reco.xpoints[i] - 3, reco.ypoints[i] - 3, 7, 7);
                }
            }
        }

        JFrame f = new JFrame("Plausi-Check");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new BuffPanel(img));
        f.pack();
//        f.setLocationRelativeTo(null);
        f.setVisible(true);

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
            
            double sW = (double)widthImg / width;
            double sH = (double)heightImg / height;
            
            double scale = Math.max(sH, sW);
            scale = Math.max(scale, 1.0);
            
            return new Dimension((int)(widthImg/scale), (int)(heightImg/scale));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }

    public static double fmeas(double prec, double rec) {
        return 2.0 * rec * prec / (rec + prec);
    }

}
