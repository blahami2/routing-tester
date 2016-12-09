/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.model;

import cz.certicon.routing.model.values.Time;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
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
    double validRatio;
    Time filteringTime;
    Time assemblyTime;
    Time routingTime;
    Time unpackTime;
    long examinedNodes;
    long relaxedEdges;
    long visitedEdges;
}
