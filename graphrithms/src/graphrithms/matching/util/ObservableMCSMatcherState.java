/*
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

import graphrithms.matching.mcs.MCSMatcherState;

public final class ObservableMCSMatcherState<V,E,U,F> extends ObservableMatcherState<V,E,U,F> implements MCSMatcherState<V,E,U,F>
{
	private final MCSMatcherState<V,E,U,F> delegate;

	public ObservableMCSMatcherState(MCSMatcherState<V,E,U,F> state, MatcherStateObserver<V,E,U,F> observer)
	{
		super(state, observer);
		this.delegate = state;
	}

	@Override
	public int getMaxMappingSize()
	{
		return delegate.getMaxMappingSize();
	}

	@Override
	public ObservableMCSMatcherState<V,E,U,F> newState(U u, V v)
	{
		return new ObservableMCSMatcherState<V,E,U,F>(delegate.newState(u, v), observer);
	}

	@Override
	public NestedIterable<U,V> candidates()
	{
		return delegate.candidates();
	}

	public void markVisited(U u)
	{
		delegate.markVisited(u);
	}
}
