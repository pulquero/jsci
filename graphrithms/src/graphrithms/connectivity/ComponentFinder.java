/**
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
package graphrithms.connectivity;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections15.ArrayStack;
import org.apache.commons.collections15.Buffer;

import edu.uci.ics.jung.graph.Graph;
import graphrithms.util.PairIterable;
import graphrithms.util.PairIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections15.KeyValue;
import org.apache.commons.collections15.keyvalue.DefaultKeyValue;

/**
 * A collection of component finding methods.
 */
public final class ComponentFinder
{
    private static final int COMPONENTS_CAPACITY = 127;
    private static final int COMPONENT_CAPACITY = 127;

    private ComponentFinder()
    {
    }

    /**
     * Finds weakly connected components using disjoint sets.
     */
    public static <V> DisjointSets<V> findWeaklyConnectedComponents(PairIterable<V, V> edges)
    {
        int capacity;
        if (edges instanceof Collection<?>)
        {
            capacity = ((Collection<?>) edges).size();
        }
        else
        {
            capacity = COMPONENT_CAPACITY;
        }
        DisjointSets<V> sets = new DisjointSets<V>(capacity);
        for (PairIterator<V, V> iter = edges.iterator(); iter.hasNext();)
        {
            V v1 = iter.nextFirst();
            V rep1 = sets.find(v1);
            if (rep1 == null)
            {
                rep1 = sets.makeSet(v1);
            }

            V v2 = iter.getSecond();
            V rep2 = sets.find(v2);
            if (rep2 == null)
            {
                rep2 = sets.makeSet(v2);
            }

            sets.union(rep1, rep2);
        }
        return sets;
    }

    /**
     * Finds weakly connected components using DFS.
     */
    public static <V> Collection<Collection<V>> findWeaklyConnectedComponents(Graph<V, ?> graph)
    {
        FirstNVisitor<V> visitor = new FirstNVisitor<V>();
        findWeaklyConnectedComponents(graph, visitor);
        return visitor.getComponents();
    }

    /**
     * Finds weakly connected components using DFS.
     */
    public static <V> void findWeaklyConnectedComponents(Graph<V, ?> graph, Visitor<V> visitor)
    {
        Set<V> unvisited = new LinkedHashSet<V>(graph.getVertices());
        while (!unvisited.isEmpty())
        {
            visitor.startComponent();

            Iterator<V> iter = unvisited.iterator();
            final V root = iter.next();
            iter.remove();

            visitor.visit(root);

            Buffer<V> stack = new ArrayStack<V>();
            stack.add(root);
            do
            {
                V v = stack.remove();
                for (V nv : graph.getNeighbors(v))
                {
                    if (unvisited.remove(nv))
                    {
                        visitor.visit(nv);
                        stack.add(nv);
                    }
                }
            }
            while (!stack.isEmpty());

            if (visitor.endComponent())
            {
                break;
            }
        }
    }

    /**
     * Returns a set of disjoint sets as a list of elements with their set representatives.
     */
    public static <V> List<DefaultKeyValue<V, V>> getRepresentatives(Collection<Collection<V>> disjointSets)
    {
        ArrayList<DefaultKeyValue<V, V>> partitions = new ArrayList<DefaultKeyValue<V, V>>(disjointSets.size());
        for (Collection<V> set : disjointSets)
        {
            if (!set.isEmpty())
            {
                partitions.ensureCapacity(partitions.size() + set.size());
                Iterator<V> iter = set.iterator();
                final V rep = iter.next();
                partitions.add(new DefaultKeyValue<V, V>(rep, rep));
                while (iter.hasNext())
                {
                    partitions.add(new DefaultKeyValue<V, V>(iter.next(), rep));
                }
            }
        }
        return partitions;
    }

    /**
     * Returns a list of elements with their set representatives as a set of disjoint sets.
     * @param representatives a collection of unique keys.
     */
    public static <V> Map<V, Collection<V>> getSets(Collection<DefaultKeyValue<V, V>> representatives)
    {
        Map<V, Collection<V>> sets = new LinkedHashMap<V, Collection<V>>(COMPONENTS_CAPACITY);
        for (DefaultKeyValue<V, V> kv : representatives)
        {
            V rep = kv.getValue();
            Collection<V> set = sets.get(rep);
            if (set == null)
            {
                set = new ArrayList<V>(COMPONENT_CAPACITY);
                sets.put(rep, set);
            }
            set.add(kv.getKey());
        }
        return sets;
    }

