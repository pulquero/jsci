package graphrithms.matching.util;

import graphrithms.util.PairIterator;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class CrossProductIterableTest
{
	@Test
	public void testBigSmall()
	{
		Collection<String> col1 = Arrays.asList("a", "b", "c", "d", "e", "f");
		Collection<Integer> col2 = Arrays.asList(1, 2, 3, 4);

		CrossProductIterable<String,Integer> iterable = new CrossProductIterable<String,Integer>(col1, col2);
		PairIterator<String,Integer> pairIter = iterable.iterator();
		NestedIterator<String,Integer> nestedIter = iterable.nestedIterator();
		for(String s : col1)
		{
			assertThat(nestedIter.hasNext(), is(true));
			assertThat(nestedIter.next(), is(s));
			Iterator<Integer> iter = nestedIter.iterator();
			for(Integer i : col2)
			{
				assertThat(pairIter.hasNext(), is(true));
				assertThat(pairIter.nextFirst(), is(s));
				assertThat(pairIter.getSecond(), is(i));
				assertThat(iter.hasNext(), is(true));
				assertThat(iter.next(), is(i));
			}
		}
		assertThat(pairIter.hasNext(), is(false));
		assertThat(nestedIter.hasNext(), is(false));
	}

	@Test
	public void testSmallBig()
	{
		Collection<Integer> col1 = Arrays.asList(1, 2, 3, 4);
		Collection<String> col2 = Arrays.asList("a", "b", "c", "d", "e", "f");

		CrossProductIterable<Integer,String> iterable = new CrossProductIterable<Integer,String>(col1, col2);
		PairIterator<Integer,String> pairIter = iterable.iterator();
		NestedIterator<Integer,String> nestedIter = iterable.nestedIterator();
		for(Integer s : col1)
		{
			assertThat(nestedIter.hasNext(), is(true));
			assertThat(nestedIter.next(), is(s));
			Iterator<String> iter = nestedIter.iterator();
			for(String i : col2)
			{
				assertThat(pairIter.hasNext(), is(true));
				assertThat(pairIter.nextFirst(), is(s));
				assertThat(pairIter.getSecond(), is(i));
				assertThat(iter.hasNext(), is(true));
				assertThat(iter.next(), is(i));
			}
		}
		assertThat(pairIter.hasNext(), is(false));
		assertThat(nestedIter.hasNext(), is(false));
	}

	@Test
	public void testLeftEmpty()
	{
		CrossProductIterable<Void,Integer> iterable = new CrossProductIterable<Void,Integer>(Collections.<Void>emptySet(), Collections.singleton(1));
		PairIterator<Void,Integer> pairIter = iterable.iterator();
		NestedIterator<Void,Integer> nestedIter = iterable.nestedIterator();
		assertThat(pairIter.hasNext(), is(false));
		assertThat(nestedIter.hasNext(), is(false));
	}

	@Test
	public void testRightEmpty()
	{
		CrossProductIterable<Integer,Void> iterable = new CrossProductIterable<Integer,Void>(Collections.singleton(1), Collections.<Void>emptySet());
		PairIterator<Integer,Void> pairIter = iterable.iterator();
		NestedIterator<Integer,Void> nestedIter = iterable.nestedIterator();
		assertThat(pairIter.hasNext(), is(false));
		assertThat(nestedIter.hasNext(), is(true));
		Iterator<Void> iter = nestedIter.iterator();
		assertThat(iter.hasNext(), is(false));
	}

	@Test(expected=NoSuchElementException.class)
	public void testGetSecondException()
	{
		CrossProductIterable<Object,Object> iterable = new CrossProductIterable<Object,Object>(Collections.singleton(new Object()), Collections.singleton(new Object()));
		PairIterator<Object,Object> iter = iterable.iterator();
		iter.getSecond();
	}
}
