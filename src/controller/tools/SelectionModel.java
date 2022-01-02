
package controller.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Group;
import javafx.scene.Node;

public class SelectionModel {

    Group selectionLayer;
    Map<Node, SelectionOverlay> map = new HashMap<>();

    List<Node> selection = new ArrayList<>();
    
    Node nodeSelection;

    public SelectionModel() {
        
    }
    
    public void setGroup(Group layoutBoundsOverlay){
        selectionLayer = layoutBoundsOverlay;
    }

    public void add( Node cell) {
        nodeSelection = cell;
        // don't add duplicates or else duplicates might be added to the javafx scene graph which causes exceptions
        if( selection.contains(cell))
            return;

        SelectionOverlay selectionOverlay = new SelectionOverlay( cell);

        // register
        map.put( cell, selectionOverlay);

        // add component
        selectionLayer.getChildren().add(selectionOverlay);

        selection.add( cell);
        
    }

    public void remove( Node cell) {

        removeOverlay( cell);

        selection.remove( cell);

    }

    private void removeOverlay( Node cell) {
        SelectionOverlay boundsDisplay = map.get( cell);
        if( boundsDisplay != null) {
            selectionLayer.getChildren().remove(boundsDisplay);
        }
    }

    public void clear() {

        // remove style
        // we can't call remove here because we'd get a ConcurrentModificationException (or we could use an iterator)
        selection.forEach((cell) -> {
            removeOverlay( cell);
        });

        // clear selection list
        selection.clear();
    }

    public boolean isEmpty()  {
        return selection.isEmpty();
    }

    public boolean contains( Node cell) {
        return selection.contains( cell);
    }

    public List<Node> getSelection() {
        return selection;
    }

    public Node getNodeSelection() {
        return nodeSelection;
    }

    

   
}