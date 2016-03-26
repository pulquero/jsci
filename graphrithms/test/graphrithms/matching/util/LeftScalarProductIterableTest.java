package graphrithms.matching.util;

import graphrithms.util.PairIterator;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class LeftScalarProductIterableTest
{
	@Test
	public void test()
	{
		Object left = new Object();
		Collection<Integer> col = Arrays.asList(1, 2, 3, 4);

		LeftScalarProductIterable<Object,Integer> iterable = new LeftScalarProductIterable<Object,Integer>(left, col);
		PairIterator<Object,Integer> pairIter = iterable.iterator();
		NestedIterator<Object,Integer> nestedIter = iterable.nestedIterator();
		assertThat(nestedIter.hasNext(), is(true));
		assertThat(nestedIter.next(), is(left));
		Iterator<Integer> iter = nestedIter.iterator();
		for(Integer i : col)
		{
			assertThat(pairIter.hasNext(), is(true));
			assertThat(pairIter.nextFirst(), is(left));
			assertThat(pairIter.getSecond(), is(i));
			assertThat(iter.hasNext(), is(true));
			assertThat(iter.next(), is(i));
		}
		assertThat(pairIter.hasNext(), is(false));
		assertThat(nestedIter.hasNext(), is(false));
	}

	@Test
	public void testRightEmpty()
	{
		Object left = new Object();

		LeftScalarProductIterable<Object,Void> iterable = new LeftScalarProductIterable<Object,Void>(left, Collections.<Void>emptySet());
		PairIterator<Object,Void> pairIter = iterable.iterator();
		NestedIterator<Object,Void> nestedIter = iterable.nestedIterator();
		assertThat(pairIter.hasNext(), is(false));
		assertThat(nestedIter.hasNext(), is(true));
		assertThat(nestedIter.next(), is(left));
		Iterator<Void> iter = nestedIter.iterator();
		assertThat(iter.hasNext(), is(false));
	}

	@Test(expected=NoSuchElementException.class)
	public void testGetSecondException()
	{
		LeftScalarProductIterable<Object,Object> iterable = new LeftScalarProductIterable<Object,Object>(new Object(), Collections.singleton(new Object()));
		PairIterator<Object,Object> iter = iterable.iterator();
		iter.getSecond();
	}
}
