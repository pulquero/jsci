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

import edu.uci.ics.jung.graph.UndirectedGraph;
import graphrithms.matching.Matcher;
import graphrithms.matching.undirected.UndirectedTraversalState;
import graphrithms.matching.util.AbstractMatcherState;
import graphrithms.matching.util.NestedIterable;
import graphrithms.matching.util.PairIterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractUndirectedMCSMatcherState<V,E,U,F> extends AbstractMatcherState<V,E,U,F> implements MCSMatcherState<V,E,U,F>
{
	protected final UndirectedMCSTraversalState<U> patternState;
	protected final UndirectedTraversalState<V> graphState;
	private final NestedIterable<U,V> candidates;
	private int[] maxMappingSizeHolder;

	protected AbstractUndirectedMCSMatcherState(UndirectedGraph<V,E> graph, UndirectedGraph<U,F> pattern, Matcher<U,V> vertexMatcher, Matcher<F,E> edgeMatcher)
    {
        super(graph, pattern, vertexMatcher, edgeMatcher);
		this.patternState = new UndirectedMCSTraversalState<U>(pattern);
		this.graphState = new UndirectedTraversalState<V>(graph);
		this.candidates = createDefaultCandidates();
		this.maxMappingSizeHolder = new int[1];
    }

    protected AbstractUndirectedMCSMatcherState(AbstractUndirectedMCSMatcherState<V,E,U,F> s, U u, V v)
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
		Collection<U> pterm = patternState.terminalSet();
		List<U> unvisitedTerm = new ArrayList<U>(pterm.size());
		for(U u : pterm)
		{
			if(!patternState.visitedSetContains(u))
			{
				unvisitedTerm.add(u);
			}
		}
    	return PairIterables.crossProduct(unvisitedTerm, graphState.terminalSet());
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
		Collection<V> gterm = graphState.terminalSet();
		for(U u : pattern.getVertices())
		{
			if(!patternState.visitedSetContains(u))
			{
				if(patternState.terminalSetContains(u))
				{
					for(V v : gterm)
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
