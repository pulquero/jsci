package graphrithms.matching.util;

import graphrithms.util.PairIterable;
import graphrithms.util.PairIterator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class SumPairIterableTest
{
	@Test
	public void test()
	{
		Collection<String> col1 = Arrays.asList("a", "b", "c", "d", "e", "f");
		Collection<Integer> col2 = Arrays.asList(1, 2, 3, 4);
		PairIterable<String,Integer> iterable1 = PairIterables.crossProduct(col1, col2);

		String left = "left";
		Collection<Integer> rcol = Arrays.asList(1, 2, 3, 4);
		PairIterable<String,Integer> iterable2 = PairIterables.leftScalarProduct(left, rcol);

		SumPairIterable<String,Integer> sumIterable = new SumPairIterable<String,Integer>(iterable1, iterable2);
		PairIterator<String,Integer> iter = sumIterable.iterator();
		for(String s : col1)
		{
			for(Integer i : col2)
			{
				assertThat(iter.hasNext(), is(true));
				assertThat(iter.nextFirst(), is(s));
				assertThat(iter.getSecond(), is(i));
			}
		}
		for(Integer i : rcol)
		{
			assertThat(iter.hasNext(), is(true));
			assertThat(iter.nextFirst(), is(left));
			assertThat(iter.getSecond(), is(i));
		}
	}

	@Test
	public void testWithEmpties()
	{
		Object l1 = new Object();
		Object r1 = new Object();
		Object l2 = new Object();
		Object r2 = new Object();
		PairIterable<Object,Object> iterable1 = PairIterables.leftScalarProduct(l1, Collections.singleton(r1));
		PairIterable<Object,Object> iterable2 = PairIterables.leftScalarProduct(l2, Collections.singleton(r2));

		SumPairIterable<Object,Object> sumIterable = new SumPairIterable<Object,Object>(
				PairIterables.empty(),
				PairIterables.empty(),
				iterable1,
				PairIterables.empty(),
				iterable2
		);
		PairIterator<Object,Object> iter = sumIterable.iterator();
		assertThat(iter.hasNext(), is(true));
		assertThat(iter.nextFirst(), is(l1));
		assertThat(iter.getSecond(), is(r1));
		assertThat(iter.hasNext(), is(true));
		assertThat(iter.nextFirst(), is(l2));
		assertThat(iter.getSecond(), is(r2));
		assertThat(iter.hasNext(), is(false));
	}

	@Test(expected=NoSuchElementException.class)
	public void testGetSecondException()
	{
		PairIterable<Object,Object> iterable = PairIterables.leftScalarProduct(new Object(), Collections.singleton(new Object()));
		SumPairIterable<Object,Object> sumIterable = new SumPairIterable<Object,Object>(iterable);
		PairIterator<Object,Object> iter = sumIterable.iterator();
		iter.getSecond();
	}
}
