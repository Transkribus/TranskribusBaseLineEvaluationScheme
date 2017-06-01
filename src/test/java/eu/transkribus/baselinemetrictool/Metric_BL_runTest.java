/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.baselinemetrictool;

import eu.transkribus.baselineevaluationscheme.Metric_BL_run;
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
        
        String[] args = new String[2];
            args[0] = "src/test/resources/truth.lst";
            args[1] = "src/test/resources/reco.lst";
    //        args = ("--help").split(" ");
        
        Metric_BL_run.main(args);
        
        
        // TODO review the generated test code and remove the default call to fail.
    }
    
}
