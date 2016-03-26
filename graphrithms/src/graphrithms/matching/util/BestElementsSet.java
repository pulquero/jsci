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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Set that maintains only the best elements
 * according to a given objective function.
 */
public final class BestElementsSet<E> implements Set<E>, Serializable
{
	private static final long serialVersionUID = -3110182726463037200L;

	private final int initialCapacity;
	private final Function<? super E> objectiveFunction;
	private Set<E> elements;
	private double bestValue;

	public BestElementsSet(int initialCapacity, Function<? super E> objectiveFunction)
	{
		this.initialCapacity = initialCapacity;
		this.objectiveFunction = objectiveFunction;
		this.elements = Collections.emptySet();
	}

	public double getBestValue()
	{
		return bestValue;
	}

	@Override
	public boolean add(E e)
	{
		double value = objectiveFunction.evaluate(e);
		int diff = !elements.isEmpty() ? (int) Math.signum(value - bestValue) : 1;
		if(diff > 0)
		{
			bestValue = value;
			elements = null; // free existing memory before reallocating
			elements = new HashSet<E>(initialCapacity);
		}
		return (diff >= 0) ? elements.add(e) : false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		boolean modified = false;
		for(E e : c)
		{
			modified |= add(e);
		}
		return modified;
	}

	@Override
	public boolean equals(Object o)
	{
		return elements.equals(o);
	}

	@Override
	public int hashCode()
	{
		return elements.hashCode();
	}

	@Override
	public int size()
	{
		return elements.size();
	}

	@Override
	public boolean isEmpty()
	{
		return elements.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return elements.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		return elements.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return elements.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return elements.toArray(a);
	}

	@Override
	public boolean remove(Object o)
	{
		return elements.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return elements.containsAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return elements.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return elements.removeAll(c);
	}

	@Override
	public void clear()
	{
		elements.clear();
	}

	@Override
	public String toString()
	{
		return elements.toString();
	}
}
