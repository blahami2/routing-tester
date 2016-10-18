/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.model;

import lombok.Value;
import lombok.experimental.Wither;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
@Value
@Wither
public class TestOptions {

    int cellSize;
    double cellRatio;
    double coreRatio;
    double lowIntervalProbability;
    double lowIntervalLimit;
    int numberOfAssemblyRuns;

    public TestOptions( int cellSize, double cellRatio, double coreRatio, double lowIntervalProbability, double lowIntervalLimit, int numberOfAssemblyRuns ) {
        // validate input
        this.cellSize = cellSize;
        this.cellRatio = cellRatio;
        this.coreRatio = coreRatio;
        this.lowIntervalProbability = lowIntervalProbability;
        this.lowIntervalLimit = lowIntervalLimit;
        this.numberOfAssemblyRuns = numberOfAssemblyRuns;
    }
}
