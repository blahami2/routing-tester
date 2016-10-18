/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.common.utils;

import cz.certicon.routing.utils.collections.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class Iterators {

    public static <T> Stream<T> stream( Iterator<T> iterator ) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream( iterable.spliterator(), false );
    }
}
