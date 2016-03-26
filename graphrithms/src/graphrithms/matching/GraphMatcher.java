/*
 * Based on:
 * 
 * The VFLib distribution is
 * Copyright (c) 2001 by 
 * Dipartimento di Informatica e Sistemistica 
 * Universit� degli studi di Napoli ``Federico II'' 
 * http://amalfi.dis.unina.it
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions: The above
 * copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS",
 * WITHOUT WARRANTY OF ANY KIND, EXPRESS ORIMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THEAUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHERLIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM,OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS INTHE SOFTWARE.
 */
package graphrithms.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.MapIterator;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import graphrithms.util.PairIterator;
import graphrithms.matching.directed.DirectedEdgeSubgraphMatcherState;
import graphrithms.matching.directed.DirectedGraphMatcherState;
import graphrithms.matching.directed.DirectedInducedSubgraphMatcherState;
import graphrithms.matching.mcs.DirectedMCESMatcherState;
import graphrithms.matching.mcs.DirectedMCISMatcherState;
import graphrithms.matching.mcs.FirstNMCSVisitor;
import graphrithms.matching.mcs.MCSMatcherState;
import graphrithms.matching.mcs.UndirectedMCESMatcherState;
import graphrithms.matching.mcs.UndirectedMCISMatcherState;
import graphrithms.matching.undirected.UndirectedEdgeSubgraphMatcherState;
import graphrithms.matching.undirected.UndirectedGraphMatcherState;
import graphrithms.matching.undirected.UndirectedInducedSubgraphMatcherState;
import graphrithms.matching.util.*;
import java.util.*;

/**
 * A collection of graph matching methods.
 */
public final class GraphMatcher
{
    // heuristics
    private static final int MINIMUM_DEGREE = 2; // based on real-world scale-free networks (conservative guess)
    private static final int AVERAGE_DEGREE = 2*MINIMUM_DEGREE; // based on a scale-free network

    private GraphMatcher()
	{
	}

    /**
     * Matches (isomorphism) two undirected graphs.
     */
	public static <V,E,U,F> Set<BidiMap<U,V>> matchGraph(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int maxMatches)
	{
		FirstNVisitor<U,V> visitor = new FirstNVisitor<U,V>(maxMatches);
		UndirectedGraphMatcherState<V,E,U,F> state = new UndirectedGraphMatcherState<V,E,U,F>(g, pattern);
		match(state, visitor);
		return visitor.getMappings();
	}

    /**
     * Matches (isomorphism) two directed graphs.
     */
    public static <V,E,U,F> Set<BidiMap<U,V>> matchGraph(DirectedGraph<V,E> g, DirectedGraph<U,F> pattern, int maxMatches)
    {
        FirstNVisitor<U,V> visitor = new FirstNVisitor<U,V>(maxMatches);
        DirectedGraphMatcherState<V,E,U,F> state = new DirectedGraphMatcherState<V,E,U,F>(g, pattern);
        match(state, visitor);
        return visitor.getMappings();
    }

    /**
	 * Finds occurrences (monomorphisms) of an undirected subgraph in an undirected graph.
	 * @param g the graph to search
	 * @param pattern the subgraph to find
	 */
	public static <V,E,U,F> Set<BidiMap<U,V>> matchSubgraph(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int maxMatches)
	{
		FirstNVisitor<U,V> visitor = new FirstNVisitor<U,V>(maxMatches);
		UndirectedEdgeSubgraphMatcherState<V,E,U,F> state = new UndirectedEdgeSubgraphMatcherState<V,E,U,F>(g, pattern);
		match(state, visitor);
		return visitor.getMappings();
	}

    /**
     * Finds occurrences (monomorphisms) of a directed subgraph in a directed graph.
     * @param g the graph to search
     * @param pattern the subgraph to find
     */
	public static <V,E,U,F> Set<BidiMap<U,V>> matchSubgraph(DirectedGraph<V,E> g, DirectedGraph<U,F> pattern, int maxMatches)
	{
		FirstNVisitor<U,V> visitor = new FirstNVisitor<U,V>(maxMatches);
		DirectedEdgeSubgraphMatcherState<V,E,U,F> state = new DirectedEdgeSubgraphMatcherState<V,E,U,F>(g, pattern);
		match(state, visitor);
		return visitor.getMappings();
	}

