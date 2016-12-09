package cz.blahami2.routingsaratester.parametertuning.model;

import cz.certicon.routing.model.values.Number;

/**
 * @author Michael Blaha {@literal <blahami2@gmail.com>}
 */
public class NumberAccumulator<T extends Number<T>> {

    private T current;

    public NumberAccumulator( T init ) {
        this.current = init;
    }

    public void add( T number ) {
        current = current.add( number );
    }

    public T get(){
        return current;
    }
}
