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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;

/**
 * Visits the first N mappings.
 */
public class FirstNVisitor<U,V> extends AbstractVisitor<U,V>
{
	private static final int DEFAULT_CAPACITY = 257;

	private final Set<BidiMap<U,V>> mappings;
	private final int maxMappings;

	/**
	 * Constructor to visit all mappings.
	 */
	public FirstNVisitor()
	{
		this(DEFAULT_CAPACITY, -1);
	}

	/**
	 * Constructor to visit the first N mappings.
	 */
	public FirstNVisitor(int max)
	{
		this(max != -1 ? max : DEFAULT_CAPACITY, max);
	}

	private FirstNVisitor(int initialCapacity, int max)
	{
		if(max < -1 || max == 0)
		{
			throw new IllegalArgumentException("Must visit at least one mapping.");
		}
		this.mappings = new HashSet<BidiMap<U,V>>(initialCapacity);
		this.maxMappings = max;
	}

	@Override
	public boolean visit(BidiMap<U,V> mapping)
	{
		mappings.add(mapping);
		return (maxMappings != -1) && (mappings.size() >= maxMappings);
	}

	@Override
	public Set<BidiMap<U,V>> getMappings()
	{
		return mappings;
	}
}
