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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.graph.UndirectedGraph;
import graphrithms.matching.util.AbstractMatcherState;
import org.apache.commons.collections15.map.LinkedMap;

public class UndirectedTraversalState<V>
{
	private static final Integer DEPTH_ZERO = 0; // not from any terminal set

	private final UndirectedGraph<V,?> graph;
	protected final LinkedMap<V,Integer> terminalSet;
	protected final Map<V,Integer> visitedSet;
	private final Collection<V> backtracking;
	protected final Integer depth;

	public UndirectedTraversalState(UndirectedGraph<V,?> graph)
	{
		this.graph = graph;
		int vertexCount = graph.getVertexCount();
		this.terminalSet = new LinkedMap<V,Integer>(vertexCount);
		this.visitedSet = new HashMap<V,Integer>(vertexCount);
		this.backtracking = Collections.emptySet();
		this.depth = DEPTH_ZERO;
	}

	protected UndirectedTraversalState(UndirectedTraversalState<V> s, V v)
	{
		this.graph = s.graph;
		this.terminalSet = s.terminalSet;
		this.visitedSet = s.visitedSet;

		// promote added vertex from terminal set to visited set
		Integer depthVertexAdded = !terminalSet.isEmpty() ? terminalSet.remove(v) : DEPTH_ZERO;
		visitedSet.put(v, depthVertexAdded);

		// add unvisited neighbours to terminal set
		this.depth = s.depth + 1;
		this.backtracking = AbstractMatcherState.extendTerminalSet(terminalSet, v, graph.getNeighbors(v), this.depth, visitedSet);
	}

	public UndirectedTraversalState<V> newState(V v)
	{
		return new UndirectedTraversalState<V>(this, v);
	}

	public final boolean terminalSetContains(V v)
	{
		return terminalSet.containsKey(v);
	}

	public final int terminalSetSize()
	{
		return terminalSet.size();
	}

	public final V getFirstTerminal()
	{
		return terminalSet.firstKey();
	}

	public final Collection<V> terminalSet()
	{
		return new ArrayList<V>(terminalSet.keySet());
	}

	public void backtrack(final V addedVertex)
	{
		// remove added neighbours from terminal set
		for(V v : backtracking)
		{
			Integer depth = terminalSet.remove(v);
			assert this.depth.equals(depth); // remove everything with the current depth from the terminal set
		}

		// demote added vertex from visited set to terminal set
		Integer depth = visitedSet.remove(addedVertex);
		if(depth != DEPTH_ZERO)
		{
			terminalSet.put(addedVertex, depth);
		}
	}
}
