/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.baselineevaluationscheme.util;

////////////////////////////////////////////////
import java.awt.Polygon;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.primaresearch.io.xml.XmlModelAndValidatorProvider;

/// File:       Page2Txt.java
/// Created:    02.03.2017  09:28:17
/// Encoding:   UTF-8
////////////////////////////////////////////////
/**
 * Desciption of Page2Txt
 *
 *
 * Since 02.03.2017
 *
 * @author Tobias Gruening tobias.gruening.hro@gmail.com
 */
public class Page2Txt {

    private static void processSingle(String pathToPage) throws XmlModelAndValidatorProvider.NoSchemasException {

        List<List<Polygon>> polysFromPageFile = Util.getPolysFromPageFile(pathToPage);

        int cnt = 0;
        if (polysFromPageFile == null) {
            return;
        }
        for (List<Polygon> aPolys : polysFromPageFile) {
            String fileName = pathToPage + "_reg_" + cnt + ".txt";
            BufferedWriter writer = null;
            try {
                //create a temporary file
                File logFile = new File(fileName);

                writer = new BufferedWriter(new FileWriter(logFile));
                if (aPolys != null && aPolys.size() > 0) {
                    writer.write(Util.poly2string(aPolys.get(0)));
                }
                writer.write("\n");
                for (int i = 1; i < aPolys.size(); i++) {
                    writer.write(Util.poly2string(aPolys.get(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    // Close the writer regardless of what happens...
                    writer.close();
                } catch (Exception e) {
                }
            }
            cnt++;
        }
    }
    
    private static void processList(String pathToList) throws XmlModelAndValidatorProvider.NoSchemasException, IOException {
        ArrayList<String> listOfXmls = Util.loadTextFile(pathToList);
        for (String aXMLpath : listOfXmls) {
            processSingle(aXMLpath);
        }
    }
    
    
    
    private static void process(String input) throws XmlModelAndValidatorProvider.NoSchemasException, IOException {
        if(input.endsWith(".xml")){
            processSingle(input);
        }else{
            processList(input);
        }
    }
    
    
    /**
     * Parses a list of page xml-files (or a single xml file) and produces several .txt-files, one for each
     * TextRegion present in the page-xmls. One .txt-file contains the text
     * region coords (if present) in the first row, followed by an empty row and
     * the baseline coords.
     *
     * @param args path to list of pathes to page-xml
     * @throws
     * org.primaresearch.io.xml.XmlModelAndValidatorProvider.NoSchemasException
     */
    public static void main(String[] args) throws IOException, XmlModelAndValidatorProvider.NoSchemasException {
        process(args[0]);
    }

}
