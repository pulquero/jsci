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
import java.util.NoSuchElementException;

import graphrithms.util.PairIterable;
import graphrithms.util.PairIterator;
import java.util.Iterator;

public final class PairIterables
{
	private static final EmptyPairIterable EMPTY_PAIR_ITERABLE = new EmptyPairIterable();
	private static final EmptyPairIterator EMPTY_PAIR_ITERATOR = new EmptyPairIterator();
	private static final EmptyNestedIterator EMPTY_NESTED_ITERATOR = new EmptyNestedIterator();
	private static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();

	@SuppressWarnings("unchecked")
	public static <U,V> NestedIterable<U,V> empty()
	{
		return (NestedIterable<U,V>) EMPTY_PAIR_ITERABLE;
	}

	public static <U,V> PairIterable<U,V> sum(final PairIterable<U,V>... iterables)
	{
		return new SumPairIterable<U,V>(iterables);
	}

	public static <U,V> NestedIterable<U,V> sum(final NestedIterable<U,V>... iterables)
	{
		return new SumNestedIterable<U,V>(iterables);
	}

	public static <U,V> NestedIterable<U,V> leftScalarProduct(U u, Collection<V> collection)
	{
		return new LeftScalarProductIterable<U,V>(u, collection);
	}

	@SuppressWarnings("unchecked")
	public static <U,V> NestedIterable<U,V> crossProduct(Collection<U> collection1, Collection<V> collection2)
	{
		if(collection1.isEmpty() || collection2.isEmpty())
		{
			return (NestedIterable<U,V>) EMPTY_PAIR_ITERABLE;
		}
		else
		{
			return new CrossProductIterable<U,V>(collection1, collection2);
		}
	}


	private static final class EmptyPairIterable implements NestedIterable<Void,Void>
	{
		@Override
		public PairIterator<Void,Void> iterator()
		{
			return EMPTY_PAIR_ITERATOR;
		}

		@Override
		public NestedIterator<Void,Void> nestedIterator()
		{
			return EMPTY_NESTED_ITERATOR;
		}
	}


	private static final class EmptyPairIterator implements PairIterator<Void,Void>
	{
		@Override
		public Void nextFirst()
		{
			throw new NoSuchElementException();
		}

		@Override
		public Void getSecond()
		{
			throw new NoSuchElementException();
		}

		@Override
		public boolean hasNext()
		{
			return false;
		}
	}


	private static final class EmptyNestedIterator extends EmptyIterator implements NestedIterator<Void,Void>
	{
		@Override
		public Iterator<Void> iterator()
		{
			return EMPTY_ITERATOR;
		}
	}


	private static class EmptyIterator implements Iterator<Void>
	{
		@Override
		public Void next()
		{
			throw new NoSuchElementException();
		}

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}


	private PairIterables()
	{
	}
}
