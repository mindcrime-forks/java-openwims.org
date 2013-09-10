/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.openwims.Objects.WIM;
import org.openwims.Objects.WIMFrame;
import org.openwims.Objects.WIMRelation;

/**
 *
 * @author jesse
 */
public class WIMsGraphJPanel extends JPanel {
    
    private WIM wim;

    public WIMsGraphJPanel(WIM wim) {
        this.wim = wim;
        this.setLayout(new GridLayout(1, 1));

        final mxGraph graph = new WIMsGraph();
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        style(graph);
    
        try {
            HashMap<WIMFrame, Object> vertexes = new HashMap();
            
            for (WIMFrame frame : wim.listFrames()) {
                Object vertex = graph.insertVertex(parent, null, frame.getName(), 20, 20, 0, 0);
                vertexes.put(frame, vertex);
            }
            
            for (WIMFrame frame : wim.listFrames()) {
                for (WIMRelation relation : frame.listRelations()) {
                    graph.insertEdge(parent, null, relation.getRelation(), vertexes.get(frame), vertexes.get(relation.getFrame()));
                }
            }
            
//            Object v1 = graph.insertVertex(parent, null, "Hello asdf asf ddd", 20, 20, 0, 0, "ROUNDED");
//            Object v2 = graph.insertVertex(parent, null, "World!", 240, 150, 80, 30);
//            Object v3 = graph.insertVertex(parent, null, "World!a", 240, 150, 80, 30);
//            Object v4 = graph.insertVertex(parent, null, "World!v", 240, 150, 80, 30);
//            Object v5 = graph.insertVertex(parent, null, "World!b", 240, 150, 80, 30);
//            
//            graph.insertEdge(parent, null, "Edge", v1, v2);
//            graph.insertEdge(parent, null, "Edge", v1, v3);
//            graph.insertEdge(parent, null, "Edge", v3, v4);
//            graph.insertEdge(parent, null, "Edge", v4, v5);
            
            
//            graph.updateCellSize(v1);
//            graph.updateCellSize(v2);
            
            for (Object vertex : vertexes.values()) {
                graph.updateCellSize(vertex);
            }
            
        } finally {
            graph.getModel().endUpdate();
        }
        
        
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setConnectable(false);
        
        //graphComponent.setEnabled(false);
        
        // define layout
        mxIGraphLayout layout = new mxFastOrganicLayout(graph);

        // layout using morphing
        graph.getModel().beginUpdate();
        try {
            layout.execute(graph.getDefaultParent());
        } finally {
            mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

            morph.addListener(mxEvent.DONE, new mxEventSource.mxIEventListener() {

                @Override
                public void invoke(Object arg0, mxEventObject arg1) {
                    graph.getModel().endUpdate();
                    // fitViewport();
                }

            });

            morph.startAnimation();
        }

        graphComponent.setBackground(Color.WHITE);
        this.add(graphComponent);
    }
    
    private void style(mxGraph graph) {
        
        //Style the nodes
        HashMap<String, Object> vertexStyle = new HashMap<String, Object>();     
        vertexStyle.put(mxConstants.STYLE_SHAPE,          mxConstants.SHAPE_RECTANGLE);
        vertexStyle.put(mxConstants.STYLE_ROUNDED,        true);
        vertexStyle.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        vertexStyle.put(mxConstants.STYLE_FILLCOLOR,      "#F0E6D5");
        vertexStyle.put(mxConstants.STYLE_FONTSTYLE,      mxConstants.FONT_BOLD);
        vertexStyle.put(mxConstants.STYLE_STROKECOLOR,    "#CFC0A7");
        vertexStyle.put(mxConstants.STYLE_STROKEWIDTH,    2);
        vertexStyle.put(mxConstants.STYLE_PERIMETER,      mxPerimeter.RectanglePerimeter);

        mxSwingConstants.VERTEX_SELECTION_STROKE = new Stroke() {
            public Shape createStrokedShape(Shape shape) {
                return new Rectangle();
            }
        };

        //Style the edges
        Map<String, Object> edgeStyle = new HashMap<String, Object>();
        edgeStyle.put(mxConstants.STYLE_ROUNDED,        true);
        edgeStyle.put(mxConstants.STYLE_SHAPE,          mxConstants.SHAPE_CONNECTOR);
        edgeStyle.put(mxConstants.STYLE_ENDARROW,       mxConstants.ARROW_CLASSIC);
        edgeStyle.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        edgeStyle.put(mxConstants.STYLE_ALIGN,          mxConstants.ALIGN_CENTER);
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR,    "#997979");
        edgeStyle.put(mxConstants.STYLE_FONTCOLOR,      "#523939");
        edgeStyle.put(mxConstants.STYLE_FONTSTYLE,      mxConstants.FONT_BOLD);

        
        
        mxStylesheet stylesheet = new mxStylesheet();
        stylesheet.setDefaultEdgeStyle(edgeStyle);
        stylesheet.setDefaultVertexStyle(vertexStyle);

        graph.setStylesheet(stylesheet);
        graph.setCellsResizable(false);
    }
    
    
    
    private class WIMsGraph extends mxGraph {

        @Override
        public boolean isCellSelectable(Object cell) {
            mxCell c = (mxCell) cell;
            if (c.isEdge()) {
                return false;
            }
            
            return super.isCellSelectable(cell); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
}
