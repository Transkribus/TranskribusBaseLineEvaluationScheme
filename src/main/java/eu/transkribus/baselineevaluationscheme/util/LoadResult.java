/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.transkribus.baselineevaluationscheme.util;

////////////////////////////////////////////////

import java.awt.Polygon;

/// File:       LoadResult.java
/// Created:    06.03.2017  10:48:16
/// Encoding:   UTF-8
////////////////////////////////////////////////




/**
 *  Desciption of LoadResult
 *
 *
 *   Since 06.03.2017
 *
 * @author Tobias Gruening tobias.gruening.hro@gmail.com
 */
public class LoadResult {

    private Polygon[] polys;
    private boolean error = false;

    public LoadResult(Polygon[] polys, boolean error) {
        this.polys = polys;
        this.error = error;
    }

    public Polygon[] getPolys() {
        return polys;
    }

    public boolean isError() {
        return error;
    }
    
} 