    /**
     * Merges the disjoint sets of a list of elements with the disjoint sets of a list of edges.
     */
    public static <V extends Comparable<? super V>> void merge(List<DefaultKeyValue<V, V>> reps, PairIterable<V, V> edges)
    {
        DefaultKeyValue<V, V>[] repArray = (DefaultKeyValue<V, V>[]) reps.toArray(new DefaultKeyValue<?, ?>[reps.size()]);
        int edgeCapacity;
        if (edges instanceof Collection<?>)
        {
            edgeCapacity = ((Collection<?>) edges).size();
        }
        else
        {
            edgeCapacity = COMPONENTS_CAPACITY * COMPONENT_CAPACITY;
        }
        V[][] edgeArray = (V[][]) new Comparable<?>[edgeCapacity][];
        int i = 0;
        for (PairIterator<V, V> iter = edges.iterator(); iter.hasNext();)
        {
            edgeArray[i++] = (V[]) new Comparable<?>[] {iter.nextFirst(), iter.getSecond()};
        }

        Arrays.sort(repArray, (Comparator<KeyValue<V, V>>) KEY_COMPARATOR);
        updateEdges(repArray, edgeArray, 0);
        updateEdges(repArray, edgeArray, 1);

        DisjointSets<V> edgeSets = new DisjointSets<V>(edgeArray.length);
        for (V[] edge : edgeArray)
        {
            V rep1 = edgeSets.find(edge[0]);
            if (rep1 == null)
            {
                rep1 = edgeSets.makeSet(edge[0]);
            }

            V rep2 = edgeSets.find(edge[1]);
            if (rep2 == null)
            {
                rep2 = edgeSets.makeSet(edge[1]);
            }

            edgeSets.union(rep1, rep2);
        }
        edgeArray = null;

        DefaultKeyValue<V, V>[] edgeRepArray = (DefaultKeyValue<V, V>[]) new DefaultKeyValue<?, ?>[edgeSets.size()];
        i = 0;
        for (PairIterator<V, V> iter = edgeSets.iterator(); iter.hasNext();)
        {
            edgeRepArray[i++] = new DefaultKeyValue<V, V>(iter.nextFirst(), iter.getSecond());
        }

        updateRepresentations(repArray, edgeRepArray);

        // sort reps
        Arrays.sort(repArray, (Comparator<KeyValue<V, V>>) KEY_COMPARATOR);
        for (i = 0; i < repArray.length; i++)
        {
            reps.set(i, repArray[i]);
        }
        repArray = null;
        union(reps, edgeRepArray);
    }

    /**
     * Sets edge.[i] = rep.value for all edge.[i] == rep.key.
     */
    private static <V extends Comparable<? super V>> void updateEdges(DefaultKeyValue<V, V>[] reps, V[][] edges, int endpointIndex)
    {
        Arrays.sort(edges, new ArrayComponentComparator<V>(endpointIndex));
        if ((reps.length > 0) && (edges.length > 0))
        {
            int pos_r = 0;
            int pos_e = 0;
            DefaultKeyValue<V, V> rep = reps[pos_r++];
            V[] edge = edges[pos_e++];
            do
            {
                int diff = rep.getKey().compareTo(edge[endpointIndex]);
                // skip trivials (rep.key==rep.value)
                while ((diff < 0 || rep.getKey().equals(rep.getValue())) && (pos_r < reps.length))
                {
                    rep = reps[pos_r++];
                    diff = rep.getKey().compareTo(edge[endpointIndex]);
                }
                while ((diff > 0) && (pos_e < edges.length))
                {
                    edge = edges[pos_e++];
                    diff = rep.getKey().compareTo(edge[endpointIndex]);
                }
                if (diff == 0)
                {
                    edge[endpointIndex] = rep.getValue();
                    edge = (pos_e < edges.length) ? edges[pos_e++] : null;
                }
                else
                {
                    rep = (pos_r < reps.length) ? reps[pos_r++] : null;
                }
            }
            while (rep != null && edge != null);
        }
    }

