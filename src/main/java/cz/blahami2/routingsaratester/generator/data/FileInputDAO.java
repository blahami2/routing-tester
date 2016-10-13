/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.generator.data;

import cz.blahami2.routingsaratester.model.Input;
import cz.blahami2.routingsaratester.model.InputElement;
import cz.certicon.routing.data.basic.DataDestination;
import cz.certicon.routing.data.basic.DataSource;
import cz.certicon.routing.model.values.Length;
import cz.certicon.routing.model.values.LengthUnits;
import cz.certicon.routing.model.values.Time;
import cz.certicon.routing.model.values.TimeUnits;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class FileInputDAO implements InputDAO {

    @Override
    public Input loadInput( DataSource source ) throws IOException {
        Input.Builder builder = Input.builder();
        try ( Scanner sc = new Scanner( source.getInputStream() ) ) {
            while ( sc.hasNext() ) {
                String line = sc.nextLine();
                Scanner lsc = new Scanner( line );
                long inputId = lsc.nextLong();
                long sourceId = lsc.nextLong();
                long targetId = lsc.nextLong();
                long length = lsc.nextLong();
                long time = lsc.nextLong();
                List<Long> edgeIds = new ArrayList<>();
                while ( lsc.hasNext() ) {
                    edgeIds.add( lsc.nextLong() );
                }
                builder.add( new InputElement( inputId, sourceId, targetId, new Length( LengthUnits.METERS, length ), new Time( TimeUnits.SECONDS, time ), edgeIds ) );
            }
        }
        source.close();
        return builder.build();
    }

    @Override
    public void saveInput( DataDestination destination, Input input ) throws IOException {
        try ( Writer writer = new BufferedWriter( new OutputStreamWriter( destination.getOutputStream() ) ) ) {
            for ( InputElement inputElement : input ) {
                writer.append( inputElement.getId() + " " )
                        .append( inputElement.getSourceNodeId() + " " )
                        .append( inputElement.getTargetNodeId() + " " )
                        .append( inputElement.getLength().getValue( LengthUnits.METERS ) + " " )
                        .append( inputElement.getTime().getValue( TimeUnits.SECONDS ) + " " )
                        .append( inputElement.getEdgeIds().stream().map( x -> x.toString() ).collect( Collectors.joining( " " ) ) + "\n" );
            }
        }
        destination.close();
    }

}