    /**
     * Finds occurrences (induced subgraph isomorphisms) of an undirected subgraph in an undirected graph.
     * @param g the graph to search
     * @param pattern the subgraph to find
     */
	public static <V,E,U,F> Set<BidiMap<U,V>> matchInducedSubgraph(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int maxMatches)
	{
		FirstNVisitor<U,V> visitor = new FirstNVisitor<U,V>(maxMatches);
		UndirectedInducedSubgraphMatcherState<V,E,U,F> state = new UndirectedInducedSubgraphMatcherState<V,E,U,F>(g, pattern);
		match(state, visitor);
		return visitor.getMappings();
	}

    /**
     * Finds occurrences (induced subgraph isomorphisms) of a directed subgraph in a directed graph.
     * @param g the graph to search
     * @param pattern the subgraph to find
     */
	public static <V,E,U,F> Set<BidiMap<U,V>> matchInducedSubgraph(DirectedGraph<V,E> g, DirectedGraph<U,F> pattern, int maxMatches)
	{
		FirstNVisitor<U,V> visitor = new FirstNVisitor<U,V>(maxMatches);
		DirectedInducedSubgraphMatcherState<V,E,U,F> state = new DirectedInducedSubgraphMatcherState<V,E,U,F>(g, pattern);
		match(state, visitor);
		return visitor.getMappings();
	}

	/**
	 * Finds the maximum common (induced) subgraph of two graphs.
	 */
	public static <V,E,U,F> Set<BidiMap<U,V>> findMCIS(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int maxMatches)
	{
		FirstNMCSVisitor<U,V> visitor = new FirstNMCSVisitor<U,V>(maxMatches);
		UndirectedMCISMatcherState<V,E,U,F> state = new UndirectedMCISMatcherState<V,E,U,F>(g, pattern);
		matchMCS(state, visitor);
		return visitor.getMappings();
	}

    /**
     * Finds the maximum common (induced) subgraph of two graphs.
     */
    public static <V,E,U,F> Set<BidiMap<U,V>> findMCIS(DirectedGraph<V,E> g, DirectedGraph<U,F> pattern, int maxMatches)
    {
        FirstNMCSVisitor<U,V> visitor = new FirstNMCSVisitor<U,V>(maxMatches);
        DirectedMCISMatcherState<V,E,U,F> state = new DirectedMCISMatcherState<V,E,U,F>(g, pattern);
        matchMCS(state, visitor);
        return visitor.getMappings();
    }

    /**
     * Finds the maximum common (edge) subgraph of two graphs.
     */
	public static <V,E,U,F> Set<BidiMap<U,V>> findMCES(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int maxMatches)
	{
		FirstNMCSVisitor<U,V> visitor = new FirstNMCSVisitor<U,V>(maxMatches);
		UndirectedMCESMatcherState<V,E,U,F> state = new UndirectedMCESMatcherState<V,E,U,F>(g, pattern);
		matchMCS(state, visitor);
		return visitor.getMappings();
	}

    /**
     * Finds the maximum common (edge) subgraph of two graphs.
     */
    public static <V,E,U,F> Set<BidiMap<U,V>> findMCES(DirectedGraph<V,E> g, DirectedGraph<U,F> pattern, int maxMatches)
    {
        FirstNMCSVisitor<U,V> visitor = new FirstNMCSVisitor<U,V>(maxMatches);
        DirectedMCESMatcherState<V,E,U,F> state = new DirectedMCESMatcherState<V,E,U,F>(g, pattern);
        matchMCS(state, visitor);
        return visitor.getMappings();
    }

