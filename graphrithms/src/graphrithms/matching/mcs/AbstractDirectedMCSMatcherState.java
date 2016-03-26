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
package graphrithms.matching.mcs;

import edu.uci.ics.jung.graph.DirectedGraph;
import graphrithms.matching.Matcher;
import graphrithms.matching.directed.DirectedTraversalState;
import graphrithms.matching.util.AbstractMatcherState;
import graphrithms.matching.util.NestedIterable;
import graphrithms.matching.util.PairIterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractDirectedMCSMatcherState<V,E,U,F> extends AbstractMatcherState<V,E,U,F> implements MCSMatcherState<V,E,U,F>
{
    protected final DirectedMCSTraversalState<U> patternState;
    protected final DirectedTraversalState<V> graphState;
	private final NestedIterable<U,V> candidates;
	private int[] maxMappingSizeHolder;

	protected AbstractDirectedMCSMatcherState(DirectedGraph<V,E> graph, DirectedGraph<U,F> pattern, Matcher<U,V> vertexMatcher, Matcher<F,E> edgeMatcher)
    {
        super(graph, pattern, vertexMatcher, edgeMatcher);
		this.patternState = new DirectedMCSTraversalState<U>(pattern);
		this.graphState = new DirectedTraversalState<V>(graph);
		this.candidates = createDefaultCandidates();
		this.maxMappingSizeHolder = new int[1];
    }

    protected AbstractDirectedMCSMatcherState(AbstractDirectedMCSMatcherState<V,E,U,F> s, U u, V v)
    {
        super(s, u, v);
		this.patternState = s.patternState.newState(u);
		this.graphState = s.graphState.newState(v);
		this.candidates = createCandidates();
		this.maxMappingSizeHolder = s.maxMappingSizeHolder;
		this.maxMappingSizeHolder[0] = Math.max(mapping.size(), maxMappingSizeHolder[0]);
    }

    private NestedIterable<U,V> createDefaultCandidates()
    {
    	return PairIterables.crossProduct(pattern.getVertices(), graph.getVertices());
    }

    protected NestedIterable<U,V> createCandidates()
    {
        Collection<U> ptermOut = patternState.outTerminalSet();
		List<U> unvisitedTermOut = new ArrayList<U>(ptermOut.size());
		for(U u : ptermOut)
		{
			if(!patternState.visitedSetContains(u))
			{
				unvisitedTermOut.add(u);
			}
		}

		Collection<U> ptermIn = patternState.inTerminalSet();
		List<U> unvisitedTermIn = new ArrayList<U>(ptermIn.size());
		for(U u : ptermIn)
		{
			if(!patternState.visitedSetContains(u))
			{
				unvisitedTermIn.add(u);
			}
		}
        return PairIterables.sum(PairIterables.crossProduct(unvisitedTermOut, graphState.outTerminalSet()), PairIterables.crossProduct(unvisitedTermIn, graphState.inTerminalSet()));
    }

	@Override
	public final boolean isGoal()
	{
		return (mapping.size() == Math.min(pattern.getVertexCount(), graph.getVertexCount()));
	}

    @Override
    public final boolean isDead()
    {
		int possibleMatches = 0;
		Collection<V> ginterm = graphState.inTerminalSet();
		Collection<V> goutterm = graphState.outTerminalSet();
		for(U u : pattern.getVertices())
		{
			if(!patternState.visitedSetContains(u))
			{
				if(patternState.inTerminalSetContains(u))
				{
					for(V v : ginterm)
					{
						if(matchVertex(u, v) && matchEdges(u, v))
						{
							possibleMatches++;
							break;
						}
					}
				}
				else if(patternState.outTerminalSetContains(u))
				{
					for(V v : goutterm)
					{
						if(matchVertex(u, v) && matchEdges(u, v))
						{
							possibleMatches++;
							break;
						}
					}
				}
				else
				{
					for(V v : graph.getVertices())
					{
						if(!mapping.containsValue(v) && matchVertex(u, v))
						{
							possibleMatches++;
							break;
						}
					}
				}
			}
		}
		return mapping.size() + possibleMatches < maxMappingSizeHolder[0];
    }

	@Override
	public final NestedIterable<U,V> candidates()
	{
		return candidates;
	}

	@Override
	protected final void backtrack(U u, V v)
	{
		patternState.backtrack(u);
		graphState.backtrack(v);
	}

	@Override
	public final int getMaxMappingSize()
	{
		return maxMappingSizeHolder[0];
	}

	@Override
	public final void markVisited(U u)
	{
		patternState.addVisited(u);
	}
}
