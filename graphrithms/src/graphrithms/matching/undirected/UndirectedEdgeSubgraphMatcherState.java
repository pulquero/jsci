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
package graphrithms.matching.undirected;

import edu.uci.ics.jung.graph.UndirectedGraph;
import graphrithms.matching.Matcher;

public final class UndirectedEdgeSubgraphMatcherState<V,E,U,F> extends AbstractUndirectedSubgraphMatcherState<V,E,U,F>
{
	public UndirectedEdgeSubgraphMatcherState(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, Matcher<U,V> vertexMatcher, Matcher<F,E> edgeMatcher)
	{
		super(g, pattern, vertexMatcher, edgeMatcher);
	}
	public UndirectedEdgeSubgraphMatcherState(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern)
	{
		super(g, pattern, null, null);
	}

	private UndirectedEdgeSubgraphMatcherState(UndirectedEdgeSubgraphMatcherState<V,E,U,F> s, U u, V v)
	{
		super(s, u, v);
	}
	
	@Override
	public UndirectedEdgeSubgraphMatcherState<V,E,U,F> newState(U u, V v)
	{
		return new UndirectedEdgeSubgraphMatcherState<V,E,U,F>(this, u, v);
	}

	@Override
	protected final boolean matchEdges(U pv, V gv)
	{
		int pterm = 0;
		int pnew = 0;
        for(F pe : pattern.getIncidentEdges(pv))
        {
            U u = pattern.getOpposite(pv, pe);
			V v = mapping.get(u);
			if(v != null)
			{
				E ge = graph.findEdge(gv, v);
				if(ge == null || !matchEdge(pe, ge))
				{
					return false;
				}
			}
			else
			{
				if(patternState.terminalSetContains(u))
				{
					pterm++;
				}
				else
				{
					pnew++;
				}
			}
		}
		int gterm = 0;
		int gnew = 0;
		for(V v : graph.getNeighbors(gv))
		{
			U u = mapping.getKey(v);
			if(u == null)
			{
				if(graphState.terminalSetContains(v))
				{
					gterm++;
				}
				else
				{
					gnew++;
				}
			}
		}
		return (pterm <= gterm) && (pterm+pnew <= gterm+gnew);
	}
}
