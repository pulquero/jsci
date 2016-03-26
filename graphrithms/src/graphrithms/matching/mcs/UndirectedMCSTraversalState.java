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

import java.util.ArrayList;
import java.util.Collection;

import edu.uci.ics.jung.graph.UndirectedGraph;
import graphrithms.matching.undirected.UndirectedTraversalState;

public final class UndirectedMCSTraversalState<V> extends UndirectedTraversalState<V>
{
	private final Collection<V> visitedBacktracking;

	public UndirectedMCSTraversalState(UndirectedGraph<V,?> graph)
	{
		super(graph);
		this.visitedBacktracking = new ArrayList<V>(terminalSet.size());
	}

	protected UndirectedMCSTraversalState(UndirectedMCSTraversalState<V> s, V v)
	{
		super(s, v);
		this.visitedBacktracking = new ArrayList<V>(terminalSet.size());
	}

	@Override
	public UndirectedMCSTraversalState<V> newState(V v)
	{
		return new UndirectedMCSTraversalState<V>(this, v);
	}

	public void addVisited(V v)
	{
		visitedSet.put(v, depth);
		visitedBacktracking.add(v);
	}

	public boolean visitedSetContains(V v)
	{
		return visitedSet.containsKey(v);
	}

	@Override
	public void backtrack(final V addedVertex)
	{
		super.backtrack(addedVertex);

		for(V v : visitedBacktracking)
		{
			visitedSet.remove(v);
		}
	}
}
