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

import java.util.NoSuchElementException;

import graphrithms.util.PairIterable;
import graphrithms.util.PairIterator;

public class SumPairIterable<U,V> implements PairIterable<U,V>
{
	private final PairIterable<U,V>[] iterables;

	public SumPairIterable(PairIterable<U,V>... iterables)
	{
		this.iterables = iterables;
	}

	@Override
	public PairIterator<U,V> iterator()
	{
		return new SumPairIterator<U,V>(iterables);
	}



	static class SumPairIterator<U,V> implements PairIterator<U,V>
	{
		private final PairIterable<U,V>[] iterables;
		private int pos = 0;
		private PairIterator<U,V> iterator;
		private boolean nextCalled;

		SumPairIterator(PairIterable<U,V>... iterables)
		{
			this.iterables = iterables;
		}

		@Override
		public boolean hasNext()
		{
			boolean available = (iterator != null && iterator.hasNext());
			while(!available && pos < iterables.length)
			{
				iterator = iterables[pos++].iterator();
				available = iterator.hasNext();
			}
			return available;
		}

		@Override
		public U nextFirst()
		{
			boolean available = (iterator != null && iterator.hasNext());
			while(!available && pos < iterables.length)
			{
				iterator = iterables[pos++].iterator();
				available = iterator.hasNext();
			}
			if(!available)
			{
				throw new NoSuchElementException();
			}
			U u = iterator.nextFirst();
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
			return iterator.getSecond();
		}
	}
}
