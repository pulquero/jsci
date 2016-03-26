package graphrithms.matching.util;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class SumNestedIterableTest
{
	@Test
	public void test()
	{
		Collection<String> col1 = Arrays.asList("a", "b", "c", "d", "e", "f");
		Collection<Integer> col2 = Arrays.asList(1, 2, 3, 4);
		NestedIterable<String,Integer> iterable1 = PairIterables.crossProduct(col1, col2);

		String left = "left";
		Collection<Integer> rcol = Arrays.asList(1, 2, 3, 4);
		NestedIterable<String,Integer> iterable2 = PairIterables.leftScalarProduct(left, rcol);

		SumNestedIterable<String,Integer> sumIterable = new SumNestedIterable<String,Integer>(iterable1, iterable2);
		NestedIterator<String,Integer> nestedIter = sumIterable.nestedIterator();
		for(String s : col1)
		{
			assertThat(nestedIter.hasNext(), is(true));
			assertThat(nestedIter.next(), is(s));
			Iterator<Integer> iter = nestedIter.iterator();
			for(Integer i : col2)
			{
				assertThat(iter.hasNext(), is(true));
				assertThat(iter.next(), is(i));
			}
		}
		assertThat(nestedIter.next(), is(left));
		Iterator<Integer> iter = nestedIter.iterator();
		for(Integer i : rcol)
		{
				assertThat(iter.hasNext(), is(true));
				assertThat(iter.next(), is(i));
		}
	}

	@Test
	public void testWithEmpties()
	{
		Object l1 = new Object();
		Object r1 = new Object();
		Object l2 = new Object();
		Object r2 = new Object();
		NestedIterable<Object,Object> iterable1 = PairIterables.leftScalarProduct(l1, Collections.singleton(r1));
		NestedIterable<Object,Object> iterable2 = PairIterables.leftScalarProduct(l2, Collections.singleton(r2));

		SumNestedIterable<Object,Object> sumIterable = new SumNestedIterable<Object,Object>(
				PairIterables.empty(),
				PairIterables.empty(),
				iterable1,
				PairIterables.empty(),
				iterable2
		);
		NestedIterator<Object,Object> nestedIter = sumIterable.nestedIterator();
		assertThat(nestedIter.hasNext(), is(true));
		assertThat(nestedIter.next(), is(l1));
		assertThat(nestedIter.iterator().next(), is(r1));
		assertThat(nestedIter.hasNext(), is(true));
		assertThat(nestedIter.next(), is(l2));
		assertThat(nestedIter.iterator().next(), is(r2));
		assertThat(nestedIter.hasNext(), is(false));
	}

	@Test(expected=NoSuchElementException.class)
	public void testIteratorException()
	{
		NestedIterable<Object,Object> iterable = PairIterables.leftScalarProduct(new Object(), Collections.singleton(new Object()));
		SumNestedIterable<Object,Object> sumIterable = new SumNestedIterable<Object,Object>(iterable);
		NestedIterator<Object,Object> nestedIter = sumIterable.nestedIterator();
		nestedIter.iterator();
	}
}
