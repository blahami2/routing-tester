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

    public TestResult add( TestResult testResult ) {
        return new TestResult(
                numberOfCells + testResult.getNumberOfCells(),
                averageCellSize + testResult.getAverageCellSize(),
                medianCellSize + testResult.medianCellSize,
                maximalCellSize + testResult.getMaximalCellSize(),
                minimalCellSize + testResult.getMinimalCellSize(),
                numberOfCutEdges + testResult.getNumberOfCutEdges(),
                validRatio + testResult.getValidRatio(),
                filteringTime.add( testResult.getFilteringTime() ),
                assemblyTime.add( testResult.getAssemblyTime() ),
                routingTime.add( testResult.getRoutingTime() ),
                unpackTime.add( testResult.getUnpackTime() ),
                examinedNodes + testResult.getExaminedNodes(),
                relaxedEdges + testResult.getRelaxedEdges(),
                visitedEdges + testResult.getVisitedEdges()
        );
    }

    public TestResult divide( int number ) {
        return new TestResult(
                numberOfCells / number,
                averageCellSize / number,
                medianCellSize / number,
                maximalCellSize / number,
                minimalCellSize / number,
                numberOfCutEdges / number,
                validRatio / number,
                filteringTime.divide( number ),
                assemblyTime.divide( number ),
                routingTime.divide( number ),
                unpackTime.divide( number ),
                examinedNodes / number,
                relaxedEdges / number,
                visitedEdges / number
        );
    }
}
