/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.plot.model;

import cz.blahami2.utils.table.model.Table;
import cz.certicon.routing.utils.ColorUtils;
import cz.certicon.routing.utils.EffectiveUtils;
import de.erichseifert.gral.data.DataSeries;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.io.data.DataWriter;
import de.erichseifert.gral.io.data.DataWriterFactory;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import lombok.NonNull;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class GralPlot<T> implements Plot<T> {

    private Table<T> table;
    private Function<T, Double> mapper;
    private int xColumn;
    private int[] yColumns;
    private ColorUtils.ColorSupplier colorSupplier;

    public GralPlot( @NonNull Table<T> table, @NonNull Function<T, Double> mapper ) {
        setData( table, mapper );
    }

    public GralPlot( @NonNull Table<T> table, @NonNull Function<T, Double> mapper, int xAxisColumn, int yAxisFirstColumn, int... yAxisColumns ) {
        setData( table, mapper, xAxisColumn, yAxisFirstColumn, yAxisColumns );
    }

    @Override
    public final void setData( @NonNull Table<T> table, @NonNull Function<T, Double> mapper ) {
        this.table = table;
        this.mapper = mapper;
        xColumn = 0;
        yColumns = new int[table.getColumnCount() - 1];
        for ( int i = 1; i < table.getColumnCount(); i++ ) {
            yColumns[i - 1] = i;
        }
        colorSupplier = ColorUtils.createColorSupplier( yColumns.length );
    }

    @Override
    public final void setData( @NonNull Table<T> table, @NonNull Function<T, Double> mapper, int xAxisColumn, int yAxisFirstColumn, int... yAxisColumns ) {
        this.table = table;
        this.mapper = mapper;
        xColumn = xAxisColumn;
        yColumns = new int[1 + yAxisColumns.length];
        yColumns[0] = yAxisFirstColumn;
        System.arraycopy( yAxisColumns, 0, yColumns, 1, yAxisColumns.length );
        colorSupplier = ColorUtils.createColorSupplier( yColumns.length );
    }

    @Override
    public void display() {
        JFrame frame = new JFrame( "Plot" );
        frame.setVisible( true );
        display( frame );
    }

    @Override
    public void display( @NonNull JFrame frame ) {
//        plot.getAxisRenderer( XYPlot.AXIS_X ).setIntersection( -Double.MAX_VALUE );
//        plot.getAxisRenderer( XYPlot.AXIS_Y ).setIntersection( -Double.MAX_VALUE );
        InteractivePanel interactivePanel = new InteractivePanel( generatePlot() );
        frame.getContentPane().add( interactivePanel, BorderLayout.CENTER );
        frame.setMinimumSize( frame.getContentPane().getMinimumSize() );
        frame.setSize( 1640, 1050 );
    }

    @Override
    public void export( File destination ) throws IOException {
        DrawableWriter writer = DrawableWriterFactory.getInstance().get( "image/png" );
        try ( OutputStream outputStream = new FileOutputStream( destination ) ) {
            writer.write( generatePlot(), outputStream, 1680, 1050 );
        }
    }

    private XYPlot generatePlot() {
        Class[] typeArray = new Class[1 + yColumns.length];
        EffectiveUtils.fillArray( typeArray, Double.class );
        DataTable data = new DataTable( typeArray );
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for ( int i = 0; i < table.getRowCount(); i++ ) {
            List<Double> values = new ArrayList<>();
            double xVal = mapper.apply( table.getCellContent( i, xColumn ) );
            minX = Math.min( minX, xVal );
            maxX = Math.max( maxX, xVal );
            values.add( xVal );
            for ( int j = 0; j < yColumns.length; j++ ) {
                double yVal = mapper.apply( table.getCellContent( i, yColumns[j] ) );
                minY = Math.min( minY, yVal );
                maxY = Math.max( maxY, yVal );
                values.add( yVal );
            }
            data.add( values );
        }
        DataSeries[] series = new DataSeries[yColumns.length];
        LineRenderer[] lines = new LineRenderer[yColumns.length];
        PointRenderer[] points = new PointRenderer[yColumns.length];
        for ( int i = 0; i < yColumns.length; i++ ) {
            if ( table.hasHeaders() ) {
                series[i] = new DataSeries( table.getHeader( yColumns[i] ), data, 0, yColumns[i] );
            } else {
                series[i] = new DataSeries( data, 0, yColumns[i] );
            }
            Color color = colorSupplier.nextColor();
            lines[i] = new DefaultLineRenderer2D();
            lines[i].setColor( color );
            points[i] = new DefaultPointRenderer2D();
            points[i].setColor( color );
        }
        XYPlot plot = new XYPlot( series );
        for ( int i = 0; i < yColumns.length; i++ ) {
            plot.setLineRenderers( series[i], lines[i] );
            plot.setPointRenderers( series[i], points[i] );
        }
        plot.setLegendVisible( true );
        plot.getAxisRenderer( XYPlot.AXIS_X ).setLabel( new Label( "X" ) );
        plot.getAxisRenderer( XYPlot.AXIS_Y ).setLabel( new Label( "Y" ) );
//        plot.getAxisRenderer( XYPlot.AXIS_X ).setTickSpacing( 1.0 );
//        plot.getAxisRenderer( XYPlot.AXIS_Y ).setTickSpacing( 2.0 );
        double xBorder = ( maxX - minX ) / 10;
        minX = Math.min( minX, -xBorder );
        maxX = maxX + xBorder;
        double yBorder = ( maxY - minY ) / 10;
        minY = Math.min( minY, -yBorder );
        maxY = maxY + yBorder;
        plot.getAxis( XYPlot.AXIS_X ).setRange( minX, maxX );
        plot.getAxis( XYPlot.AXIS_Y ).setRange( minY, maxY );
        return plot;
    }

}
