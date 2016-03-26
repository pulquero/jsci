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

import java.util.Collection;

import edu.uci.ics.jung.graph.DirectedGraph;
import graphrithms.util.PairIterable;
import graphrithms.matching.Matcher;
import graphrithms.matching.util.AbstractMatcherState;
import graphrithms.matching.util.PairIterables;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDirectedSubgraphMatcherState<V,E,U,F> extends AbstractMatcherState<V,E,U,F>
{
    protected final DirectedTraversalState<U> patternState;
    protected final DirectedTraversalState<V> graphState;
	private final PairIterable<U,V> candidates;

    protected AbstractDirectedSubgraphMatcherState(DirectedGraph<V,E> graph, DirectedGraph<U,F> pattern, Matcher<U,V> vertexMatcher, Matcher<F,E> edgeMatcher)
	{
		super(graph, pattern, vertexMatcher, edgeMatcher);
		this.patternState = new DirectedTraversalState<U>(pattern);
		this.graphState = new DirectedTraversalState<V>(graph);
		this.candidates = createDefaultCandidates();
	}

	protected AbstractDirectedSubgraphMatcherState(AbstractDirectedSubgraphMatcherState<V,E,U,F> s, U pv, V gv)
	{
		super(s, pv, gv);
		this.patternState = s.patternState.newState(pv);
		this.graphState = s.graphState.newState(gv);
		this.candidates = createCandidates();
	}

	protected final PairIterable<U,V> createDefaultCandidates()
	{
		U pmin = getPatternMin();
		PairIterable<U,V> iter;
		if(pmin != null)
		{
            List<V> gvertices = new ArrayList<V>(graph.getVertexCount());
			for(V gv : graph.getVertices())
			{
				if(!mapping.containsValue(gv))
				{
				    gvertices.add(gv);
				}
			}
			iter = PairIterables.leftScalarProduct(pmin, gvertices);
		}
		else
		{
		    iter = PairIterables.empty();
		}
		return iter;
	}
	private U getPatternMin()
	{
        for(U p : pattern.getVertices())
        {
            if(!mapping.containsKey(p))
            {
                return p;
            }
        }
        return null;
	}

    protected PairIterable<U,V> createCandidates()
    {
	    U pmin = patternState.outTerminalSetSize() > 0 ? patternState.getFirstOutTerminal() : null;
        final Collection<V> gterm;
        if(pmin != null)
        {
            gterm = graphState.outTerminalSet();
        }
        else
        {
            pmin = patternState.inTerminalSetSize() > 0 ? patternState.getFirstInTerminal() : null;
            gterm = graphState.inTerminalSet();
        }
        final PairIterable<U,V> iter;
        if(pmin != null && !gterm.isEmpty())
        {
            iter = PairIterables.leftScalarProduct(pmin, gterm);
        }
        else if(pmin == null)
        {
            iter = createDefaultCandidates();
        }
        else
        {
            iter = PairIterables.empty();
        }
        return iter;
    }

	@Override
	public boolean isDead()
	{
		return (pattern.getVertexCount() > graph.getVertexCount())
		    || (patternState.inTerminalSetSize() > graphState.inTerminalSetSize())
		    || (patternState.outTerminalSetSize() > graphState.outTerminalSetSize());
	}

	@Override
	public final PairIterable<U,V> candidates()
	{
		return candidates;
	}

	@Override
	protected final void backtrack(U u, V v)
	{
		patternState.backtrack(u);
		graphState.backtrack(v);
	}
}
