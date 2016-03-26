package graphrithms.matching;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.DirectedGraph;

import graphrithms.matching.GraphMatcher;
import graphrithms.matching.Matcher;
import graphrithms.matching.mcs.DirectedMCISMatcherState;
import graphrithms.matching.mcs.FirstNMCSVisitor;
import graphdb.GraphDB;
import graphdb.AbstractEdge;
import graphdb.AbstractNode;
import graphdb.LabeledEdge;
import graphdb.LabeledNode;
import graphdb.UnlabeledEdge;
import graphdb.UnlabeledNode;

import org.apache.commons.collections15.BidiMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class GraphDBTest
{
    private static final int MAX_MATCHES = -1;

    @Parameters
    public static Collection<Object[]> getTests()
    {
        return Arrays.<Object[]>asList(
                new Object[] {GraphDB.ISO_MATCH_TYPE, GraphDB.RAND_TYPE_GROUP, GraphDB.R001_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20},
                new Object[] {GraphDB.ISO_MATCH_TYPE, GraphDB.RAND_TYPE_GROUP, GraphDB.R01_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20},
                new Object[] {GraphDB.ISO_MATCH_TYPE, GraphDB.M3D_TYPE_GROUP, GraphDB.M3D_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 27},
                new Object[] {GraphDB.ISO_MATCH_TYPE, GraphDB.M4D_TYPE_GROUP, GraphDB.M4DR4_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 16},
                new Object[] {GraphDB.ISO_MATCH_TYPE, GraphDB.BVG_TYPE_GROUP, GraphDB.B03_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20},
                new Object[] {GraphDB.ISO_MATCH_TYPE, GraphDB.BVG_TYPE_GROUP, GraphDB.B06M_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20},

                new Object[] {GraphDB.SI2_MATCH_TYPE, GraphDB.RAND_TYPE_GROUP, GraphDB.R001_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20},
                new Object[] {GraphDB.SI2_MATCH_TYPE, GraphDB.RAND_TYPE_GROUP, GraphDB.R01_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20},
                new Object[] {GraphDB.SI6_MATCH_TYPE, GraphDB.RAND_TYPE_GROUP, GraphDB.R001_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20},
                new Object[] {GraphDB.SI6_MATCH_TYPE, GraphDB.RAND_TYPE_GROUP, GraphDB.R01_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 20}

/*
                new Object[] {GraphDB.MCS30_MATCH_TYPE, GraphDB.RAND_TYPE_GROUP, GraphDB.R02_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 10},
                new Object[] {GraphDB.MCS50_MATCH_TYPE, GraphDB.BVG_TYPE_GROUP, GraphDB.B03M_GRAPH_TYPE, GraphDB.SMALL_SIZE_GROUP, 10}
*/
        );
    }

    private final String matchType;
    private final String typeGroup;
    private final String graphType;
    private final char sizeGroup;
    private final int size;
    private final Map<String,?> gtr;

    public GraphDBTest(String matchType, String typeGroup, String graphType, char sizeGroup, int size) throws IOException
    {
        this.matchType = matchType;
        this.typeGroup = typeGroup;
        this.graphType = graphType;
        this.sizeGroup = sizeGroup;
        this.size = size;
        if(matchType.startsWith(GraphDB.MCS_MATCH_GROUP))
        {
            this.gtr = GraphDB.loadLabeledGroundTruth(matchType, graphType);
        }
        else
        {
            this.gtr = GraphDB.loadUnlabeledGroundTruth(matchType, graphType);
        }
    }

    @Test
    public void testPairs() throws IOException
    {
        for(int pair = 0; pair < GraphDB.NUMBER_OF_PAIRS; pair++)
        {
            matchPair(pair);
        }
    }

    @SuppressWarnings("unchecked")
    private void matchPair(int pair) throws IOException
    {
        DirectedGraph<? extends AbstractNode,? extends AbstractEdge> graphA = GraphDB.loadGraph(matchType, typeGroup, graphType, sizeGroup, size, GraphDB.GRAPH_A, pair);
        DirectedGraph<? extends AbstractNode,? extends AbstractEdge> graphB = GraphDB.loadGraph(matchType, typeGroup, graphType, sizeGroup, size, GraphDB.GRAPH_B, pair);
        String graphName = GraphDB.getGraphName(matchType, typeGroup, graphType, sizeGroup, size, GraphDB.GRAPH_A, pair);
        if(GraphDB.ISO_MATCH_GROUP.equals(matchType))
        {
            DirectedGraph<UnlabeledNode,UnlabeledEdge> unlabeledGraphA = (DirectedGraph<UnlabeledNode,UnlabeledEdge>) graphA;
            DirectedGraph<UnlabeledNode,UnlabeledEdge> unlabeledGraphB = (DirectedGraph<UnlabeledNode,UnlabeledEdge>) graphB;
            matchGraphPair(graphName, unlabeledGraphA, unlabeledGraphB);
        }
        else if(matchType.startsWith(GraphDB.SI_MATCH_GROUP))
        {
            DirectedGraph<UnlabeledNode,UnlabeledEdge> unlabeledGraphA = (DirectedGraph<UnlabeledNode,UnlabeledEdge>) graphA;
            DirectedGraph<UnlabeledNode,UnlabeledEdge> unlabeledGraphB = (DirectedGraph<UnlabeledNode,UnlabeledEdge>) graphB;
            matchSubgraphPair(graphName, unlabeledGraphA, unlabeledGraphB);
        }
        else if(matchType.startsWith(GraphDB.MCS_MATCH_GROUP))
        {
            DirectedGraph<LabeledNode,LabeledEdge> labeledGraphA = (DirectedGraph<LabeledNode,LabeledEdge>) graphA;
            DirectedGraph<LabeledNode,LabeledEdge> labeledGraphB = (DirectedGraph<LabeledNode,LabeledEdge>) graphB;
            matchMCSPair(graphName, labeledGraphA, labeledGraphB);
        }
        else
        {
            throw new AssertionError("Unsupported match type: "+matchType);
        }
    }

    private void matchGraphPair(String graphName, DirectedGraph<UnlabeledNode,UnlabeledEdge> graphA, DirectedGraph<UnlabeledNode,UnlabeledEdge> graphB)
    {
        int expectedMatches = (Integer) gtr.get(graphName);
        int numMatches = GraphMatcher.matchGraph(graphB, graphA, MAX_MATCHES).size();
        assertThat(graphName, numMatches, is(expectedMatches));
    }

    private void matchSubgraphPair(String graphName, DirectedGraph<UnlabeledNode,UnlabeledEdge> graphA, DirectedGraph<UnlabeledNode,UnlabeledEdge> graphB)
    {
        int expectedMatches = (Integer) gtr.get(graphName);
        int numMatches = GraphMatcher.matchInducedSubgraph(graphB, graphA, MAX_MATCHES).size();
        assertThat(graphName, numMatches, is(expectedMatches));
    }

    private void matchMCSPair(String graphName, DirectedGraph<LabeledNode,LabeledEdge> graphA, DirectedGraph<LabeledNode,LabeledEdge> graphB)
    {
        int[] expectedMatches = (int[]) gtr.get(graphName);
        for(int i=0; i<expectedMatches.length; i++)
        {
            float fraction = GraphDB.ATTRIBUTE_MATCHING_FRACTIONS[i];
            int numAttributeBits = getNumberOfAttributeBitsToMatch(size, fraction);
            int numMatches = findMCIS(graphB, graphA, numAttributeBits).size();
            assertThat(graphName+" ["+fraction+"]", numMatches, is(equalTo(expectedMatches[i])));
        }
    }

    private static int getNumberOfAttributeBitsToMatch(int size, float fraction)
    {
        return (int) Math.ceil(Math.log(size*fraction)/Math.log(2.0));
    }

    private static Set<BidiMap<LabeledNode,LabeledNode>> findMCIS(DirectedGraph<LabeledNode,LabeledEdge> g, DirectedGraph<LabeledNode,LabeledEdge> pattern, int numBits)
    {
        return findMCIS(g, pattern, new NodeAttributeMatcher(numBits), new EdgeAttributeMatcher(numBits));
    }

    private static <V,E,U,F> Set<BidiMap<U,V>> findMCIS(DirectedGraph<V,E> g, DirectedGraph<U,F> pattern, Matcher<U,V> vertexMatcher, Matcher<F,E> edgeMatcher)
    {
        FirstNMCSVisitor<U,V> visitor = new FirstNMCSVisitor<U,V>();
        DirectedMCISMatcherState<V,E,U,F> state = new DirectedMCISMatcherState<V,E,U,F>(g, pattern, vertexMatcher, edgeMatcher);
        GraphMatcher.matchMCS(state, visitor);
        return visitor.getMappings();
    }



    private static final class NodeAttributeMatcher implements Matcher<LabeledNode,LabeledNode>
    {
        private final int numBits;

        NodeAttributeMatcher(int bits)
        {
            this.numBits = bits;
        }

        @Override
        public boolean match(LabeledNode s, LabeledNode t)
        {
            int sv = GraphDB.getValue(s.getAttribute(), numBits);
            int tv = GraphDB.getValue(t.getAttribute(), numBits);
            return (sv == tv);
        }
    }



    private static final class EdgeAttributeMatcher implements Matcher<LabeledEdge,LabeledEdge>
    {
        private final int numBits;

        EdgeAttributeMatcher(int bits)
        {
            this.numBits = bits;
        }

        @Override
        public boolean match(LabeledEdge s, LabeledEdge t)
        {
            int sv = GraphDB.getValue(s.getAttribute(), numBits);
            int tv = GraphDB.getValue(t.getAttribute(), numBits);
            return (sv == tv);
        }
    }
}
