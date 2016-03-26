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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import graphrithms.util.PairIterable;
import graphrithms.util.PairIterator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Forest implementation of disjoint sets (union-find).
 */
public final class DisjointSets<E> implements PairIterable<E,E>
{
	private static final int DEFAULT_CAPACITY = 16;

	// elements of all sets
	private final Map<E,Node<E>> elements;
	private int setCount;

	public DisjointSets()
	{
		this(DEFAULT_CAPACITY);
	}

	public DisjointSets(int capacity)
	{
		this.elements = new HashMap<E,Node<E>>(capacity);
	}

	/**
	 * Creates a disjoint set containing the specified element.
	 * @return the representative of the set.
	 */
	public E makeSet(E element) throws IllegalStateException
	{
		return createSet(element).value;
	}

	private Node<E> createSet(E element) throws IllegalStateException
	{
		Node<E> node = new Node<E>(element);
		add(element, node);
		setCount++;
		return node;
	}

	/**
	 * Creates a disjoint set containing the specified elements.
	 * @return the representative of the set.
	 */
	public E makeSet(Collection<E> col) throws IllegalStateException
	{
		if(col.isEmpty())
		{
			return null;
		}

		Iterator<E> iter = col.iterator();
		final E rep = iter.next();
		final Node<E> root = new Node<E>(rep);
		root.rank = 1;
		add(rep, root);
		while(iter.hasNext())
		{
			E element = iter.next();
			Node<E> node = new Node<E>(element, root);
			add(element, node);
		}
		setCount++;
		return rep;
	}

	private void add(E element, Node<E> node) throws IllegalStateException
	{
		Node<E> oldNode = elements.put(element, node);
		if(oldNode != null)
		{
			throw new IllegalStateException("Element is already in a set: "+element+" is in ["+find(oldNode)+"]");
		}
	}

	/**
	 * Returns the representative of the set containing the specified element,
	 * or null if the element does not belong to any set.
	 */
	public E find(E element)
	{
		Node<E> node = elements.get(element);
		if(node == null)
		{
			return null;
		}
		return find(node).value;
	}

	private Node<E> find(Node<E> node)
	{
		Node<E> parent = node.parent;
		if(parent == null)
		{
			return node;
		}
		if(parent.parent == null)
		{
			return parent;
		}

		ArrayList<Node<E>> stack = new ArrayList<Node<E>>();
		do
		{
			stack.add(node);
			node = parent;
			parent = node.parent;
		}
		while(parent.parent != null);
		for(int i=stack.size()-1; i>=0; i--)
		{
			stack.get(i).parent = parent;
		}
		return parent;
	}

	/**
	 * Unites the sets containing the specified elements.
	 */
	public void union(E e1, E e2)
	{
		Node<E> node1 = getNode(e1);
		Node<E> node2 = getNode(e2);
		union(node1, node2);
	}

	private Node<E> getNode(E element)
	{
		Node<E> node = elements.get(element);
		if(node == null)
		{
			throw new NoSuchElementException("Unknown element: "+element);
		}
		return node;
	}

	private void union(Node<E> e1, Node<E> e2)
	{
		Node<E> root1 = find(e1);
		Node<E> root2 = find(e2);

		if(root1 == root2)
		{
			return;
		}

		if(root1.rank > root2.rank)
		{
			// append tree 2 on to tree 1
			root2.parent = root1;
		}
		else if(root1.rank < root2.rank)
		{
			// append tree 1 on to tree 2
			root1.parent = root2;
		}
		else
		{
			root1.parent = root2;
			root2.rank++;
		}
		setCount--;
	}

	/**
	 * Merges a set of disjoint sets into this set.
	 */
	public void merge(DisjointSets<E> other)
	{
		for(Node<E> otherNode : other.elements.values())
		{
			Node<E> otherParent = otherNode.parent;
			if(otherParent != null)
			{
				// element-rep edge
				E element = otherNode.value;
				E rep = otherParent.value;
				externalUnion(element, rep);
			}
		}
	}

	/**
	 * Merges a collection of elements and their representatives into this set.
	 */
	public void merge(PairIterator<E,E> iter)
	{
		while(iter.hasNext())
		{
			E element = iter.nextFirst();
			E rep = iter.getSecond();
			// do a quick check
			if(element != rep)
			{
				// element-rep edge
				externalUnion(element, rep);
			}
		}
	}

	private void externalUnion(E e1, E e2)
	{
		Node<E> root1 = externalFind(e1);
		Node<E> root2 = externalFind(e2);
		if(root1 != root2)
		{
			union(root1, root2);
		}
	}

	private Node<E> externalFind(E e)
	{
		Node<E> node = this.elements.get(e);
		return (node != null) ? find(node) : createSet(e);
	}

	/**
	 * Returns the number of disjoint sets.
	 */
	public int getSetCount()
	{
		return setCount;
	}

	/**
	 * Returns the number of elements.
	 */
	public int size()
	{
		return elements.size();
	}

	public PairIterator<E,E> iterator()
	{
		return new PairIterator<E,E>()
		{
			private final Iterator<Node<E>> nodeIter = elements.values().iterator();
			private Node<E> node;

			public boolean hasNext()
			{
				return nodeIter.hasNext();
			}

			public E nextFirst()
			{
				node = nodeIter.next();
				return node.value;
			}

			public E getSecond()
			{
				return find(node).value;
			}
		};
	}

	/**
	 * Returns a map of elements to their set representatives.
	 */
	public Map<E,E> getRepresentatives()
	{
		Map<E,E> reps = new HashMap<E,E>(elements.size());
		for(Node<E> node : elements.values())
		{
			reps.put(node.value, find(node).value);
		}
		return reps;
	}

	/**
	 * Returns the disjoint sets.
	 */
	public Map<E,Collection<E>> getSets()
	{
		Map<E,Collection<E>> sets = new HashMap<E,Collection<E>>(setCount);
		final int meanSetSize = elements.size()/setCount; // average size heuristic
		for(Node<E> node : elements.values())
		{
			Node<E> root = find(node);
			E rep = root.value;
			Collection<E> set = sets.get(rep);
			if(set == null)
			{
				set = new ArrayList<E>(meanSetSize);
				sets.put(rep, set);
			}
			set.add(node.value);
		}
		return sets;
	}



	private static final class Node<E>
	{
		private final E value;
		private Node<E> parent;
		private int rank;

		/**
		 * Constructs a root node.
		 */
		private Node(E element)
		{
			this(element, null);
		}

		/**
		 * Constructs a child node.
		 */
		private Node(E element, Node<E> parent)
		{
			this.value = element;
			this.parent = parent;
		}
	}
}