    /**
     * Sets rep1.value = rep2.value for all rep1.value == rep2.key.
     */
    private static <V extends Comparable<? super V>> void updateRepresentations(DefaultKeyValue<V, V>[] reps1, DefaultKeyValue<V, V>[] reps2)
    {
        Arrays.sort(reps1, (Comparator<KeyValue<V, V>>) VALUE_COMPARATOR);
        Arrays.sort(reps2, (Comparator<KeyValue<V, V>>) KEY_COMPARATOR);
        if ((reps1.length > 0) && (reps2.length > 0))
        {
            int pos1 = 0;
            int pos2 = 0;
            DefaultKeyValue<V, V> rep1 = reps1[pos1++];
            DefaultKeyValue<V, V> rep2 = reps2[pos2++];
            do
            {
                int diff = rep2.getKey().compareTo(rep1.getValue());
                // skip trivials (edge.key==edge.value)
                while ((diff < 0 || rep2.getKey().equals(rep2.getValue())) && (pos2 < reps2.length))
                {
                    rep2 = reps2[pos2++];
                    diff = rep2.getKey().compareTo(rep1.getValue());
                }
                while ((diff > 0) && (pos1 < reps1.length))
                {
                    rep1 = reps1[pos1++];
                    diff = rep2.getKey().compareTo(rep1.getValue());
                }
                if (diff == 0)
                {
                    rep1.setValue(rep2.getValue());
                    rep1 = (pos1 < reps1.length) ? reps1[pos1++] : null;
                }
                else
                {
                    rep2 = (pos2 < reps2.length) ? reps2[pos2++] : null;
                }
            }
            while (rep1 != null && rep2 != null);
        }
    }

    private static <V extends Comparable<? super V>> void union(List<DefaultKeyValue<V, V>> l1, DefaultKeyValue<V, V>[] arr2)
    {
        if (l1 instanceof ArrayList<?>)
        {
            ((ArrayList<?>) l1).ensureCapacity(l1.size() + arr2.length);
        }

        if (!l1.isEmpty())
        {
            if (arr2.length > 0)
            {
                DefaultKeyValue<V, V> kv1 = l1.get(l1.size() - 1);
                DefaultKeyValue<V, V> kv2 = arr2[0];
                if (kv2.getKey().compareTo(kv1.getKey()) <= 0)
                {
                    unionMerge(l1, arr2);
                }
                else
                {
                    Collections.addAll(l1, arr2);
                }
            }
        }
        else
        {
            Collections.addAll(l1, arr2);
        }
    }

    /**
     * Inserts non-matching elements from arr2 into l1, preserving order.
     */
    private static <V extends Comparable<? super V>> void unionMerge(List<DefaultKeyValue<V, V>> l1, DefaultKeyValue<V, V>[] arr2)
    {
        int pos1 = 0;
        int pos2 = 0;
        V prevKey1 = null;
        V key1 = l1.get(pos1).getKey();
        DefaultKeyValue<V, V> kv2 = arr2[pos2];
        do
        {
            int diff = kv2.getKey().compareTo(key1);
            if (diff > 0)
            {
                if (++pos1 < l1.size())
                {
                    prevKey1 = key1;
                    key1 = l1.get(pos1).getKey();
                }
                else
                {
                    prevKey1 = null;
                    key1 = null;
                }
            }
            else
            {
                // only insert if doesnt match any existing key
                if (diff < 0 && !kv2.getKey().equals(prevKey1))
                {
                    l1.add(pos1++, kv2);
                    prevKey1 = kv2.getKey();
                }
                kv2 = (++pos2 < arr2.length) ? arr2[pos2] : null;
            }
        }
        while (key1 != null && kv2 != null);
        for (; pos2 < arr2.length; pos2++)
        {
            l1.add(arr2[pos2]);
        }
    }

	private static final Comparator<? extends KeyValue<?, ?>> KEY_COMPARATOR = new KeyComparator();
    private static final Comparator<? extends KeyValue<?, ?>> VALUE_COMPARATOR = new ValueComparator();

    static final class KeyComparator<V extends Comparable<V>> implements Comparator<KeyValue<V, V>>
    {
		@Override
        public int compare(KeyValue<V, V> o1, KeyValue<V, V> o2)
        {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    static final class ValueComparator<V extends Comparable<V>> implements Comparator<KeyValue<V, V>>
    {
		@Override
        public int compare(KeyValue<V, V> o1, KeyValue<V, V> o2)
        {
            return o1.getValue().compareTo(o2.getValue());
        }
    }

    static final class ArrayComponentComparator<V extends Comparable<? super V>> implements Comparator<V[]>
    {
        final int index;

        ArrayComponentComparator(int index)
        {
            this.index = index;
        }

		@Override
        public int compare(V[] o1, V[] o2)
        {
            return o1[index].compareTo(o2[index]);
        }
    }
}
