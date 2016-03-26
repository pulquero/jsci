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

import java.util.Iterator;
import java.util.NoSuchElementException;

import graphrithms.util.PairIterator;

public final class LeftScalarProductIterable<U,V> implements NestedIterable<U,V>
{
	private final U u;
	private final Iterable<V> iterable;

	public LeftScalarProductIterable(U u, Iterable<V> iterable)
	{
		this.u = u;
		this.iterable = iterable;
	}

	public PairIterator<U,V> iterator()
	{
		return new LeftScalarProductPairIterator<U,V>(u, iterable);
	}

	@Override
	public NestedIterator<U,V> nestedIterator()
	{
		return new LeftScalarProductNestedIterator<U,V>(u, iterable);
	}



	static class LeftScalarProductPairIterator<U,V> implements PairIterator<U,V>
	{
		private final U u;
		private final Iterator<V> iterator;
		private V value2;
		private boolean nextCalled;

		LeftScalarProductPairIterator(U u, Iterable<V> col)
		{
			this(u, col.iterator());
		}

		LeftScalarProductPairIterator(U u, Iterator<V> iter)
		{
			this.u = u;
			this.iterator = iter;
		}

		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		@Override
		public U nextFirst()
		{
			value2 = iterator.next();
			nextCalled = true;
			return u;
		}

		@Override
		public V getSecond()
		{
			if(!nextCalled)
			{
				throw new NoSuchElementException();
			}
			return value2;
		}
	}



	static class LeftScalarProductNestedIterator<U,V> implements NestedIterator<U,V>
	{
		private final U u;
		private final Iterable<V> iterable;
		private boolean nextCalled;

		LeftScalarProductNestedIterator(U u, Iterable<V> col)
		{
			this.u = u;
			this.iterable = col;
		}

		@Override
		public boolean hasNext()
		{
			return !nextCalled;
		}

		@Override
		public U next()
		{
			if(nextCalled)
			{
				throw new NoSuchElementException();
			}
			nextCalled = true;
			return u;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<V> iterator()
		{
			return iterable.iterator();
		}
	}
}
