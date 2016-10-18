/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.common.model;

import cz.certicon.routing.model.Identifiable;
import cz.certicon.routing.model.values.Length;
import cz.certicon.routing.model.values.Time;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
@Value
@EqualsAndHashCode
public class InputElement implements Identifiable {

    long id;
    long sourceNodeId;
    long targetNodeId;
    Length length;
    Time time;
    List<Long> edgeIds;

}
