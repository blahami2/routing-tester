/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.common.logic;

import cz.blahami2.routingsaratester.common.model.Output;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class ResultComparator {

    public boolean isValid( Output expected, Output actual ) {
        return expected.equals( actual );
    }
}
