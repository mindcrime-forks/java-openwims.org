/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openwims.UI;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author jesse
 */
public class FileDropJPanel extends JPanel {

    private DropTarget dropTarget;
    private DropTargetHandler dropTargetHandler;
    private boolean dragOver = false;
    
    private LinkedList<FilesDraggedListener> listeners;

    public FileDropJPanel() {
        this.listeners = new LinkedList();
    }
    
    public void addFilesDraggedListener(FilesDraggedListener listener) {
        this.listeners.add(listener);
    }

    protected DropTarget getMyDropTarget() {
        if (dropTarget == null) {
            dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null);
        }
        return dropTarget;
    }

    protected DropTargetHandler getDropTargetHandler() {
        if (dropTargetHandler == null) {
            dropTargetHandler = new DropTargetHandler();
        }
        return dropTargetHandler;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        try {
            getMyDropTarget().addDropTargetListener(getDropTargetHandler());
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dragOver) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 255, 0, 64));
            g2d.fill(new Rectangle(getWidth(), getHeight()));
            g2d.dispose();
        }
    }

    protected void importFiles(final List files) {
        Runnable run = new Runnable() {

            @Override
            public void run() {
                for (FilesDraggedListener listener : listeners) {
                    listener.filesDraggedEvent(files);
                }
            }
        };
        SwingUtilities.invokeLater(run);
    }

    protected class DropTargetHandler implements DropTargetListener {

        protected void processDrag(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            processDrag(dtde);
            SwingUtilities.invokeLater(new DragUpdate(true));
            repaint();
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            processDrag(dtde);
            SwingUtilities.invokeLater(new DragUpdate(true));
            repaint();
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            SwingUtilities.invokeLater(new DragUpdate(false));
            repaint();
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {

            SwingUtilities.invokeLater(new DragUpdate(false));

            Transferable transferable = dtde.getTransferable();
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(dtde.getDropAction());
                try {

                    List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (transferData != null && transferData.size() > 0) {
                        importFiles(transferData);
                        dtde.dropComplete(true);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                dtde.rejectDrop();
            }
        }
    }

    public class DragUpdate implements Runnable {

        private boolean dragOver;

        public DragUpdate(boolean dragOver) {
            this.dragOver = dragOver;
        }

        @Override
        public void run() {
            FileDropJPanel.this.dragOver = dragOver;
            FileDropJPanel.this.repaint();
        }
    }
    
    public interface FilesDraggedListener {
        public void filesDraggedEvent(List<File> files);
    }
}
