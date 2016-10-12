/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.data;

import cz.blahami2.routingsaratester.model.Input;
import cz.certicon.routing.data.basic.DataDestination;
import cz.certicon.routing.data.basic.DataSource;
import java.io.IOException;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public interface InputDAO {

    public Input loadInput( DataSource source ) throws IOException;

    public void saveInput( DataDestination destination, Input input ) throws IOException;
}
