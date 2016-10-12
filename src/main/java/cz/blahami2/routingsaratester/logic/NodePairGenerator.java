/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.logic;

import cz.blahami2.routingsaratester.utils.Iterators;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.graph.Edge;
import cz.certicon.routing.model.graph.Graph;
import cz.certicon.routing.model.graph.Node;
import cz.certicon.routing.utils.RandomUtils;
import java.util.Iterator;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@gmail.com>}
 */
public class NodePairGenerator {

    public <N extends Node<N, E>, E extends Edge<N, E>> Stream<Pair<N, N>> generatorStream( Graph<? extends N, ? extends E> graph ) {
        return StreamSupport.stream( new NodePairSpliterator( graph ), false );
    }

    public <N extends Node<N, E>, E extends Edge<N, E>> Iterator<Pair<N, N>> generatorIterator( Graph<? extends N, ? extends E> graph ) {
        return new NodePairIterator( graph );
    }

    private static class NodePairSpliterator<N extends Node<N, E>, E extends Edge<N, E>> implements Spliterator<Pair< ? super N, ? super N>> {

        private final Iterator<Pair<? super N, ? super N>> iterator;

        public NodePairSpliterator( Graph<? extends N, ? extends E> graph ) {
            iterator = new NodePairIterator( graph );
        }

        @Override
        public boolean tryAdvance( Consumer<? super Pair< ? super N, ? super N>> action ) {
            action.accept( iterator.next() );
            return true;
        }

        @Override
        public Spliterator<Pair<? super N, ? super N>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return 0;
        }

    }

    private static class NodePairIterator<N extends Node<N, E>, E extends Edge<N, E>> implements Iterator<Pair< ? super N, ? super N>> {

        private final Random rand = RandomUtils.createRandom();
        private final Node[] nodes;

        public NodePairIterator( Graph<N, E> graph ) {
            this.nodes = Iterators.stream( graph.getNodes() ).collect( Collectors.toList() ).toArray( new Node[]{} );
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Pair<? super Node, ? super Node> next() {
            int a = rand.nextInt( nodes.length );
            int b = rand.nextInt( nodes.length );
            if ( a == b ) {
                b = b + 1 % nodes.length;
            }
            return new Pair<>( nodes[a], nodes[b] );
        }

    }
}
