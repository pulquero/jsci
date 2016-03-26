package graphrithms.matching;

import edu.uci.ics.jung.graph.DirectedGraph;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import graphrithms.matching.undirected.UndirectedGraphMatcherState;
import graphrithms.matching.util.FirstNVisitor;

import org.apache.commons.collections15.BidiMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import graphdb.GraphDB;
import graphdb.UnlabeledEdge;
import graphdb.UnlabeledNode;
import java.io.DataInputStream;
import java.util.*;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

@RunWith(Parameterized.class)
public class MatcherTest
{
	private static final String PATH = "testdata/";
	private static final int MAX_MATCHES = -1;

	@Parameters
	public static Collection<Object[]> getTests()
	{
		return Arrays.<Object[]>asList(
				new Object[] {"test1"},
				new Object[] {"test2"},
				new Object[] {"test3"},
				new Object[] {"test4"}
		);
	}

	private final Properties properties = new Properties();
	private final UndirectedGraph<UnlabeledNode,UnlabeledEdge> g;
	private final UndirectedGraph<UnlabeledNode,UnlabeledEdge> h;
	private final UndirectedGraph<UnlabeledNode,UnlabeledEdge> mcis;
	private final UndirectedGraph<UnlabeledNode,UnlabeledEdge> mces;

	public MatcherTest(String testPrefix) throws Exception
	{
		testPrefix = PATH + testPrefix;
		FileInputStream in = new FileInputStream(testPrefix+".properties");
		try
		{
			properties.load(in);
		}
		finally
		{
			in.close();
		}
		g = loadGraph(testPrefix+"g.bin");
		h = loadGraph(testPrefix+"h.bin");
		mcis = loadGraph(testPrefix+"mcis.bin");
		mces = loadGraph(testPrefix+"mces.bin");
	}

	private int getMatches(String match)
	{
		String s = properties.getProperty(match);
		assertThat(match, s, notNullValue());
		return Integer.parseInt(s);
	}

	@Test
	public void testFindMCIS()
	{
		assertThat(isGraph(this.mcis, findMCIS(g, h, -1)), is(true));
	}
	@Test
	public void testFindMCIS_swapped()
	{
		assertThat(isGraph(this.mcis, findMCIS(h, g, -1)), is(true));
	}
	@Test
	public void testFindMCES()
	{
		assertThat(isGraph(this.mces, findMCES(g, h, -1)), is(true));
	}
	@Test
	public void testFindMCES_swapped()
	{
		assertThat(isGraph(this.mces, findMCES(h, g, -1)), is(true));
	}

	@Test
	public void testMCISIsInducedSubgraph()
	{
		assertThat(isInducedSubgraph(g, mcis, getMatches("g.mcis.induced")), is(true));
		assertThat(isInducedSubgraph(h, mcis, getMatches("h.mcis.induced")), is(true));
	}

	@Test
	public void testMCESIsSubgraph()
	{
		assertThat(isSubgraph(g, mces, getMatches("g.mces")), is(true));
		assertThat(isSubgraph(h, mces, getMatches("h.mces")), is(true));
	}

	@Test
	public void testMinCommonInducedSupergraph()
	{
		Set<BidiMap<UnlabeledNode,UnlabeledNode>> mappings = GraphMatcher.findMCIS(g, h, MAX_MATCHES);
		assertThat(mappings.isEmpty(), is(false));
		Set<BidiMap<UnlabeledNode,UnlabeledNode>> bestMappings = getBestMCSMappings(mappings, h, g, -1);
		UndirectedGraph<Object,Object> mcsuper = getBestSupergraph(bestMappings, h, g);
		assertThat(isMCIS(mcsuper, g, getMatches("mincisup.g.induced")), is(true));
		assertThat(isMCIS(mcsuper, h, getMatches("mincisup.h.induced")), is(true));
		assertThat(isInducedSubgraph(mcsuper, mcis, getMatches("mincisup.mcis.induced")), is(true));
	}

