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
package graphrithms.matching.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import graphrithms.util.PairIterator;

public final class CrossProductIterable<U,V> implements NestedIterable<U,V>
{
	private final Iterable<U> iterable1;
	private final Collection<V> iterable2;

	public CrossProductIterable(Iterable<U> iterable1, Collection<V> iterable2)
	{
		this.iterable1 = iterable1;
		this.iterable2 = iterable2;
	}

	@Override
	public PairIterator<U,V> iterator()
	{
		return new CrossProductPairIterator<U,V>(iterable1, iterable2);
	}

	@Override
	public NestedIterator<U,V> nestedIterator()
	{
		return new CrossProductNestedIterator<U,V>(iterable1, iterable2);
	}



	static final class CrossProductPairIterator<U,V> implements PairIterator<U,V>
	{
		private final Iterator<U> iterator1;
		private final Collection<V> collection2;
		private Iterator<V> iterator2;
		private U value1;
		private V value2;

		CrossProductPairIterator(Iterable<U> iterable1, Collection<V> col2)
		{
			this(iterable1.iterator(), col2);
		}

		CrossProductPairIterator(Iterator<U> iter1, Collection<V> col2)
		{
			this.iterator1 = iter1;
			this.collection2 = col2;
		}

		@Override
		public boolean hasNext()
		{
			boolean available = (iterator2 != null && iterator2.hasNext());
			if(!available)
			{
				available = iterator1.hasNext() && !collection2.isEmpty();
			}
			return available;
		}

		@Override
		public U nextFirst()
		{
			if(iterator2 == null || !iterator2.hasNext())
			{
				value1 = iterator1.next();
				iterator2 = collection2.iterator();
			}
			value2 = iterator2.next();
			return value1;
		}

		@Override
		public V getSecond()
		{
			if(iterator2 == null)
			{
				throw new NoSuchElementException();
			}
			return value2;
		}
	}



	static final class CrossProductNestedIterator<U,V> implements NestedIterator<U,V>
	{
		private final Iterator<U> iterator1;
		private final Iterable<V> collection2;

		CrossProductNestedIterator(Iterable<U> iterable1, Iterable<V> col2)
		{
			this(iterable1.iterator(), col2);
		}

		CrossProductNestedIterator(Iterator<U> iter1, Iterable<V> col2)
		{
			this.iterator1 = iter1;
			this.collection2 = col2;
		}

		@Override
		public boolean hasNext()
		{
			return iterator1.hasNext();
		}

		@Override
		public U next()
		{
			return iterator1.next();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<V> iterator()
		{
			return collection2.iterator();
		}
	}
}
