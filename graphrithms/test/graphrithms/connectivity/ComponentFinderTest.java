package graphrithms.connectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import graphdb.GraphDB;
import graphdb.UnlabeledEdge;
import graphdb.UnlabeledNode;
import graphrithms.util.PairCollection;
import graphrithms.util.PairIterable;
import graphrithms.util.PairIterator;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.collections15.keyvalue.DefaultKeyValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class ComponentFinderTest
{
	private static final String TEST_GRAPH_FILE = "testdata/disconnected.bin";

	@Parameters
	public static List<Object[]> params()
	{
		return Arrays.asList(new Object[50][0]);
	}

	@Test
	public void testFindByDFS() throws Exception
	{
		Graph<UnlabeledNode,UnlabeledEdge> graph = loadGraph(TEST_GRAPH_FILE);
		Collection<Collection<UnlabeledNode>> sets = ComponentFinder.findWeaklyConnectedComponents(graph);
		assertComponents(graph, graph.getEdges(), sets);
	}

	@Test
	public void testFindByUnionFind() throws Exception
	{
		final Graph<UnlabeledNode,UnlabeledEdge> graph = loadGraph(TEST_GRAPH_FILE);
		final List<UnlabeledEdge> edges = getEdges(graph);
		PairIterable<UnlabeledNode,UnlabeledNode> endpoints = new PairIterable<UnlabeledNode,UnlabeledNode>()
		{
			@Override
			public PairIterator<UnlabeledNode,UnlabeledNode> iterator()
			{
				return new PairIterator<UnlabeledNode,UnlabeledNode>()
				{
					private final Iterator<UnlabeledEdge> edgeIter = edges.iterator();
					private UnlabeledEdge edge;

					@Override
					public boolean hasNext()
					{
						return edgeIter.hasNext();
					}
					@Override
					public UnlabeledNode nextFirst()
					{
						edge = edgeIter.next();
						return graph.getEndpoints(edge).getFirst();
					}
					@Override
					public UnlabeledNode getSecond()
					{
						return graph.getEndpoints(edge).getSecond();
					}
				};
			}
		};
		DisjointSets<UnlabeledNode> sets = ComponentFinder.findWeaklyConnectedComponents(endpoints);
		assertComponents(graph, edges, sets.getSets().values());
	}

	@Test
	public void testFindByMerge() throws Exception
	{
		Graph<UnlabeledNode,UnlabeledEdge> graph = loadGraph(TEST_GRAPH_FILE);
		final List<UnlabeledEdge> edges = getEdges(graph);
		List<DefaultKeyValue<UnlabeledNode,UnlabeledNode>> reps = new ArrayList<DefaultKeyValue<UnlabeledNode,UnlabeledNode>>(graph.getEdgeCount());
		for(UnlabeledEdge edge : edges)
		{
			Pair<UnlabeledNode> endpoints = graph.getEndpoints(edge);
			UnlabeledNode v1 = endpoints.getFirst();
			UnlabeledNode v2 = endpoints.getSecond();
			List<Pair<UnlabeledNode>> comp = new ArrayList<Pair<UnlabeledNode>>(2);
			comp.add(new Pair<UnlabeledNode>(v1, v1));
			comp.add(new Pair<UnlabeledNode>(v2, v1));
			ComponentFinder.merge(reps, new PairCollection<UnlabeledNode>(comp));
		}
		assertComponents(graph, edges, ComponentFinder.getSets(reps).values());
	}

	@Test
	public void testFindByDFSThenUnionFind() throws Exception
	{
		Graph<UnlabeledNode,UnlabeledEdge> graph = loadGraph(TEST_GRAPH_FILE);
		final List<UnlabeledEdge> edges = getEdges(graph);
		DisjointSets<UnlabeledNode> sets = new DisjointSets<UnlabeledNode>();
		Graph<UnlabeledNode,UnlabeledEdge> subgraph = new UndirectedSparseGraph<UnlabeledNode,UnlabeledEdge>();
		int i = 0;
		for(UnlabeledEdge edge : edges)
		{
			subgraph.addEdge(edge, graph.getEndpoints(edge));
			if((i++ % 5) == 4)
			{
				findByDFSAndMergeByUnionFind(sets, subgraph);
				subgraph = null;
				subgraph = new UndirectedSparseGraph<UnlabeledNode,UnlabeledEdge>();
			}
		}
		findByDFSAndMergeByUnionFind(sets, subgraph);
		assertComponents(graph, edges, sets.getSets().values());
	}

	private static void findByDFSAndMergeByUnionFind(DisjointSets<UnlabeledNode> sets, Graph<UnlabeledNode,UnlabeledEdge> subgraph)
	{
		Collection<Collection<UnlabeledNode>> partialSets = ComponentFinder.findWeaklyConnectedComponents(subgraph);
		DisjointSets<UnlabeledNode> partiald = new DisjointSets<UnlabeledNode>();
		for(Collection<UnlabeledNode> partialset : partialSets)
		{
			partiald.makeSet(partialset);
		}
		sets.merge(partiald);
	}

	@Test
	public void testFindByDFSThenMerge() throws Exception
	{
		Graph<UnlabeledNode,UnlabeledEdge> graph = loadGraph(TEST_GRAPH_FILE);
		final List<UnlabeledEdge> edges = getEdges(graph);
		List<DefaultKeyValue<UnlabeledNode,UnlabeledNode>> reps = new ArrayList<DefaultKeyValue<UnlabeledNode,UnlabeledNode>>();
		Graph<UnlabeledNode,UnlabeledEdge> subgraph = new UndirectedSparseGraph<UnlabeledNode,UnlabeledEdge>();
		int i = 0;
		for(UnlabeledEdge edge : edges)
		{
			subgraph.addEdge(edge, graph.getEndpoints(edge));
			if((i++ % 5) == 4)
			{
				findByDFSAndMergeBySort(reps, subgraph);
				subgraph = null;
				subgraph = new UndirectedSparseGraph<UnlabeledNode,UnlabeledEdge>();
			}
		}
		findByDFSAndMergeBySort(reps, subgraph);
		assertComponents(graph, edges, ComponentFinder.getSets(reps).values());
	}

	private static <V extends Comparable<? super V>> void findByDFSAndMergeBySort(List<DefaultKeyValue<V,V>> reps, Graph<V,?> subgraph)
	{
		Collection<Collection<V>> partialSets = ComponentFinder.findWeaklyConnectedComponents(subgraph);
		List<DefaultKeyValue<V,V>> partialReps = ComponentFinder.getRepresentatives(partialSets);
		List<Pair<V>> edges = new ArrayList<Pair<V>>(partialReps.size());
		for(DefaultKeyValue<V,V> entry : partialReps)
		{
			edges.add(new Pair<V>(entry.getKey(), entry.getValue()));
		}
		ComponentFinder.merge(reps, new PairCollection<V>(edges));
	}

	private static List<UnlabeledEdge> getEdges(Graph<UnlabeledNode,UnlabeledEdge> graph)
	{
		return getEdges_random(graph);
	}

	private static List<UnlabeledEdge> getEdges_random(Graph<UnlabeledNode,UnlabeledEdge> graph)
	{
		List<UnlabeledEdge> edges = new ArrayList<UnlabeledEdge>(graph.getEdges());
		Collections.shuffle(edges);
		return edges;
	}

	private static <V,E> void assertComponents(Graph<V,E> graph, Collection<E> edges, Collection<? extends Collection<?>> sets)
	{
		int[] setSizes = new int[sets.size()];
		int i = 0;
		for(Collection<?> set : sets)
		{
			setSizes[i++] = set.size();
		}
		Arrays.sort(setSizes);
		int[] expected = {5, 5, 8, 9};
		assertThat("\nEdge order:\n"+toString(graph, edges)+"\nComponents:\n"+sets.toString(), setSizes, is(expected));
	}

	private static <V,E> String toString(Graph<V,E> graph, Collection<E> edges)
	{
		List<Object> endpoints = new ArrayList<Object>();
		for(E edge : edges)
		{
			endpoints.add(graph.getEndpoints(edge));
		}
		return endpoints.toString();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMerge4_2dups()
	{
		List<DefaultKeyValue<String,String>> l1 = new ArrayList<DefaultKeyValue<String,String>>();
		Collections.addAll(l1,
				new DefaultKeyValue<String,String>("a", "a"),
				new DefaultKeyValue<String,String>("b", "b"),
				new DefaultKeyValue<String,String>("x", "x"),
				new DefaultKeyValue<String,String>("y", "x")
		);
		List<Pair<String>> l2 = Arrays.asList(
				new Pair<String>("a", "x"),
				new Pair<String>("b", "y"),
				new Pair<String>("x", "x"),
				new Pair<String>("y", "y")
		);
		ComponentFinder.merge(l1, new PairCollection<String>(l2));
		List<DefaultKeyValue<String,String>> expected = Arrays.asList(
				new DefaultKeyValue<String,String>("a", "x"),
				new DefaultKeyValue<String,String>("b", "x"),
				new DefaultKeyValue<String,String>("x", "x"),
				new DefaultKeyValue<String,String>("y", "x")
		);
		assertThat(l1, is(expected));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMerge4_4dups()
	{
		List<DefaultKeyValue<String,String>> l1 = new ArrayList<DefaultKeyValue<String,String>>();
		Collections.addAll(l1,
				new DefaultKeyValue<String,String>("1", "1"),
				new DefaultKeyValue<String,String>("2", "1"),
				new DefaultKeyValue<String,String>("3", "1"),
				new DefaultKeyValue<String,String>("4", "1")
		);
		List<Pair<String>> l2 = Arrays.asList(
				new Pair<String>("2", "2"),
				new Pair<String>("3", "3"),
				new Pair<String>("4", "4")
		);
		ComponentFinder.merge(l1, new PairCollection<String>(l2));
		List<DefaultKeyValue<String,String>> expected = Arrays.asList(
				new DefaultKeyValue<String,String>("1", "1"),
				new DefaultKeyValue<String,String>("2", "1"),
				new DefaultKeyValue<String,String>("3", "1"),
				new DefaultKeyValue<String,String>("4", "1")
		);
		assertThat(l1, is(expected));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMerge4_0dups()
	{
		List<DefaultKeyValue<String,String>> l1 = new ArrayList<DefaultKeyValue<String,String>>();
		Collections.addAll(l1,
				new DefaultKeyValue<String,String>("1", "1"),
				new DefaultKeyValue<String,String>("2", "1"),
				new DefaultKeyValue<String,String>("3", "1"),
				new DefaultKeyValue<String,String>("4", "1")
		);
		List<Pair<String>> l2 = Arrays.asList(
				new Pair<String>("1", "2"),
				new Pair<String>("2", "2"),
				new Pair<String>("3", "2"),
				new Pair<String>("4", "2")
		);
		ComponentFinder.merge(l1, new PairCollection<String>(l2));
		List<DefaultKeyValue<String,String>> expected = Arrays.asList(
				new DefaultKeyValue<String,String>("1", "1"),
				new DefaultKeyValue<String,String>("2", "1"),
				new DefaultKeyValue<String,String>("3", "1"),
				new DefaultKeyValue<String,String>("4", "1")
		);
		assertThat(l1, is(expected));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMerge5()
	{
		List<DefaultKeyValue<String,String>> l1 = new ArrayList<DefaultKeyValue<String,String>>();
		Collections.addAll(l1,
				new DefaultKeyValue<String,String>("rx", "rx"),
				new DefaultKeyValue<String,String>("ry", "ry"),
				new DefaultKeyValue<String,String>("x", "a"),
				new DefaultKeyValue<String,String>("y", "a"),
				new DefaultKeyValue<String,String>("a", "a")
		);
		List<Pair<String>> l2 = Arrays.asList(
				new Pair<String>("rx", "rx"),
				new Pair<String>("ry", "ry"),
				new Pair<String>("x", "rx"),
				new Pair<String>("y", "ry"),
				new Pair<String>("a", "a")
		);
		ComponentFinder.merge(l1, new PairCollection<String>(l2));
		Collection<Collection<String>> comps = ComponentFinder.getSets(l1).values();
		assertThat(comps.size(), is(1));
		assertThat(comps.iterator().next(), is((Collection<String>) Arrays.asList("a", "rx", "ry", "x", "y")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByMerge5()
	{
		List<Pair<String>> edges = Arrays.asList(
				new Pair<String>("x", "rx"),
				new Pair<String>("y", "ry"),
				new Pair<String>("x", "y"),
				new Pair<String>("a", "x"),
				new Pair<String>("a", "y"));
		List<DefaultKeyValue<String,String>> reps = new ArrayList<DefaultKeyValue<String,String>>(edges.size());
		for(Pair<String> edge : edges)
		{
			String v1 = edge.getFirst();
			String v2 = edge.getSecond();
			List<Pair<String>> comp = new ArrayList<Pair<String>>(2);
			comp.add(new Pair<String>(v1, v1));
			comp.add(new Pair<String>(v2, v1));
			ComponentFinder.merge(reps, new PairCollection<String>(comp));
		}
		Collection<Collection<String>> comps = ComponentFinder.getSets(reps).values();
		assertThat(comps.size(), is(1));
		assertThat(comps.iterator().next(), is((Collection<String>) Arrays.asList("a", "rx", "ry", "x", "y")));
	}

	private static Graph<UnlabeledNode,UnlabeledEdge> loadGraph(String file) throws IOException
	{
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try
		{
			return GraphDB.readUnlabeledGraph(in);
		}
		finally
		{
			in.close();
		}
	}
}