	@Test
	public void testMinCommonSupergraph()
	{
		Set<BidiMap<UnlabeledNode,UnlabeledNode>> mappings = GraphMatcher.findMCES(g, h, MAX_MATCHES);
		assertThat(mappings.isEmpty(), is(false));
		Set<BidiMap<UnlabeledNode,UnlabeledNode>> bestMappings = getBestMCSMappings(mappings, h, g, -1);
		UndirectedGraph<Object,Object> mcsuper = getBestSupergraph(bestMappings, h, g);
		assertThat(isMCES(mcsuper, g, getMatches("mincesup.g")), is(true));
		assertThat(isMCES(mcsuper, h, getMatches("mincesup.h")), is(true));
		assertThat(isSubgraph(mcsuper, mces, getMatches("mincesup.mces")), is(true));
	}

	private static UndirectedGraph<Object,Object> getBestSupergraph(Set<BidiMap<UnlabeledNode,UnlabeledNode>> mappings, UndirectedGraph<UnlabeledNode,UnlabeledEdge> h, UndirectedGraph<UnlabeledNode,UnlabeledEdge> g)
	{
		Set<BidiMap<UnlabeledNode,UnlabeledNode>> bestMappings = GraphMatcher.getBestSupergraphMappings(mappings, h, g);
		BidiMap<UnlabeledNode,UnlabeledNode> mapping = bestMappings.iterator().next();

		Map<UnlabeledNode,Object> vertexConversion = new HashMap<UnlabeledNode,Object>(g.getVertexCount());
		UndirectedGraph<Object,Object> gObj = new UndirectedSparseGraph<Object,Object>();
		for(UnlabeledNode v : g.getVertices())
		{
			Object o = new Object();
			gObj.addVertex(o);
			vertexConversion.put(v, o);
		}
		for(UnlabeledEdge e : g.getEdges())
		{
			Pair<UnlabeledNode> endpoints = g.getEndpoints(e);
			gObj.addEdge(new Object(), vertexConversion.get(endpoints.getFirst()), vertexConversion.get(endpoints.getSecond()));
		}
		BidiMap<UnlabeledNode,Object> mappingObj = new DualHashBidiMap<UnlabeledNode,Object>();
		for(Map.Entry<UnlabeledNode,UnlabeledNode> entry : mapping.entrySet())
		{
			mappingObj.put(entry.getKey(), vertexConversion.get(entry.getValue()));
		}

		UndirectedGraph<Object,Object> mcsuper = (UndirectedGraph<Object,Object>) GraphMatcher.createMinSupergraph(mappingObj, h, gObj, new ObjectFactory(), new ObjectFactory());
		return mcsuper;
	}

	@Test
	public void testAutomorphisms()
	{
		testAutomorphisms(g, getMatches("g.g"));
		testAutomorphisms(h, getMatches("h.h"));
		testAutomorphisms(mcis, getMatches("mcis.mcis"));
		testAutomorphisms(mces, getMatches("mces.mces"));
	}

	private static UndirectedGraph<UnlabeledNode,UnlabeledEdge> loadGraph(String filename) throws Exception
	{
		DataInputStream in = new DataInputStream(new FileInputStream(filename));
		try
		{
			DirectedGraph<UnlabeledNode,UnlabeledEdge> digraph = GraphDB.readUnlabeledGraph(in);
			UndirectedGraph<UnlabeledNode,UnlabeledEdge> graph = new UndirectedSparseGraph<UnlabeledNode,UnlabeledEdge>();
			for(UnlabeledNode v : digraph.getVertices())
			{
				graph.addVertex(v);
			}
			for(UnlabeledEdge e : digraph.getEdges())
			{
				Pair<UnlabeledNode> endpoints = digraph.getEndpoints(e);
				graph.addEdge(e, endpoints);
			}
			return GraphMatcher.sortByDegreeFrequency(graph);
		}
		finally
		{
			in.close();
		}
	}

	private static <V,E,U,F> UndirectedGraph<U,F> findMCIS(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int matches)
	{
		Set<BidiMap<U,V>> mappings = GraphMatcher.findMCIS(g, pattern, MAX_MATCHES);
		Set<BidiMap<U,V>> bestMappings = getBestMCSMappings(mappings, pattern, g, matches);
		BidiMap<U,V> mapping = bestMappings.iterator().next();
		return (UndirectedGraph<U, F>) GraphMatcher.createMappedGraph(mapping, pattern, g);
	}

