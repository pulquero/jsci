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
package graphrithms.matching.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.BidiMap;

import edu.uci.ics.jung.graph.Graph;
import graphrithms.matching.Matcher;
import graphrithms.matching.MatcherState;

public abstract class AbstractMatcherState<V,E,U,F> implements MatcherState<V,E,U,F>
{
	protected final Graph<V,E> graph;
	protected final Graph<U,F> pattern;
	protected final DualLinkedBidiMap<U,V> mapping;
	private final Matcher<U,V> vertexMatcher;
	private final Matcher<F,E> edgeMatcher;

	public AbstractMatcherState(Graph<V,E> graph, Graph<U,F> pattern, Matcher<U,V> vertexMatcher, Matcher<F,E> edgeMatcher)
	{
		this.graph = graph;
		this.pattern = pattern;
		this.mapping = new DualLinkedBidiMap<U,V>(pattern.getVertexCount());
		this.vertexMatcher = (vertexMatcher != null) ? vertexMatcher : TrueMatcher.<U,V>getInstance();
		this.edgeMatcher = (edgeMatcher != null) ? edgeMatcher : TrueMatcher.<F,E>getInstance();
	}

	protected AbstractMatcherState(AbstractMatcherState<V,E,U,F> s, U pv, V gv)
	{
		this.graph = s.graph;
		this.pattern = s.pattern;
		this.mapping = s.mapping;
		this.vertexMatcher = s.vertexMatcher;
		this.edgeMatcher = s.edgeMatcher;
		this.mapping.put(pv, gv);
	}

	@Override
	public boolean isGoal()
	{
		return (mapping.size() == pattern.getVertexCount());
	}

	@Override
	public final BidiMap<U,V> getMapping()
	{
		return new DualLinkedBidiMap<U,V>(mapping);
	}

	@Override
	public final int getMappingSize()
	{
		return mapping.size();
	}

	@Override
	public boolean isFeasible(U u, V v)
	{
		return matchVertex(u, v) && matchEdges(u, v);
	}

	protected abstract boolean matchEdges(U u, V v);

	protected final boolean matchVertex(U u, V v)
	{
		return vertexMatcher.match(u, v);
	}

	protected final boolean matchEdge(F pe, E ge)
	{
		return edgeMatcher.match(pe, ge);
	}

	@Override
	public final void backtrack()
	{
		// remove added vertices from mapping
		U pv = mapping.lastKey();
		V gv = mapping.remove(pv);
		backtrack(pv, gv);
	}

	protected abstract void backtrack(U u, V v);

	public static <T> Collection<T> extendTerminalSet(Map<T,Integer> terminalSet, T addedVertex, Collection<T> neighbours, Integer depth, Map<T,?> visitedSet)
	{
		// add unmapped neighbours to terminal set
		List<T> added = new ArrayList<T>(neighbours.size());
		for(T t : neighbours)
		{
			if(!terminalSet.containsKey(t) && !visitedSet.containsKey(t))
			{
				terminalSet.put(t, depth);
				added.add(t);
			}
		}
		return added;
	}
}
