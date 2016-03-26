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
package graphrithms.matching.directed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.graph.DirectedGraph;
import graphrithms.matching.util.AbstractMatcherState;
import org.apache.commons.collections15.map.LinkedMap;

public class DirectedTraversalState<V>
{
	private static final Integer DEPTH_ZERO = 0; // not from any terminal set

	private final DirectedGraph<V,?> graph;
	protected final LinkedMap<V,Integer> inTerminalSet;
	protected final LinkedMap<V,Integer> outTerminalSet;
	protected final Map<V,Integer[]> visitedSet;
	private final Collection<V> inBacktracking;
	private final Collection<V> outBacktracking;
	protected final Integer depth;

	public DirectedTraversalState(DirectedGraph<V,?> graph)
	{
		this.graph = graph;
		int vertexCount = graph.getVertexCount();
		this.inTerminalSet = new LinkedMap<V,Integer>(vertexCount);
		this.outTerminalSet = new LinkedMap<V,Integer>(vertexCount);
		this.visitedSet = new HashMap<V,Integer[]>(vertexCount);
		this.inBacktracking = Collections.emptySet();
		this.outBacktracking = Collections.emptySet();
		this.depth = DEPTH_ZERO;
	}

	protected DirectedTraversalState(DirectedTraversalState<V> s, V v)
	{
		this.graph = s.graph;
		this.inTerminalSet = s.inTerminalSet;
		this.outTerminalSet = s.outTerminalSet;
		this.visitedSet = s.visitedSet;

		// promote added vertex from terminal sets to visited set
		Integer inDepthVertexAdded = !inTerminalSet.isEmpty() ? inTerminalSet.remove(v) : DEPTH_ZERO;
		Integer outDepthVertexAdded = !outTerminalSet.isEmpty() ? outTerminalSet.remove(v) : DEPTH_ZERO;
		visitedSet.put(v, new Integer[] {inDepthVertexAdded, outDepthVertexAdded});

		// add unvisited neighbours to terminal sets
		this.depth = s.depth + 1;
		this.inBacktracking = AbstractMatcherState.extendTerminalSet(inTerminalSet, v, graph.getPredecessors(v), this.depth, visitedSet);
		this.outBacktracking = AbstractMatcherState.extendTerminalSet(outTerminalSet, v, graph.getSuccessors(v), this.depth, visitedSet);
	}

	public DirectedTraversalState<V> newState(V v)
	{
		return new DirectedTraversalState<V>(this, v);
	}

	public final boolean inTerminalSetContains(V v)
	{
		return inTerminalSet.containsKey(v);
	}

	public final int inTerminalSetSize()
	{
		return inTerminalSet.size();
	}

	public final V getFirstInTerminal()
	{
		return inTerminalSet.firstKey();
	}

	public final Collection<V> inTerminalSet()
	{
		return new ArrayList<V>(inTerminalSet.keySet());
	}

	public final boolean outTerminalSetContains(V v)
	{
		return outTerminalSet.containsKey(v);
	}

	public final int outTerminalSetSize()
	{
		return outTerminalSet.size();
	}

	public final V getFirstOutTerminal()
	{
		return outTerminalSet.firstKey();
	}

	public final Collection<V> outTerminalSet()
	{
		return new ArrayList<V>(outTerminalSet.keySet());
	}

	public void backtrack(final V addedVertex)
	{
		// remove added neighbours from terminal sets
		for(V v : inBacktracking)
		{
			Integer depth = inTerminalSet.remove(v);
			assert this.depth.equals(depth); // remove everything with the current depth from the terminal set
		}
		for(V v : outBacktracking)
		{
			Integer depth = outTerminalSet.remove(v);
			assert this.depth.equals(depth); // remove everything with the current depth from the terminal set
		}

		// demote added vertex from visited set to terminal sets
		Integer[] depths = visitedSet.remove(addedVertex);
		Integer inDepth = depths[0];
		if(inDepth != null && inDepth != DEPTH_ZERO)
		{
			inTerminalSet.put(addedVertex, inDepth);
		}
		Integer outDepth = depths[1];
		if(outDepth != null && outDepth != DEPTH_ZERO)
		{
			outTerminalSet.put(addedVertex, outDepth);
		}
	}
}