	private static <V,E,U,F> UndirectedGraph<U,F> findMCES(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int matches)
	{
		Set<BidiMap<U,V>> mappings = GraphMatcher.findMCES(g, pattern, MAX_MATCHES);
		Set<BidiMap<U,V>> bestMappings = getBestMCSMappings(mappings, pattern, g, matches);
		BidiMap<U,V> mapping = bestMappings.iterator().next();
		return (UndirectedGraph<U, F>) GraphMatcher.createMappedGraph(mapping, pattern, g);
	}

	private static <U,V> Set<BidiMap<U,V>> getBestMCSMappings(Set<BidiMap<U,V>> mappings, UndirectedGraph<U,?> h, UndirectedGraph<V,?> g, int matches)
	{
		Set<BidiMap<U,V>> bestMappings = GraphMatcher.getMostEdgesMappings(mappings, h, g);
		assertMatches(bestMappings, matches);
		return bestMappings;
	}

	private static <V,E,U,F> boolean isGraph(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern)
	{
		assertThat("The number of vertices is not equal", pattern.getVertexCount(), is(g.getVertexCount()));
		assertThat("The number of edges is not equal", pattern.getEdgeCount(), is(g.getEdgeCount()));
		FirstNVisitor<U,V> visitor = new FirstNVisitor<U,V>(1);
		UndirectedGraphMatcherState<V,E,U,F> state = new UndirectedGraphMatcherState<V,E,U,F>(g, pattern);
		return GraphMatcher.match(state, visitor);
	}

	private static <V,E,U,F> boolean isGraph(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int matches)
	{
		Set<BidiMap<U,V>> mappings = GraphMatcher.matchGraph(g, pattern, MAX_MATCHES);
        assertMatches(mappings, matches);
		return true;
	}
	private static <V,E,U,F> boolean isInducedSubgraph(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int matches)
	{
		Set<BidiMap<U,V>> mappings = GraphMatcher.matchInducedSubgraph(g, pattern, MAX_MATCHES);
		Set<BidiMap<U,V>> bestMappings = GraphMatcher.getMostEdgesMappings(mappings, pattern, g);
        assertMatches(bestMappings, matches);
		BidiMap<U,V> mapping = bestMappings.iterator().next();
		UndirectedGraph<U,F> subgraph = (UndirectedGraph<U, F>) GraphMatcher.createMappedGraph(mapping, pattern, g);
		return isGraph(pattern, subgraph);
	}
	private static <V,E,U,F> boolean isSubgraph(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int matches)
	{
		Set<BidiMap<U,V>> mappings = GraphMatcher.matchSubgraph(g, pattern, MAX_MATCHES);
		Set<BidiMap<U,V>> bestMappings = GraphMatcher.getMostEdgesMappings(mappings, pattern, g);
        assertMatches(bestMappings, matches);
		BidiMap<U,V> mapping = bestMappings.iterator().next();
		UndirectedGraph<U,F> subgraph = (UndirectedGraph<U, F>) GraphMatcher.createMappedGraph(mapping, pattern, g);
		return isGraph(pattern, subgraph);
	}
	private static <V,E,U,F> boolean isMCIS(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int matches)
	{
		return isGraph(pattern, findMCIS(g, pattern, matches));
	}
	private static <V,E,U,F> boolean isMCES(UndirectedGraph<V,E> g, UndirectedGraph<U,F> pattern, int matches)
	{
		return isGraph(pattern, findMCES(g, pattern, matches));
	}

	private static <V,E,U,F> void testAutomorphisms(UndirectedGraph<V,E> g, int matches)
	{
		assertThat(isGraph(g, g, matches), is(true));
		assertThat(isInducedSubgraph(g, g, matches), is(true));
		assertThat(isSubgraph(g, g, matches), is(true));
		assertThat(isMCIS(g, g, matches), is(true));
		assertThat(isMCES(g, g, matches), is(true));
	}

	private static <U,V> void assertMatches(Set<BidiMap<U,V>> mappings, int matches)
	{
        if(matches != -1)
        {
            assertThat("The number of matches is not equal", mappings.size(), is(matches));
        }
        else
        {
            assertThat("The number of matches is not equal", mappings.isEmpty(), is(false));
        }
	}



	static class ObjectFactory implements Factory<Object>
	{
		@Override
		public Object create()
		{
			return new Object();
		}
	}
}