    /**
	 * Performs graph matching.
	 */
	public static <V,E,U,F> boolean match(MatcherState<V,E,U,F> state, Visitor<U,V> visitor)
	{
		boolean stop = false;
		if(!state.isDead())
		{
		    for(PairIterator<U,V> iter = state.candidates().iterator(); iter.hasNext(); )
			{
			    final U u = iter.nextFirst();
			    final V v = iter.getSecond();
				if(state.isFeasible(u, v))
				{
					MatcherState<V,E,U,F> nextState = state.newState(u, v);
					if(nextState.isGoal())
					{
						stop = visitor.visit(nextState.getMapping());
					}
					else
					{
						stop = match(nextState, visitor);
					}
					if(stop)
					{
						break;
					}
					nextState.backtrack();
				}
			}
		}
		return stop;
	}

    /**
     * Performs maximum common subgraph matching.
     */
	public static <V,E,U,F> boolean matchMCS(MCSMatcherState<V,E,U,F> state, Visitor<U,V> visitor)
	{
		boolean stop = false;
		if(!state.isDead())
		{
			NestedIterable<U,V> candidates = state.candidates();
			for(NestedIterator<U,V> nestedIter = candidates.nestedIterator(); nestedIter.hasNext(); )
			{
				final U u = nestedIter.next();
				for(Iterator<V> iter = nestedIter.iterator(); iter.hasNext(); )
				{
					final V v = iter.next();
					if(state.isFeasible(u, v))
					{
						MCSMatcherState<V,E,U,F> nextState = state.newState(u, v);
						if(nextState.getMappingSize() >= nextState.getMaxMappingSize())
						{
							stop = visitor.visit(nextState.getMapping());
						}
						if(!nextState.isGoal() && !stop)
						{
							stop = matchMCS(nextState, visitor);
						}
						if(stop)
						{
							break;
						}
						nextState.backtrack();
					}
				}
				state.markVisited(u);
			}
		}
		return stop;
	}

	/**
	 * Returns the subgraph mapped from pattern to graph.
	 */
	public static <V,E,U,F> Graph<U,F> createMappedGraph(BidiMap<U,V> mapping, Graph<U,F> pattern, Graph<V,E> graph)
	{
		Graph<U,F> mappedGraph;
		if((pattern instanceof UndirectedGraph<?,?>) && (graph instanceof UndirectedGraph<?,?>))
		{
			mappedGraph = new UndirectedSparseGraph<U,F>();
		}
		else if((pattern instanceof DirectedGraph<?, ?>) && (graph instanceof DirectedGraph<?, ?>))
		{
			mappedGraph = new DirectedSparseGraph<U,F>();
		}
		else
		{
			mappedGraph = new SparseGraph<U,F>();
		}
		for(MapIterator<U,V> iter = mapping.mapIterator(); iter.hasNext(); )
		{
			final U u1 = iter.next();
			final V v1 = iter.getValue();
			mappedGraph.addVertex(u1);
			for(final F e : pattern.getIncidentEdges(u1))
			{
				final U u2 = pattern.getOpposite(u1, e);
				final V v2 = mapping.get(u2);
				// if u2 is mapped and the edge between u1 and u2 is mapped
				// and the edge hasn't already been added
				// then add it
				if(v2 != null && graph.findEdge(v1, v2) != null && !mappedGraph.containsEdge(e))
				{
					mappedGraph.addEdge(e, u1, u2);
				}
			}
		}
		return mappedGraph;
	}

