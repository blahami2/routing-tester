/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.plot.model;

import cz.blahami2.utils.table.model.Table;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import javax.swing.JFrame;

/**
 *
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 * @param <T>
 */
public interface Plot<T> {

    public void setData( Table<T> table, Function<T, Double> mapper );

    public void setData( Table<T> table, Function<T, Double> mapper, int xAxisColumn, int yAxisFirstColumn, int... yAxisColumns );

    public void display( JFrame frame );

    public void display();

    public void export( File destination ) throws IOException;
}
