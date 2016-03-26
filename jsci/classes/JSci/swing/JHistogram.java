package JSci.swing;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import JSci.awt.*;
import JSci.maths.ExtraMath;

/**
* A histogram Swing component.
* The y-values are the counts for each bin.
* Each bin is specified by an interval.
* So that y[i] contains the counts for the bin from x[i-1] to x[i].
* The value of y[0] is disregarded.
* @version 1.0
* @author Mark Hale
*/
public class JHistogram extends JDoubleBufferedComponent implements GraphDataListener {
        /**
        * Data model.
        */
        protected Graph2DModel model;
        /**
        * Origin.
        */
        protected Point origin = new Point();
        /**
        * Series colors.
        */
        protected Color seriesColor[]={Color.blue,Color.green,Color.red,Color.yellow,Color.cyan,Color.lightGray,Color.magenta,Color.orange,Color.pink};
        /**
        * Axis numbering.
        */
        protected boolean numbering = true;
        protected NumberFormat xNumberFormat = new DecimalFormat("##0.##");
        protected NumberFormat yNumberFormat = new DecimalFormat("##0.##");
        protected boolean gridLines = false;
        private final Color gridLineColor = Color.lightGray;
        /**
        * Axis scaling.
        */
        private float xScale,yScale;
        /**
        * Axis extrema.
        */
        private float minX, minY, maxX, maxY;
        private boolean autoXExtrema=true, autoYExtrema=true;
        private float xGrowth, yGrowth;
        /**
        * Axis numbering increment.
        */
        private final float xIncPixels = 40.0f;
        private final float yIncPixels = 40.0f;
        private float xInc,yInc;
        private boolean autoXInc=true,autoYInc=true;
        /**
        * Padding.
        */
        protected final int scalePad=5;
        protected final int axisPad=25;
        protected int leftAxisPad;
        /**
        * Constructs a histogram.
        */
        public JHistogram(Graph2DModel gm) {
                model=gm;
                model.addGraphDataListener(this);
                dataChanged(new GraphDataEvent(model));
        }
        /**
        * Sets the data plotted by this graph to the specified data.
        */
        public final void setModel(Graph2DModel gm) {
                model.removeGraphDataListener(this);
                model=gm;
                model.addGraphDataListener(this);
                dataChanged(new GraphDataEvent(model));
        }
        /**
        * Returns the model used by this graph.
        */
        public final Graph2DModel getModel() {
                return model;
        }
        /**
        * Implementation of GraphDataListener.
        * Application code will not use this method explicitly, it is used internally.
        */
        public void dataChanged(GraphDataEvent e) {
                if(e.isIncremental()) {
                        Graphics g = getOffscreenGraphics();
                        if(g == null)
                                return;
                        final int series = e.getSeries();
                        if(series == GraphDataEvent.ALL_SERIES) {
                                model.firstSeries();
                                int n = 0;
                                do {
                                        final int i = model.seriesLength() - 1;
                                        incrementalRescale(model.getXCoord(i), model.getYCoord(i));
                                        g.setColor(seriesColor[n]);
                                        drawDataPoint(g, i);
                                        n++;
                                } while(model.nextSeries());
                        } else {
                                model.firstSeries();
                                int n=0;
                                for(; n<series; n++)
                                        model.nextSeries();
                                final int i = model.seriesLength() - 1;
                                incrementalRescale(model.getXCoord(i), model.getYCoord(i));
                                g.setColor(seriesColor[n]);
                                drawDataPoint(g, i);
                        }
                        repaint();
                } else {
                        // ensure there are enough colors
                        int n = 1;
                        model.firstSeries();
                        while(model.nextSeries())
                                n++;
                        if(n>seriesColor.length) {
                                Color tmp[]=seriesColor;
                                seriesColor=new Color[n];
                                System.arraycopy(tmp,0,seriesColor,0,tmp.length);
                                for(int i=tmp.length; i<n; i++)
                                        seriesColor[i]=seriesColor[i-tmp.length];
                        }
                        if(autoXExtrema)
                                setXExtrema(0.0f, 0.0f);
                        if(autoYExtrema)
                                setYExtrema(0.0f, 0.0f);
                        redraw();
                }
        }
        private void incrementalRescale(final float x, final float y) {
                float min, max;
                if(x < minX)
                        min = autoXExtrema ? Math.min(x, minX-xGrowth) : minX-xGrowth;
                else
                        min = minX;
                if(x > maxX)
                        max = autoXExtrema ? Math.max(x, maxX+xGrowth) : maxX+xGrowth;
                else
                        max = maxX;
                rescaleX(min, max);

                if(y < minY)
                        min = autoYExtrema ? Math.min(y, minY-yGrowth) : minY-yGrowth;
                else
                        min = minY;
                if(y > maxY)
                        max = autoYExtrema ? Math.max(y, maxY+yGrowth) : maxY+yGrowth;
                else
                        max = maxY;
                rescaleY(min, max);
        }
        /**
        * Turns axis numbering on/off.
        * Default is on.
        */
        public final void setNumbering(boolean flag) {
                numbering=flag;
                leftAxisPad=axisPad;
                if(numbering && getFont() != null) {
                        // adjust leftAxisPad to accomodate y-axis numbering
                        final FontMetrics metrics = getFontMetrics(getFont());
                        final int maxYNumLen = metrics.stringWidth(yNumberFormat.format(maxY));
                        final int minYNumLen = metrics.stringWidth(yNumberFormat.format(minY));
                        int yNumPad = Math.max(minYNumLen, maxYNumLen);
                        if(minX<0.0f) {
                                final int negXLen = (int)((Math.max(getSize().width,getMinimumSize().width)-2*(axisPad+scalePad))*minX/(minX-maxX));
                                yNumPad = Math.max(yNumPad-negXLen, 0);
                        }
                        leftAxisPad += yNumPad;
                }
                rescale();
        }
        public void addNotify() {
                super.addNotify();
                // getFont() is now not null
                // recalculate padding
                setNumbering(numbering);
        }
        /**
        * Sets the display format used for axis numbering.
        * Convenience method.
        * @see #setXNumberFormat(NumberFormat)
        * @see #setYNumberFormat(NumberFormat)
        */
        public final void setNumberFormat(NumberFormat format) {
                xNumberFormat = format;
                yNumberFormat = format;
                setNumbering(numbering);
        }
        /**
        * Sets the display format used for x-axis numbering.
        */
        public final void setXNumberFormat(NumberFormat format) {
                xNumberFormat = format;
                setNumbering(numbering);
        }
        /**
        * Sets the display format used for y-axis numbering.
        */
        public final void setYNumberFormat(NumberFormat format) {
                yNumberFormat = format;
                setNumbering(numbering);
        }
        /**
        * Turns grid lines on/off.
        * Default is off.
        */
        public final void setGridLines(boolean flag) {
                gridLines = flag;
                redraw();
        }
        /**
        * Sets the x-axis numbering increment.
        * @param dx use 0.0f for auto-adjusting (default).
        */
        public final void setXIncrement(float dx) {
                if(dx < 0.0f) {
                        throw new IllegalArgumentException("Increment should be positive.");
                } else if(dx == 0.0f) {
                        if(!autoXInc) {
                                autoXInc = true;
                                rescale();
                        }
                } else {
                        autoXInc = false;
                        if(dx != xInc) {
                                xInc = dx;
                                rescale();
                        }
                }
        }
        /**
        * Returns the x-axis numbering increment.
        */
        public final float getXIncrement() {
                return xInc;
        }
        /**
        * Sets the y-axis numbering increment.
        * @param dy use 0.0f for auto-adjusting (default).
        */
        public final void setYIncrement(float dy) {
                if(dy < 0.0f) {
                        throw new IllegalArgumentException("Increment should be positive.");
                } else if(dy == 0.0f) {
                        if(!autoYInc) {
                                autoYInc = true;
                                rescale();
                        }
                } else {
                        autoYInc = false;
                        if(dy != yInc) {
                                yInc = dy;
                                rescale();
                        }
                }
        }
        /**
        * Returns the y-axis numbering increment.
        */
        public final float getYIncrement() {
                return yInc;
        }
        /**
        * Sets the minimum/maximum values on the x-axis.
        * Set both min and max to 0.0f for auto-adjusting (default).
        */
        public final void setXExtrema(float min, float max) {
                if(min==0.0f && max==0.0f) {
                        autoXExtrema=true;
                        // determine min and max from model
                        min=Float.POSITIVE_INFINITY;
                        max=Float.NEGATIVE_INFINITY;
                        float tmp;
                        model.firstSeries();
                        do {
                                for(int i=0;i<model.seriesLength();i++) {
                                        tmp=model.getXCoord(i);
                                        if(!Float.isNaN(tmp)) {
                                                min=Math.min(tmp,min);
                                                max=Math.max(tmp,max);
                                        }
                                }
                        } while(model.nextSeries());
                        if(min==max) {
                                // default values if no variation in data
                                min-=0.5f;
                                max+=0.5f;
                        }
                        if(min==Float.POSITIVE_INFINITY || max==Float.NEGATIVE_INFINITY) {
                                // default values if no data
                                min=-5.0f;
                                max=5.0f;
                        }
                } else if(max<=min) {
                        throw new IllegalArgumentException("Maximum should be greater than minimum; max = "+max+" and min = "+min);
                } else {
                        autoXExtrema=false;
                }
                rescaleX(min, max);
        }
        public final void setXExtrema(float min, float max, float growth) {
                setXExtrema(min, max);
                xGrowth = growth;
        }
	public final float getXMinimum() {
		return minX;
	}
	public final float getXMaximum() {
		return maxX;
	}
        private void rescaleX(final float min, final float max) {
                if(min != minX || max != maxX) {
                        minX = min;
                        maxX = max;
                        setNumbering(numbering);
                }
        }
        /**
        * Sets the minimum/maximum values on the y-axis.
        * Set both min and max to 0.0f for auto-adjusting (default).
        */
        public final void setYExtrema(float min, float max) {
                if(min==0.0f && max==0.0f) {
                        autoYExtrema=true;
                        // determine min and max from model
                        min=Float.POSITIVE_INFINITY;
                        max=Float.NEGATIVE_INFINITY;
                        float tmp;
                        model.firstSeries();
                        do {
                                for(int i=0;i<model.seriesLength();i++) {
                                        tmp=model.getYCoord(i);
                                        if(!Float.isNaN(tmp)) {
                                                min=Math.min(tmp,min);
                                                max=Math.max(tmp,max);
                                        }
                                }
                        } while(model.nextSeries());
                        if(min==max) {
                                // default values if no variation in data
                                max+=5.0f;
                        }
                        if(min==Float.POSITIVE_INFINITY || max==Float.NEGATIVE_INFINITY) {
                                // default values if no data
                                min=0.0f;
                                max=5.0f;
                        }
                } else if(max<=min) {
                        throw new IllegalArgumentException("Maximum should be greater than minimum; max = "+max+" and min = "+min);
                } else {
                        autoYExtrema=false;
                }
                rescaleY(min, max);
        }
        public final void setYExtrema(float min, float max, float growth) {
                setYExtrema(min, max);
                yGrowth = growth;
        }
	public final float getYMinimum() {
		return minY;
	}
	public final float getYMaximum() {
		return maxY;
	}
        private void rescaleY(final float min, final float max) {
                if(min != minY || max != maxY) {
                        minY = min;
                        maxY = max;
                        setNumbering(numbering);
                }
        }
        /**
        * Returns the bounding box for the axis extrema.
        */
        public final Rectangle2D.Float getExtrema() {
                return new Rectangle2D.Float(minX, minY, maxX-minX, maxY-minY);
        }
        /**
        * Sets the color of the nth y-series.
        * @param n the index of the y-series.
        * @param c the line color.
        */
        public final void setColor(int n,Color c) {
                seriesColor[n]=c;
                redraw();
        }
        /**
        * Gets the color of the nth y-series.
        * @param n the index of the y-series.
        */
        public final Color getColor(int n) {
                return seriesColor[n];
        }
        /**
        * Reshapes this graph to the specified bounding box.
        */
        public final void setBounds(int x,int y,int width,int height) {
                super.setBounds(x,y,width,height);
                rescale();
        }
        /**
        * Returns the preferred size of this component.
        */
        public Dimension getPreferredSize() {
                return getMinimumSize();
        }
        /**
        * Returns the minimum size of this component.
        */
        public Dimension getMinimumSize() {
                return new Dimension(170, 170);
        }
        /**
        * Rescales this graph.
        */
        protected final void rescale() {
                final Dimension minSize = getMinimumSize();
                // Swing optimised
                final int thisWidth=Math.max(getWidth(), minSize.width);
                final int thisHeight=Math.max(getHeight(), minSize.height);
                xScale = (float) ((double)(thisWidth-(leftAxisPad+axisPad)) / (double)(maxX-minX));
                yScale = (float) ((double)(thisHeight-2*axisPad) / (double)(maxY-minY));
                if(autoXInc) {
                        xInc = (float) ExtraMath.round((double)xIncPixels/(double)xScale, 1);
                        if(xInc == 0.0f)
                                xInc = Float.MIN_VALUE;
                }
                //assert xInc > 0.0f;
                if(autoYInc) {
                        yInc = (float) ExtraMath.round((double)yIncPixels/(double)yScale, 1);
                        if(yInc == 0.0f)
                                yInc = Float.MIN_VALUE;
                }
                //assert yInc > 0.0f;
                origin.x=leftAxisPad-Math.round(minX*xScale);
                origin.y=thisHeight-axisPad+Math.round(minY*yScale);
                redraw();
        }
        /**
        * Converts a data point to screen coordinates.
        */
        protected final Point dataToScreen(float x,float y) {
                return new Point(origin.x+Math.round(xScale*x), origin.y-Math.round(yScale*y));
        }
        /**
        * Converts a screen point to data coordinates.
        */
        protected final Point2D.Float screenToData(Point p) {
                double x = (double)(p.x-origin.x) / (double)xScale;
                double y = (double)(origin.y-p.y) / (double)yScale;
                return new Point2D.Float((float)x, (float)y);
        }
        /**
        * Draws the graph axes.
        */
        protected final void drawAxes(Graphics g) {
                // Swing optimised
                final int width = getWidth();
                final int height = getHeight();
                g.setColor(getForeground());
// grid lines and numbering
                if(gridLines || numbering) {
// x-axis numbering and vertical grid lines
                        float xAxisY;
                        if(minY > 0.0f) {
                                xAxisY = minY;
                        } else if(maxY <= 0.0f) {
                                xAxisY = maxY;
                        } else {
                                xAxisY = 0.0f;
			}
                        for(double x=(minX>0.0f)?minX:xInc; x<=maxX; x+=xInc) {
                                Point p=dataToScreen((float)x, xAxisY);
                                if(gridLines) {
                                        g.setColor(gridLineColor);
                                        g.drawLine(p.x, axisPad-scalePad, p.x, height-(axisPad-scalePad));
                                        g.setColor(getForeground());
                                }
                                if(numbering) {
					drawXLabel(g, x, p);
                                }
                        }
                        for(double x=-xInc; x>=minX; x-=xInc) {
                                Point p=dataToScreen((float)x, xAxisY);
                                if(gridLines) {
                                        g.setColor(gridLineColor);
                                        g.drawLine(p.x, axisPad-scalePad, p.x, height-(axisPad-scalePad));
                                        g.setColor(getForeground());
                                }
                                if(numbering) {
					drawXLabel(g, x, p);
                                }
                        }
// y-axis numbering and horizontal grid lines
                        float yAxisX;
                        if(minX > 0.0f)
                                yAxisX = minX;
                        else if(maxX < 0.0f)
                                yAxisX = maxX;
                        else
                                yAxisX = 0.0f;
                        for(double y=(minY>0.0f)?minY:yInc; y<=maxY; y+=yInc) {
                                Point p=dataToScreen(yAxisX, (float)y);
                                if(gridLines) {
                                        g.setColor(gridLineColor);
                                        g.drawLine(leftAxisPad-scalePad, p.y, width-(axisPad-scalePad), p.y);
                                        g.setColor(getForeground());
                                }
                                if(numbering) {
					drawYLabel(g, y, p);
                                }
                        }
                        for(double y=-yInc; y>=minY; y-=yInc) {
                                Point p=dataToScreen(yAxisX, (float)y);
                                if(gridLines) {
                                        g.setColor(gridLineColor);
                                        g.drawLine(leftAxisPad-scalePad, p.y, width-(axisPad-scalePad), p.y);
                                        g.setColor(getForeground());
                                }
                                if(numbering) {
					drawYLabel(g, y, p);
                                }
                        }
                }

// axis lines
                // horizontal axis
                if(minY > 0.0f) {
                        // draw at bottom
                        g.drawLine(leftAxisPad-scalePad, height-axisPad, width-(axisPad-scalePad), height-axisPad);
                } else if(maxY < 0.0f) {
                        // draw at top
                        g.drawLine(leftAxisPad-scalePad, axisPad, width-(axisPad-scalePad), axisPad);
                } else {
                        // draw through y origin
                        g.drawLine(leftAxisPad-scalePad, origin.y, width-(axisPad-scalePad), origin.y);
                }
                // vertical axis
                if(minX > 0.0f) {
                        // draw at left
                        g.drawLine(leftAxisPad, axisPad-scalePad, leftAxisPad, height-(axisPad-scalePad));
                } else if(maxX < 0.0f) {
                        // draw at right
                        g.drawLine(width-axisPad, axisPad-scalePad, width-axisPad, height-(axisPad-scalePad));
                } else {
                        // draw through x origin
                        g.drawLine(origin.x, axisPad-scalePad, origin.x, height-(axisPad-scalePad));
                }
        }
	protected void drawXLabel(Graphics g, double x, Point p) {
		String str = xNumberFormat.format(x);
		FontMetrics metrics=g.getFontMetrics();
		int strWidth=metrics.stringWidth(str);
		int strHeight=metrics.getHeight();
		boolean numberingAbove = (maxY <= 0.0f);
		if(numberingAbove) {
			g.drawLine(p.x,p.y,p.x,p.y-5);
			g.drawString(str,p.x-strWidth/2,p.y-7);
		} else {
			g.drawLine(p.x,p.y,p.x,p.y+5);
			g.drawString(str,p.x-strWidth/2,p.y+5+strHeight);
		}
	}
	protected void drawYLabel(Graphics g, double y, Point p) {
		String str = yNumberFormat.format(y);
		FontMetrics metrics=g.getFontMetrics();
		int strWidth=metrics.stringWidth(str);
		int strHeight=metrics.getHeight();
		g.drawLine(p.x,p.y,p.x-5,p.y);
		g.drawString(str,p.x-8-strWidth,p.y+strHeight/3);
	}
        /**
        * Draws the graph data.
        * Override this method to change how the graph data is plotted.
        */
        protected void drawData(Graphics g) {
// bars
                model.firstSeries();
                for(int i=1; i<model.seriesLength(); i++) {
	                g.setColor(seriesColor[0]);
                        drawDataPoint(g, i);
		}
                for(int n=1; model.nextSeries(); n++) {
                        for(int i=1; i<model.seriesLength(); i++) {
	                        g.setColor(seriesColor[n]);
                                drawDataPoint(g, i);
			}
                }
        }
        /**
        * Draws a single data point in the current series.
        * @param i index of the data point.
        */
        private void drawDataPoint(Graphics g, int i) {
		if(i == 0)
			return;
		float value = model.getYCoord(i);
                Point p1 = dataToScreen(model.getXCoord(i-1), 0.0f);
                Point p2 = dataToScreen(model.getXCoord(i), value);
		int y = (value<0.0f ? p1.y : p2.y);
		int width = Math.abs(p2.x-p1.x);
		int height = Math.abs(p2.y-p1.y);
		g.fillRect(p1.x, y, width, height);
		g.setColor(Color.black);
		g.drawRect(p1.x, y, width, height);
        }
        /**
        * Paints the graph (draws the graph axes and data).
        */
        protected void offscreenPaint(Graphics g) {
                // Swing optimised
                final int width = getWidth();
                final int height = getHeight();
                g.setClip(leftAxisPad-scalePad, axisPad-scalePad, width-(leftAxisPad+axisPad-2*scalePad), height-2*(axisPad-scalePad));
                drawData(g);
		g.setClip(null);
                drawAxes(g);
        }
}
