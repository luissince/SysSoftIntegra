package controller.tools;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

public class MouseHandler {

    private final SelectionModel selectionModel;

    private Text textReferent;

    private ComboBox<String> cbFuente;

    private Spinner<Double> spFontSize;

    public MouseHandler(SelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }
    
    private class DragContext {

        double x;
        double y;
    }

    public void makeDraggable(final Node node) {

        final DragContext dragDelta = new DragContext();
        this.textReferent = (Text) node;

        node.setOnMousePressed(mouseEvent -> {

            // TODO: add shift & ctrl check to add/remove nodes to selection
            selectionModel.clear();

            // add to selection model, create drag handles
            selectionModel.add(node);

            cbFuente.getSelectionModel().select(textReferent.getFont().getName());
            spFontSize.getValueFactory().setValue(textReferent.getFont().getSize());

            dragDelta.x = node.getTranslateX() - mouseEvent.getSceneX();
            dragDelta.y = node.getTranslateY() - mouseEvent.getSceneY();

            // consume event, so that scene won't get it (which clears selection)
            mouseEvent.consume();
        });

        node.setOnMouseDragged(mouseEvent -> {

            node.setTranslateX(mouseEvent.getSceneX() + dragDelta.x);
            node.setTranslateY(mouseEvent.getSceneY() + dragDelta.y);

        });

        node.setOnMouseReleased(mouseEvent -> {

            fixPosition(node);

        });
    }

    private void fixPosition(Node node) {

        double x = node.getTranslateX();
        double y = node.getTranslateY();

        node.relocate(node.getLayoutX() + x, node.getLayoutY() + y);

        node.setTranslateX(0);
        node.setTranslateY(0);

    }
}
