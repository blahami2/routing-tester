/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.blahami2.routingsaratester.generator.logic;

import cz.blahami2.routingsaratester.common.utils.Iterators;
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
public class NodeGenerator {

    public <N extends Node<N, E>, E extends Edge<N, E>> Stream<Pair<N, N>> generatorStream( Graph<? extends N, ? extends E> graph ) {
        return StreamSupport.stream( new NodeSpliterator( graph ), false );
    }

    public <N extends Node<N, E>, E extends Edge<N, E>> Iterator<N> generatorIterator( Graph<? extends N, ? extends E> graph ) {
        return new NodeIterator( graph );
    }

    private static class NodeIterator<N extends Node<N, E>, E extends Edge<N, E>> implements Iterator<N> {

        private final Random rand = RandomUtils.createRandom();
        private final Node[] nodes;

        public NodeIterator( Graph<N, E> graph ) {
            this.nodes = Iterators.stream( graph.getNodes() ).collect( Collectors.toList() ).toArray( new Node[]{} );
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public N next() {
            return (N) nodes[rand.nextInt( nodes.length )];
        }

    }

    private static class NodeSpliterator<N extends Node<N, E>, E extends Edge<N, E>> implements Spliterator<N> {

        private final Random rand = RandomUtils.createRandom();
        private final Node[] nodes;

        public NodeSpliterator( Graph<? extends N, ? extends E> graph ) {
            this.nodes = Iterators.stream( graph.getNodes() ).collect( Collectors.toList() ).toArray( new Node[]{} );
        }

        @Override
        public boolean tryAdvance( Consumer<? super N> action ) {
            Node n = nodes[rand.nextInt( nodes.length )];
            action.accept( (N) n );
            return true;
        }

        @Override
        public Spliterator<N> trySplit() {
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
}
