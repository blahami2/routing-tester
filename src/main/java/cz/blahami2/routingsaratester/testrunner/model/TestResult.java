/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.testrunner.model;

import cz.certicon.routing.model.values.Number;
import cz.certicon.routing.model.values.Time;
import lombok.Builder;
import lombok.Value;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
@Value
@Builder
public class TestResult {


    int[] numberOfCells;
    int totalNumberOfCells;
    double[] averageCellSize;
    double totalAverageCellSize;
    int[] medianCellSize;
    int[] maximalCellSize;
    int totalMaximalCellSize;
    int[] minimalCellSize;
    int totalMinimalCellSize;
    int[] numberOfCutEdges;
    int totalNumberOfCutEdges;
    double validRatio;
    Time filteringTime;
    Time assemblyTime;
    Time overlayTime;
    Time routingTime;
    Time[] routingTimes;
    Time unpackTime;
    Time[] unpackTimes;
    long examinedNodes;
    long relaxedEdges;
    long visitedEdges;

    public TestResult add( TestResult testResult ) {
        return new TestResult(
                addArray( numberOfCells, testResult.getNumberOfCells() ),
                totalNumberOfCells + testResult.getTotalNumberOfCells(),
                addArray( averageCellSize, testResult.getAverageCellSize() ),
                totalAverageCellSize + testResult.getTotalAverageCellSize(),
                addArray( medianCellSize, testResult.medianCellSize ),
                addArray( maximalCellSize, testResult.getMaximalCellSize() ),
                totalMaximalCellSize + testResult.getTotalMaximalCellSize(),
                addArray( minimalCellSize, testResult.getMinimalCellSize() ),
                totalMinimalCellSize + testResult.getTotalMinimalCellSize(),
                addArray( numberOfCutEdges, testResult.getNumberOfCutEdges() ),
                totalNumberOfCutEdges + testResult.getTotalNumberOfCutEdges(),
                validRatio + testResult.getValidRatio(),
                filteringTime.add( testResult.getFilteringTime() ),
                assemblyTime.add( testResult.getAssemblyTime() ),
                overlayTime.add( testResult.getOverlayTime() ),
                routingTime.add( testResult.getRoutingTime() ),
                addArray( routingTimes, testResult.getRoutingTimes() ),
                unpackTime.add( testResult.getUnpackTime() ),
                addArray( unpackTimes, testResult.getUnpackTimes() ),
                examinedNodes + testResult.getExaminedNodes(),
                relaxedEdges + testResult.getRelaxedEdges(),
                visitedEdges + testResult.getVisitedEdges()
        );
    }

    private int[] addArray( int[] a, int[] b ) {
        return IntStream.range( 0, a.length ).map( i -> a[i] + b[i] ).toArray();
    }

    private double[] addArray( double[] a, double[] b ) {
        return IntStream.range( 0, a.length ).mapToDouble( i -> a[i] + b[i] ).toArray();
    }

    private Time[] addArray( Time[] a, Time[] b ) {
        return IntStream.range( 0, a.length ).mapToObj( i -> a[i].add( b[i] ) ).toArray( Time[]::new );
    }

    public TestResult divide( int number ) {
        return new TestResult(
                divideArray( numberOfCells, number ),
                totalNumberOfCells / number,
                divideArray( averageCellSize, number ),
                totalAverageCellSize / number,
                divideArray( medianCellSize, number ),
                divideArray( maximalCellSize, number ),
                totalMaximalCellSize / number,
                divideArray( minimalCellSize, number ),
                totalMinimalCellSize / number,
                divideArray( numberOfCutEdges, number ),
                totalNumberOfCutEdges / number,
                validRatio / number,
                filteringTime.divide( number ),
                assemblyTime.divide( number ),
                overlayTime.divide( number ),
                routingTime.divide( number ),
                divideArray( routingTimes, number ),
                unpackTime.divide( number ),
                divideArray( unpackTimes, number ),
                examinedNodes / number,
                relaxedEdges / number,
                visitedEdges / number
        );
    }

    private int[] divideArray( int[] array, int divisor ) {
        return Arrays.stream( array ).map( x -> x / divisor ).toArray();
    }

    private double[] divideArray( double[] array, int divisor ) {
        return Arrays.stream( array ).map( x -> x / divisor ).toArray();
    }

    private Time[] divideArray( Time[] array, int divisor ) {
        return Arrays.stream( array ).map( x -> x.divide( divisor ) ).toArray( Time[]::new );
    }
}
