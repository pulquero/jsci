package JSci.swing;

import java.awt.*;
import JSci.awt.*;

/**
* A layered bar graph Swing component.
* Multiple series are layered.
* @version 1.2
* @author Mark Hale
*/
public class JLayeredBarGraph extends JBarGraph {
        /**
        * Constructs a layered bar graph.
        */
        public JLayeredBarGraph(CategoryGraph2DModel gm) {
                super(gm);
        }
        /**
        * Draws the graph bars.
        */
        protected void drawBars(Graphics g) {
// bars
                int numSeries=1;
                model.firstSeries();
                while(model.nextSeries())
                        numSeries++;
                if(numSeries==1) {
                        for(int i=0;i<model.seriesLength();i++) {
                                drawBar(g, i, model.getValue(i), barColor[0]);
                        }
                } else {
                        float seriesValue[]=new float[numSeries];
                        Color seriesColor[]=new Color[numSeries];
                        for(int i=0;i<model.seriesLength();i++) {
                                model.firstSeries();
                                for(int j=0;j<numSeries;j++) {
                                        seriesValue[j]=model.getValue(i);
                                        seriesColor[j]=barColor[j];
                                        model.nextSeries();
                                }
                                // sort
                                float val;
                                Color col;
                                for(int k,j=1;j<numSeries;j++) {
                                        val=seriesValue[j];
                                        col=seriesColor[j];
                                        for(k=j-1;k>=0 && seriesValue[k]<val;k--) {
                                                seriesValue[k+1]=seriesValue[k];
                                                seriesColor[k+1]=seriesColor[k];
                                        }
                                        seriesValue[k+1]=val;
                                        seriesColor[k+1]=col;
                                }
                                // draw
                                for(int j=0;j<numSeries;j++)
                                        drawBar(g, i, seriesValue[j], seriesColor[j]);
                        }
                }
        }
        /**
        * Draws a bar.
        */
        private void drawBar(Graphics g, int pos, float value, Color color) {
                Point p;
                if(value<0.0f)
                        p=dataToScreen(pos,0.0f);
                else
                        p=dataToScreen(pos,value);
                g.setColor(color);
                final int dy=Math.abs(p.y-origin.y);
                g.fillRect(p.x+barPad, p.y, barWidth, dy);
                g.setColor(Color.black);
                g.drawRect(p.x+barPad, p.y, barWidth, dy);
        }
}

