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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.MapIterator;
import org.apache.commons.collections15.OrderedMap;
import org.apache.commons.collections15.bidimap.AbstractDualBidiMap;
import org.apache.commons.collections15.map.LinkedMap;

public class DualLinkedBidiMap<K,V> extends AbstractDualBidiMap<K,V> implements Serializable
{
	private static final long serialVersionUID = 7193247533614998157L;
	private static final int DEFAULT_CAPACITY = 16;

	public DualLinkedBidiMap()
	{
		this(DEFAULT_CAPACITY);
	}

	public DualLinkedBidiMap(Map<? extends K,? extends V> map)
	{
		this(map.size());
		putAll(map);
	}

	public DualLinkedBidiMap(BidiMap<? extends K,? extends V> bidimap)
	{
		this(bidimap.size());
		putAll(bidimap);
	}

	public DualLinkedBidiMap(int initialCapacity)
	{
		super(new LinkedMap<K,V>(initialCapacity), new LinkedMap<V,K>(initialCapacity));
	}

	protected DualLinkedBidiMap(Map<K,V> normalMap, Map<V,K> reverseMap, BidiMap<V,K> inverseBidiMap)
	{
		super(normalMap, reverseMap, inverseBidiMap);
	}

	@Override
	protected <S,T> BidiMap<S,T> createBidiMap(Map<S,T> normalMap, Map<T,S> reverseMap, BidiMap<T,S> inverseBidiMap)
	{
		return new DualLinkedBidiMap<S,T>(normalMap, reverseMap, inverseBidiMap);
	}

	public void putAll(BidiMap<? extends K,? extends V> bidimap)
	{
		for(MapIterator<? extends K, ? extends V> iter = bidimap.mapIterator(); iter.hasNext(); )
		{
			put(iter.next(), iter.getValue());
		}
	}

	public K firstKey()
	{
		return ((OrderedMap<K,V>)this.forwardMap).firstKey();
	}
	public V firstValue()
	{
		return ((OrderedMap<V,K>)this.inverseMap).firstKey();
	}

	public K nextKey(K k)
	{
		return ((OrderedMap<K,V>)this.forwardMap).nextKey(k);
	}
	public V nextValue(V v)
	{
		return ((OrderedMap<V,K>)this.inverseMap).nextKey(v);
	}

	public K previousKey(K k)
	{
		return ((OrderedMap<K,V>)this.forwardMap).previousKey(k);
	}
	public V previousValue(V v)
	{
		return ((OrderedMap<V,K>)this.inverseMap).previousKey(v);
	}

	public K lastKey()
	{
		return ((OrderedMap<K,V>)this.forwardMap).lastKey();
	}
	public V lastValue()
	{
		return ((OrderedMap<V,K>)this.inverseMap).lastKey();
	}



	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		out.writeObject(this.forwardMap);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		@SuppressWarnings("unchecked")
		Map<K,V> map = (Map<K,V>) in.readObject();
		this.forwardMap = new LinkedMap<K,V>(map.size());
		this.inverseMap = new LinkedMap<V,K>(map.size());
		putAll(map);
	}
}