	/**
	 * Returns the edges mapped from pattern to graph.
	 */
	public static <V,E,U,F> Set<F> getMappedEdges(BidiMap<U,V> mapping, Graph<U,F> pattern, Graph<V,E> graph)
	{
		Set<F> edges = new HashSet<F>(mapping.size()*AVERAGE_DEGREE);
        for(MapIterator<U,V> iter = mapping.mapIterator(); iter.hasNext(); )
        {
            final U u1 = iter.next();
            final V v1 = iter.getValue();
            for(final F e : pattern.getIncidentEdges(u1))
            {
                final U u2 = pattern.getOpposite(u1, e);
                final V v2 = mapping.get(u2);
                // if u2 is mapped and the edge between u1 and u2 is mapped
                // then add it
    			if(v2 != null && graph.findEdge(v1, v2) != null)
    			{
	    			edges.add(e);
    			}
    		}
    	}
		return edges;
	}
	/**
	 * Returns the number of edges mapped from pattern to graph.
	 */
	public static <U,V> int getMappedEdgeCount(BidiMap<U,V> mapping, Graph<U,?> pattern, Graph<V,?> graph)
	{
		int totalDegree = 0;
        for(MapIterator<U,V> iter = mapping.mapIterator(); iter.hasNext(); )
        {
            final U u1 = iter.next();
            final V v1 = iter.getValue();
			for(final U u2 : pattern.getNeighbors(u1))
            {
                final V v2 = mapping.get(u2);
                // if u2 is mapped and the edge between u1 and u2 is mapped
                // then count it
    			if(v2 != null && graph.findEdge(v1, v2) != null)
    			{
    				totalDegree++;
    			}
    		}
		}
		int edgeCount = totalDegree/2;
		return edgeCount;
	}

	/**
	 * Returns the subset of mappings that match the most edges.
	 */
	public static <V,E,U,F> Set<BidiMap<U,V>> getMostEdgesMappings(Set<BidiMap<U,V>> mappings, final Graph<U,F> pattern, final Graph<V,E> graph)
	{
		BestElementsSet<BidiMap<U,V>> bestMappings = new BestElementsSet<BidiMap<U,V>>(
				37,
				new Function<BidiMap<U,V>>()
				{
					@Override
					public double evaluate(BidiMap<U,V> mapping)
					{
						return getMappedEdgeCount(mapping, pattern, graph);
					}
				}
		);
		bestMappings.addAll(mappings);
		return bestMappings;
	}

	/**
	 * Returns the subset of mappings that have supergraphs with the most degree variance.
	 */
	public static <V,E,U,F> Set<BidiMap<U,V>> getBestSupergraphMappings(Set<BidiMap<U,V>> mappings, final Graph<U,F> pattern, final Graph<V,E> graph)
	{
		BestElementsSet<BidiMap<U,V>> bestMappings = new BestElementsSet<BidiMap<U,V>>(
				37,
				new Function<BidiMap<U,V>>()
				{
					@Override
					public double evaluate(BidiMap<U,V> mapping)
					{
						return getSupergraphDegreeVariance(mapping, pattern, graph);
					}
				}
		);
		bestMappings.addAll(mappings);
		return bestMappings;
	}
	/**
	 * Returns the degree variance of the combined pattern+graph edges for the mapped vertices.
	 */
	public static <U,V> double getSupergraphDegreeVariance(BidiMap<U,V> mapping, Graph<U,?> pattern, Graph<V,?> graph)
	{
		int totalDegree = 0;
		int[] degrees = new int[mapping.size()];
		int i = 0;
		for(MapIterator<U,V> iter = mapping.mapIterator(); iter.hasNext(); i++)
		{
			final U u1 = iter.next();
			final V v1 = iter.getValue();
			int degree = pattern.degree(u1);
			for(final V v2 : graph.getNeighbors(v1))
			{
				if(!mapping.containsValue(v2))
				{
					degree++;
				}
			}
			totalDegree += degree;
			degrees[i] = degree;
		}
		final double mappedVertexCount = mapping.size();
		final double meanDeg = totalDegree/mappedVertexCount;
		double variance = 0.0;
		for(int deg : degrees)
		{
			double degDiff = deg - meanDeg;
			variance += degDiff*degDiff;
		}
		variance /= mappedVertexCount;
		return variance;
	}

