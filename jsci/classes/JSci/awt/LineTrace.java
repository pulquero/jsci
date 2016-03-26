package JSci.awt;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import JSci.maths.ExtraMath;

/**
* A line trace AWT component.
* @version 1.4
* @author Mark Hale
*/
public final class LineTrace extends DoubleBufferedCanvas {
        /**
        * Data points.
        */
        private final List dataPoints = new ArrayList();
        /**
        * Sampling interval.
        */
        private float samplingInterval;
        /**
        * Axis numbering.
        */
        private boolean numbering=true;
        private NumberFormat xNumberFormat = new DecimalFormat("##0.##");
        private NumberFormat yNumberFormat = new DecimalFormat("##0.##");
        /**
        * Origin.
        */
        private Point origin=new Point();
        /**
        * Min and max data points.
        */
        private float minX,minY,maxX,maxY;
        /**
        * Axis scaling.
        */
        private float xScale,yScale;
        private final float xIncPixels = 40.0f;
        private final float yIncPixels = 40.0f;
        /**
        * Padding.
        */
        private final int scalePad=5;
        private final int axisPad=25;
        private int leftAxisPad;
        /**
        * Constructs a line trace.
        */
        public LineTrace(float minx,float maxx,float miny,float maxy) {
                addMouseMotionListener(new MouseLineAdapter());
                setXExtrema(minx,maxx);
                setYExtrema(miny,maxy);
                setSamplingInterval(0.2f);
        }
        /**
        * Gets the data sampled by this line trace.
        */
        public Graph2DModel getModel() {
                final Point2DListModel model=new Point2DListModel();
                model.setData(dataPoints);
                return model;
        }
        /**
        * Turns axis numbering on/off.
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
        * Sets the minimum/maximum values on the x-axis.
        */
        public void setXExtrema(float min,float max) {
                if(max<min)
                        throw new IllegalArgumentException("Maximum should be greater than minimum; max = "+max+" and min = "+min);
                minX=min;
                maxX=max;
                rescale();
        }
        /**
        * Sets the minimum/maximum values on the y-axis.
        */
        public void setYExtrema(float min,float max) {
                if(max<min)
                        throw new IllegalArgumentException("Maximum should be greater than minimum; max = "+max+" and min = "+min);
                minY=min;
                maxY=max;
                rescale();
        }
        /**
        * Sets the sampling interval.
        * Smaller values give a more accurate trace, but more susceptible to mouse noise.
        */
        public void setSamplingInterval(float interval) {
                samplingInterval = interval;
        }
        /**
        * Clears the current trace.
        */
        public void clear() {
                dataPoints.clear();
                redraw();
        }
        /**
        * Reshapes the line trace to the specified bounding box.
        */
        public final void setBounds(int x,int y,int width,int height) {
                super.setBounds(x,y,width,height);
                rescale();
        }
        /**
        * Rescales the line trace.
        */
        private void rescale() {
                final Dimension s=getMinimumSize();
                final int thisWidth=Math.max(getSize().width,s.width);
                final int thisHeight=Math.max(getSize().height,s.height);
                xScale = (float) ((double)(thisWidth-(leftAxisPad+axisPad)) / (double)(maxX-minX));
                yScale = (float) ((double)(thisHeight-2*axisPad) / (double)(maxY-minY));
                origin.x=leftAxisPad-Math.round(minX*xScale);
                origin.y=thisHeight-axisPad+Math.round(minY*yScale);
                redraw();
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
                return new Dimension(200,200);
        }
        /**
        * Converts a screen point to data coordinates.
        */
        private Point2D.Float screenToData(Point p) {
                double x = (double)(p.x-origin.x) / (double)xScale;
                double y = (double)(origin.y-p.y) / (double)yScale;
                return new Point2D.Float((float)x, (float)y);
        }
        /**
        * Converts a data point to screen coordinates.
        */
        private Point dataToScreen(Point2D.Float p) {
                return new Point(origin.x+Math.round(xScale*p.x),origin.y-Math.round(yScale*p.y));
        }
        private Point dataToScreen(float x, float y) {
                return new Point(origin.x+Math.round(xScale*x),origin.y-Math.round(yScale*y));
        }
        /**
        * Paint the trace.
        */
        protected void offscreenPaint(Graphics g) {
                drawAxes(g);
// lines
                Point p1,p2;
                Iterator iter = dataPoints.iterator();
                if(iter.hasNext()) {
                        p1 = dataToScreen((Point2D.Float) iter.next());
                        while(iter.hasNext()) {
                                p2 = dataToScreen((Point2D.Float) iter.next());
                                g.drawLine(p1.x, p1.y, p2.x, p2.y);
                                p1 = p2;
                        }
                }
        }
        /**
        * Draws the graph axes.
        */
        private void drawAxes(Graphics g) {
// axis
                g.setColor(getForeground());
                if(minY>0.0f)
                        g.drawLine(leftAxisPad-scalePad,getSize().height-axisPad,getSize().width-(axisPad-scalePad),getSize().height-axisPad);
                else
                        g.drawLine(leftAxisPad-scalePad,origin.y,getSize().width-(axisPad-scalePad),origin.y);
                if(minX>0.0f)
                        g.drawLine(leftAxisPad,axisPad-scalePad,leftAxisPad,getSize().height-(axisPad-scalePad));
                else
                        g.drawLine(origin.x,axisPad-scalePad,origin.x,getSize().height-(axisPad-scalePad));
// numbering
                if(numbering) {
                        final FontMetrics metrics=g.getFontMetrics();
                        final int strHeight=metrics.getHeight();
// x-axis numbering
                        float dx = (float) ExtraMath.round((double)xIncPixels/(double)xScale, 1);
                        if(dx == 0.0f)
                                dx = Float.MIN_VALUE;
                        for(double x=(minX>0.0f)?minX:dx; x<=maxX; x+=dx) {
                                String str = xNumberFormat.format((float)x);
// add a + prefix to compensate for - prefix in negative number strings when calculating length
                                int strWidth=metrics.stringWidth('+'+str);
                                Point p=dataToScreen((float)x, (minY>0.0f)?minY:0.0f);
                                g.drawLine(p.x,p.y,p.x,p.y+5);
                                g.drawString(str,p.x-strWidth/2,p.y+5+strHeight);
                        }
                        for(double x=-dx; x>=minX; x-=dx) {
                                String str = xNumberFormat.format((float)x);
                                int strWidth=metrics.stringWidth(str);
                                Point p=dataToScreen((float)x, (minY>0.0f)?minY:0.0f);
                                g.drawLine(p.x,p.y,p.x,p.y+5);
                                g.drawString(str,p.x-strWidth/2,p.y+5+strHeight);
                        }
// y-axis numbering
                        float dy = (float) ExtraMath.round((double)yIncPixels/(double)yScale, 1);
                        if(dy == 0.0f)
                                dy = Float.MIN_VALUE;
                        for(double y=(minY>0.0f)?minY:dy; y<=maxY; y+=dy) {
                                String str = yNumberFormat.format((float)y);
                                int strWidth=metrics.stringWidth(str);
                                Point p=dataToScreen((minX>0.0f)?minX:0.0f, (float)y);
                                g.drawLine(p.x,p.y,p.x-5,p.y);
                                g.drawString(str,p.x-8-strWidth,p.y+strHeight/4);
                        }
                        for(double y=-dy; y>=minY; y-=dy) {
                                String str = yNumberFormat.format((float)y);
                                int strWidth=metrics.stringWidth(str);
                                Point p=dataToScreen((minX>0.0f)?minX:0.0f, (float)y);
                                g.drawLine(p.x,p.y,p.x-5,p.y);
                                g.drawString(str,p.x-8-strWidth,p.y+strHeight/4);
                        }
                }
        }
        class MouseLineAdapter extends MouseMotionAdapter {
                public void mouseDragged(MouseEvent evt) {
                        Point2D.Float p=screenToData(evt.getPoint());
                        final int i = dataPoints.size()-1;
                        if(p.x >= i*samplingInterval+minX && p.x <= maxX) {
                                dataPoints.add(p);
                        }
                        redraw();
                }
        }
}
