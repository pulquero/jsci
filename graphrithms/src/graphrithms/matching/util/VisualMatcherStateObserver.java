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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.AbstractListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import graphrithms.util.PairIterable;
import graphrithms.util.PairIterator;
import graphrithms.matching.MatcherState;

public class VisualMatcherStateObserver<V,E,U,F> implements MatcherStateObserver<V,E,U,F>
{
	private final JFrame frame;
	private VisualizationViewer<U,F> patternViewer;
	private VisualizationViewer<V,E> graphViewer;
	private CandidateListModel<U,V> candidateListModel;
	private boolean initDone = false;
	private AbstractMatcherState<V,E,U,F> state;
	private SpinnerNumberModel timeoutModel = new SpinnerNumberModel(0, 0, null, 100);
	private int backtrackCount = 0;
	private JLabel backtrackNumber = new JLabel("0");
	private final AbstractVisitor<U,V> visitor;
	private JLabel matchesNumber;

    public VisualMatcherStateObserver()
    {
        this(null);
    }

    public VisualMatcherStateObserver(AbstractVisitor<U,V> visitorImpl)
	{
        visitor = visitorImpl;
        frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void init()
	{
	    patternViewer = createViewer(state.pattern);
		graphViewer = createViewer(state.graph);
		updateViewerVertexPainters(state);

		frame.setSize(1000, 600);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new GraphZoomScrollPane(patternViewer), new GraphZoomScrollPane(graphViewer));
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);
		frame.add(splitPane);

		candidateListModel = new CandidateListModel<U,V>();
		updateCandidates(state);
		JList candidateList = new JList(candidateListModel);
		candidateList.setCellRenderer(new DefaultListCellRenderer()
		{
            private static final long serialVersionUID = 4182325983305527976L;

            @Override
		    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		    {
            	Pair<?> pair = (Pair<?>) value;
            	String label = pair.getFirst()+" = "+pair.getSecond();
            	return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
		    }
		});
		frame.add(candidateList, BorderLayout.EAST);

		Box statisticsPanel = Box.createHorizontalBox();
		statisticsPanel.add(new JLabel("Backtracks: "));
		statisticsPanel.add(backtrackNumber);
		if(visitor != null)
		{
		    statisticsPanel.add(Box.createHorizontalStrut(20));
		    statisticsPanel.add(new JLabel("Matches: "));
		    matchesNumber = new JLabel("0");
		    statisticsPanel.add(matchesNumber);
		}

		Box controlPanel = Box.createHorizontalBox();
		JButton stepButton = new JButton("Step");
		stepButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				resume();
			}
		});
		controlPanel.add(stepButton);
		controlPanel.add(new JSpinner(timeoutModel));

		Box bottomPanel = Box.createVerticalBox();
		bottomPanel.add(statisticsPanel);
		bottomPanel.add(controlPanel);
		frame.add(bottomPanel, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	private void updateViewerVertexPainters(AbstractMatcherState<V,E,U,F> state)
	{
	    Collection<U> mappedPatternVertices = state.mapping.keySet();
        patternViewer.getRenderContext().setVertexFillPaintTransformer(new VertexFillPaintTransformer<U>(mappedPatternVertices));

        Collection<V> mappedGraphVertices = state.mapping.values();
        graphViewer.getRenderContext().setVertexFillPaintTransformer(new VertexFillPaintTransformer<V>(mappedGraphVertices));
	}

	private static <V,E> VisualizationViewer<V,E> createViewer(Graph<V,E> graph)
	{
		final ISOMLayout<V,E> layout = new ISOMLayout<V,E>(graph);
		final VisualizationViewer<V,E> viewer = new VisualizationViewer<V,E>(layout);
		viewer.setGraphMouse(new DefaultModalGraphMouse<V,E>());
		viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<V>());
		viewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<V,E>());
		return viewer;
	}

	private void updateCandidates(AbstractMatcherState<V,E,U,F> state)
	{
	    candidateListModel.setCandidates(state.candidates());
	}

	@Override
	public void newState(final MatcherState<V,E,U,F> state)
	{
		if(!initDone)
		{
			this.state = (AbstractMatcherState<V,E,U,F>) state;
			init();
			initDone = true;
		}
		else
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
				    updateState((AbstractMatcherState<V,E,U,F>) state);
				}
			});
			pause();
		}
	}

	private void updateState(AbstractMatcherState<V,E,U,F> state)
	{
	    this.state = state;
	    updateViewerVertexPainters(state);
        updateCandidates(state);
        if(visitor != null)
        {
            matchesNumber.setText(Integer.toString(visitor.getMappings().size()));
        }
        frame.repaint();
	}

	@Override
	public void backtrack(final MatcherState<V,E,U,F> state)
	{
		this.backtrackCount++;
		final String backtrackText = Integer.toString(backtrackCount);
		final String matchesText = (visitor != null) ? Integer.toString(visitor.getMappings().size()) : null;
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				VisualMatcherStateObserver.this.backtrackNumber.setText(backtrackText);
				if(matchesText != null)
				{
				    VisualMatcherStateObserver.this.matchesNumber.setText(matchesText);
				}
			}
		});
	}

	private synchronized void pause()
	{
		try
		{
			this.wait(timeoutModel.getNumber().longValue());
		}
		catch (InterruptedException e)
		{
		}
	}

	private synchronized void resume()
	{
		this.notifyAll();
	}

	static class VertexFillPaintTransformer<V> implements Transformer<V,Paint>
	{
		private static final Color MAPPED_COLOR = Color.RED;
		private static final Color DEFAULT_COLOR = Color.YELLOW;
		private final Map<V,Color> mappedVertices;

		public VertexFillPaintTransformer(Collection<V> mappedVertices)
		{
            int size = mappedVertices.size();
		    this.mappedVertices = new HashMap<V,Color>(size);
		    int n = 0;
		    for(V v : mappedVertices)
		    {
                Color c = fade(MAPPED_COLOR, (float) (size-n)/(float) size);
		        this.mappedVertices.put(v, c);
		        n++;
		    }
		}

		@Override
		public Paint transform(V v)
		{
		    Paint colour = mappedVertices.get(v);
		    if(colour == null)
		    {
		        colour = DEFAULT_COLOR;
		    }
		    return colour;
		}
	}

	static Color fade(Color c, float fraction)
	{
	    return new Color(Math.round(fraction*c.getRed()), Math.round(fraction*c.getGreen()), Math.round(fraction*c.getBlue()));
	}

	static class CandidateListModel<U,V> extends AbstractListModel
	{
        private static final long serialVersionUID = 1949307680471712813L;

        private List<Pair<Object>> candidateList = Collections.emptyList();

        public void setCandidates(PairIterable<U,V> candidates)
        {
            final int newSize = candidateList.size(); // estimate size based on previous size
            List<Pair<Object>> newList = new ArrayList<Pair<Object>>(newSize);
            for(PairIterator<U,V> iter = candidates.iterator(); iter.hasNext(); )
            {
                newList.add(new Pair<Object>(iter.nextFirst(), iter.getSecond()));
            }
            candidateList = newList;
            fireContentsChanged(this, 0, candidateList.size()-1);
        }

        @Override
        public Object getElementAt(int index)
        {
            return candidateList.get(index);
        }

        @Override
        public int getSize()
        {
            return candidateList.size();
        }
	}
}