	/**
	 * Returns the minimum common supergraph of two graphs.
	 */
	public static <V,E,U,F> Graph<V,E> createMinSupergraph(BidiMap<U,V> mapping, Graph<U,F> pattern, Graph<V,E> graph, Factory<V> vertexFactory, Factory<E> edgeFactory)
	{
		Graph<V,E> mcs;
		if((pattern instanceof UndirectedGraph<?,?>) && (graph instanceof UndirectedGraph<?,?>))
		{
			mcs = new UndirectedSparseGraph<V,E>();
		}
		else if((pattern instanceof DirectedGraph<?, ?>) && (graph instanceof DirectedGraph<?, ?>))
		{
			mcs = new DirectedSparseGraph<V,E>();
		}
		else
		{
			mcs = new SparseGraph<V,E>();
		}
		// add graph
		for(final E edge : graph.getEdges())
		{
			Pair<V> ends = graph.getEndpoints(edge);
			mcs.addEdge(edge, ends);
		}
		// glue pattern minus mapping
		Map<U,V> glued = new HashMap<U,V>(pattern.getVertexCount() - mapping.size());
		for(final F edge : pattern.getEdges())
		{
			Pair<U> ends = pattern.getEndpoints(edge);
			U u1 = ends.getFirst();
			U u2 = ends.getSecond();
			V v1 = mapping.get(u1);
			V v2 = mapping.get(u2);
			// if u1 not mapped then get the glue
			if(v1 == null)
			{
				v1 = glued.get(u1);
				// if not already glued then glue it
				if(v1 == null)
				{
					v1 = vertexFactory.create();
					mcs.addVertex(v1);
					glued.put(u1, v1);
				}
			}
			// if u2 not mapped then get the glue
			if(v2 == null)
			{
				v2 = glued.get(u2);
                // if not already glued then glue it
				if(v2 == null)
				{
					v2 = vertexFactory.create();
					mcs.addVertex(v2);
					glued.put(u2, v2);
				}
			}
			if(mcs.findEdge(v1, v2) == null)
			{
				mcs.addEdge(edgeFactory.create(), v1, v2);
			}
		}
		return mcs;
	}

	/**
	 * Bunke and Shearer metric.
	 */
	public static double mcsMetric(int g1Size, int g2Size, int mcsSize)
	{
		return 1.0 - ((double)mcsSize)/(double)Math.max(g1Size, g2Size);
	}
	/**
	 * Wallis et al metric.
	 */
	public static double mcsUnionMetric(int g1Size, int g2Size, int mcsSize)
	{
		return 1.0 - ((double)mcsSize)/(double)(g1Size + g2Size - mcsSize);
	}
	/**
	 * Fernandez and Valiente metric.
	 */
	public static double mcsMCSMetric(int mcsSize, int MCSSize)
	{
		return 1.0 - ((double)mcsSize)/((double)MCSSize);
	}

	/**
	 * Returns a new graph with its vertices sorted by degree frequency.
	 */
	public static <V,E> UndirectedGraph<V,E> sortByDegreeFrequency(final UndirectedGraph<V,E> graph)
	{
		final Map<Integer,Integer> degreeFreq = new HashMap<Integer,Integer>();
		for(V v : graph.getVertices())
		{
			int degree = graph.degree(v);
			Integer freq = degreeFreq.get(degree);
			if(freq == null)
			{
				freq = 1;
			}
			else
			{
				freq++;
			}
			degreeFreq.put(degree, freq);
		}

		List<V> sortedVertices = new ArrayList<V>(graph.getVertices());
		Collections.sort(sortedVertices, new Comparator<V>()
		{
			@Override
			public int compare(V v1, V v2)
			{
				int deg1 = graph.degree(v1);
				int deg2 = graph.degree(v2);
				int freq1 = degreeFreq.get(deg1);
				int freq2 = degreeFreq.get(deg2);
				return (freq1 != freq2 ? freq1 - freq2 : deg2 - deg1);
			}
		});

		UndirectedOrderedSparseMultigraph<V,E> sortedGraph = new UndirectedOrderedSparseMultigraph<V,E>();
		for(V v : sortedVertices)
		{
			sortedGraph.addVertex(v);
		}
		for(E e : graph.getEdges())
		{
			sortedGraph.addEdge(e, graph.getEndpoints(e));
		}
		return sortedGraph;
	}
}
