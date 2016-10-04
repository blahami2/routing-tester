/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.model;

import cz.certicon.routing.model.values.Time;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
@Value
@Builder
public class TestResult {

    int numberOfCells;
    double averageCellSize;
    int medianCellSize;
    int maximalCellSize;
    int minimalCellSize;
    int numberOfCutEdges;
    Time filteringTime;
    Time assemblyTime;
}
