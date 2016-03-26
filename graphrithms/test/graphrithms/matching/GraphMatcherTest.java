package graphrithms.matching;

import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.Set;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Factory;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class GraphMatcherTest<V,E>
{
	private static final int MAX_MATCHES = -1;

	private Factory<V> vertexFactory = (Factory<V>) new ObjectFactory();
	private Factory<E> edgeFactory = (Factory<E>) new ObjectFactory();

	@Test
	public void testUndirectedMCIS()
	{
		UndirectedGraph<V,E> graph1 = createUndirectedGraph();
		UndirectedGraph<V,E> graph2 = createUndirectedGraph();
		UndirectedGraph<V,E> mcs1 = findMCIS(graph2, graph1);
		UndirectedGraph<V,E> mcs2 = findMCIS(graph1, graph2);
		Set<BidiMap<V,V>> matches = GraphMatcher.matchGraph(mcs1, mcs2, 1);
		assertThat(!matches.isEmpty(), is(true));

		Set<BidiMap<V,V>> subgraphs21 = GraphMatcher.matchInducedSubgraph(graph1, mcs2, 1);
		assertThat(!subgraphs21.isEmpty(), is(true));
		Set<BidiMap<V,V>> subgraphs12 = GraphMatcher.matchInducedSubgraph(graph2, mcs1, 1);
		assertThat(!subgraphs12.isEmpty(), is(true));
		System.out.println("Undirected MCIS "+mcs1.getVertexCount()+" of "+graph1.getVertexCount()+" and "+graph2.getVertexCount());
	}

	@Test
	public void testUndirectedMCES()
	{
		UndirectedGraph<V,E> graph1 = createUndirectedGraph();
		UndirectedGraph<V,E> graph2 = createUndirectedGraph();
		UndirectedGraph<V,E> mcs1 = findMCES(graph2, graph1);
		UndirectedGraph<V,E> mcs2 = findMCES(graph1, graph2);
		Set<BidiMap<V,V>> matches = GraphMatcher.matchGraph(mcs1, mcs2, 1);
		assertThat(!matches.isEmpty(), is(true));

		Set<BidiMap<V,V>> subgraphs21 = GraphMatcher.matchSubgraph(graph1, mcs2, 1);
		assertThat(!subgraphs21.isEmpty(), is(true));
		Set<BidiMap<V,V>> subgraphs12 = GraphMatcher.matchSubgraph(graph2, mcs1, 1);
		assertThat(!subgraphs12.isEmpty(), is(true));
		System.out.println("Undirected MCES "+mcs1.getVertexCount()+" of "+graph1.getVertexCount()+" and "+graph2.getVertexCount());
	}

	private UndirectedGraph<V,E> createUndirectedGraph()
	{
		ErdosRenyiGenerator<V,E> gen = new ErdosRenyiGenerator<V,E>(UndirectedSparseGraph.<V,E>getFactory(), vertexFactory, edgeFactory, 5+(int) Math.floor(5*Math.random()), Math.random());
		return (UndirectedGraph<V,E>) gen.create();
	}

	private UndirectedGraph<V,E> findMCIS(UndirectedGraph<V,E> g, UndirectedGraph<V,E> pattern)
	{
		Set<BidiMap<V,V>> mappings = GraphMatcher.findMCIS(g, pattern, MAX_MATCHES);
		Set<BidiMap<V,V>> bestMappings = GraphMatcher.getMostEdgesMappings(mappings, pattern, g);
		BidiMap<V,V> mapping = bestMappings.iterator().next();
		return (UndirectedGraph<V,E>) GraphMatcher.createMappedGraph(mapping, pattern, g);
	}

	private UndirectedGraph<V,E> findMCES(UndirectedGraph<V,E> g, UndirectedGraph<V,E> pattern)
	{
		Set<BidiMap<V,V>> mappings = GraphMatcher.findMCES(g, pattern, MAX_MATCHES);
		Set<BidiMap<V,V>> bestMappings = GraphMatcher.getMostEdgesMappings(mappings, pattern, g);
		BidiMap<V,V> mapping = bestMappings.iterator().next();
		return (UndirectedGraph<V,E>) GraphMatcher.createMappedGraph(mapping, pattern, g);
	}

	@Test
	public void testDirectedMCIS()
	{
		DirectedGraph<V,E> graph1 = createDirectedGraph();
		DirectedGraph<V,E> graph2 = createDirectedGraph();
		DirectedGraph<V,E> mcs1 = findMCIS(graph2, graph1);
		DirectedGraph<V,E> mcs2 = findMCIS(graph1, graph2);
		Set<BidiMap<V,V>> matches = GraphMatcher.matchGraph(mcs1, mcs2, 1);
		assertThat(!matches.isEmpty(), is(true));

		Set<BidiMap<V,V>> subgraphs21 = GraphMatcher.matchInducedSubgraph(graph1, mcs2, 1);
		assertThat(!subgraphs21.isEmpty(), is(true));
		Set<BidiMap<V,V>> subgraphs12 = GraphMatcher.matchInducedSubgraph(graph2, mcs1, 1);
		assertThat(!subgraphs12.isEmpty(), is(true));
		System.out.println("Directed MCIS "+mcs1.getVertexCount()+" of "+graph1.getVertexCount()+" and "+graph2.getVertexCount());
	}

	@Test
	public void testDirectedMCES()
	{
		DirectedGraph<V,E> graph1 = createDirectedGraph();
		DirectedGraph<V,E> graph2 = createDirectedGraph();
		DirectedGraph<V,E> mcs1 = findMCES(graph2, graph1);
		DirectedGraph<V,E> mcs2 = findMCES(graph1, graph2);
		Set<BidiMap<V,V>> matches = GraphMatcher.matchGraph(mcs1, mcs2, 1);
		assertThat(!matches.isEmpty(), is(true));

		Set<BidiMap<V,V>> subgraphs21 = GraphMatcher.matchSubgraph(graph1, mcs2, 1);
		assertThat(!subgraphs21.isEmpty(), is(true));
		Set<BidiMap<V,V>> subgraphs12 = GraphMatcher.matchSubgraph(graph2, mcs1, 1);
		assertThat(!subgraphs12.isEmpty(), is(true));
		System.out.println("Directed MCES "+mcs1.getVertexCount()+" of "+graph1.getVertexCount()+" and "+graph2.getVertexCount());
	}

	private DirectedGraph<V,E> createDirectedGraph()
	{
		int numVertices = 5+(int) Math.floor(5*Math.random());
		EppsteinPowerLawGenerator<V,E> gen = new EppsteinPowerLawGenerator<V,E>((Factory) DirectedSparseGraph.<V,E>getFactory(), vertexFactory, edgeFactory, numVertices, 2*numVertices, 100);
		return (DirectedGraph<V,E>) gen.create();
	}

	private DirectedGraph<V,E> findMCIS(DirectedGraph<V,E> g, DirectedGraph<V,E> pattern)
	{
		Set<BidiMap<V,V>> mappings = GraphMatcher.findMCIS(g, pattern, MAX_MATCHES);
		Set<BidiMap<V,V>> bestMappings = GraphMatcher.getMostEdgesMappings(mappings, pattern, g);
		BidiMap<V,V> mapping = bestMappings.iterator().next();
		return (DirectedGraph<V,E>) GraphMatcher.createMappedGraph(mapping, pattern, g);
	}

	private DirectedGraph<V,E> findMCES(DirectedGraph<V,E> g, DirectedGraph<V,E> pattern)
	{
		Set<BidiMap<V,V>> mappings = GraphMatcher.findMCES(g, pattern, MAX_MATCHES);
		Set<BidiMap<V,V>> bestMappings = GraphMatcher.getMostEdgesMappings(mappings, pattern, g);
		BidiMap<V,V> mapping = bestMappings.iterator().next();
		return (DirectedGraph<V,E>) GraphMatcher.createMappedGraph(mapping, pattern, g);
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
