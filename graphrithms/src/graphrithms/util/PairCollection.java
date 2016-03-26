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
package graphrithms.util;

import edu.uci.ics.jung.graph.util.Pair;
import java.util.Collection;
import java.util.Iterator;

public class PairCollection<V> implements Collection<Pair<V>>, PairIterable<V,V>
{
        private final Collection<Pair<V>> collection;

        public PairCollection(Collection<Pair<V>> c)
        {
            this.collection = c;
        }

        @Override
        public boolean equals(Object o)
        {
            return collection.equals(o);
        }

        @Override
        public int hashCode()
        {
            return collection.hashCode();
        }

        @Override
        public String toString()
        {
            return collection.toString();
        }

        public Iterator<V> iterator()
        {
            return new Iterator<V>(collection.iterator());
        }

        public int size()
        {
            return collection.size();
        }

        public boolean isEmpty()
        {
            return collection.isEmpty();
        }

        public boolean contains(Object o)
        {
            return collection.contains(o);
        }

        public Object[] toArray()
        {
            return collection.toArray();
        }

        public <T> T[] toArray(T[] a)
        {
            return collection.toArray(a);
        }

        public boolean add(Pair<V> e)
        {
            return collection.add(e);
        }

        public boolean remove(Object o)
        {
            return collection.remove(o);
        }

        public boolean containsAll(Collection<?> c)
        {
            return collection.containsAll(c);
        }

        public boolean addAll(Collection<? extends Pair<V>> c)
        {
            return collection.addAll(c);
        }

        public boolean removeAll(Collection<?> c)
        {
            return collection.removeAll(c);
        }

        public boolean retainAll(Collection<?> c)
        {
            return collection.retainAll(c);
        }

        public void clear()
        {
            collection.clear();
        }



        public static final class Iterator<V> implements java.util.Iterator<Pair<V>>, PairIterator<V,V>
        {
                private final java.util.Iterator<Pair<V>> iterator;
                private Pair<V> current;

                public Iterator(java.util.Iterator<Pair<V>> iter)
                {
                    iterator = iter;
                }

                public Pair<V> next()
                {
                    current = iterator.next();
                    return current;
                }

                public V nextFirst()
                {
                    return next().getFirst();
                }

                public V getSecond()
                {
                    return current.getSecond();
                }

                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                public void remove()
                {
                    iterator.remove();
                    current = null;
                }
        }
}
