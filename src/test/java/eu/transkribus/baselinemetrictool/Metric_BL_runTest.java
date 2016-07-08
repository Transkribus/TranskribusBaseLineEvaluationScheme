/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.baselinemetrictool;

import org.junit.Test;

/**
 *
 * @author tobi
 */
public class Metric_BL_runTest {
    

    /**
     * Test of main method, of class Metric_BL_run.
     */
    @Test
    public void testMain() throws Exception {
        
        String[] args = new String[3];
            args[0] = "src/test/resources/truth.lst";
            args[1] = "src/test/resources/reco.lst";
    //        args[0] = "src/test/resources/lineTruth.txt";
    //        args[1] = "src/test/resources/lineReco1.txt";
    //        args[1] = "src/test/resources/lineReco2.txt";
    //        args[1] = "src/test/resources/lineReco3.txt";
    //        args[1] = "src/test/resources/lineReco4.txt";
    //        args[1] = "src/test/resources/lineReco5.txt";
    //        args[1] = "src/test/resources/lineReco6.txt";
    //        args[1] = "src/test/resources/lineReco7.txt";
    //        args[1] = "src/test/resources/lineReco8.txt";
            args[2] = "-p";
//            args[3] = "-tol";
//            args[4] = "-i";
//            args[5] = "src/test/resources/metrEx.png";
//            args[6] = "-t";
    //        args = ("--help").split(" ");
        
        Metric_BL_run.main(args);
        
        
        // TODO review the generated test code and remove the default call to fail.
    }
    
}
