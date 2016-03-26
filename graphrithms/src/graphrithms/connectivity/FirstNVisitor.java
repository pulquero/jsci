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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Visits the first N connected components.
 */
public class FirstNVisitor<V> implements Visitor<V>
{
	private static final int COMPONENTS_CAPACITY = 127;
	private static final int COMPONENT_CAPACITY = 127;

	private final Collection<Collection<V>> components;
	private final int maxComponents;
	private Collection<V> component;

	/**
	 * Constructor to visit all components.
	 */
	public FirstNVisitor()
	{
		this(COMPONENTS_CAPACITY, -1);
	}

	/**
	 * Constructor to visit the first N components.
	 */
	public FirstNVisitor(int max)
	{
		this(max, max);
	}

	private FirstNVisitor(int initialCapacity, int max)
	{
		this.components = new ArrayList<Collection<V>>(initialCapacity);
		if(max < -1 || max == 0)
		{
			throw new IllegalArgumentException("Must visit at least one component.");
		}
		this.maxComponents = max;
	}

	public void startComponent()
	{
		component = new ArrayList<V>(COMPONENT_CAPACITY);
	}

	public void visit(V v)
	{
		component.add(v);
	}

	public boolean endComponent()
	{
		components.add(component);
		component = null;
		return (maxComponents != -1) && (components.size() >= maxComponents);
	}

	public Collection<Collection<V>> getComponents()
	{
		return components;
	}
}
