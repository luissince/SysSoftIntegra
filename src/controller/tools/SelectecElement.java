package controller.tools;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class SelectecElement {

    private final List<Node> selection = new ArrayList<>();

    public SelectecElement() {

    }

    public void clear() {
        for (int i = 0; i < selection.size(); i++) {
            Group group = (Group) selection.get(i);
            for (int j = 0; j < group.getChildren().size(); j++) {
                if (group.getChildren().get(j) instanceof Region) {
                    Region region = (Region) group.getChildren().get(j);
                    if (region.getId().equalsIgnoreCase("rcon")) {
                        group.getChildren().remove(region);
                        break;
                    }
                }
            }
        }
    }

    public void add(Node node, double width, double height) {
        Group group = (Group) node;
        Region region = new Region();
        region.setId("rcon");
        region.setPrefSize(width, height);
        region.setStyle("-fx-border-color:green;-fx-border-width:2;");

        if (selection.contains(node)) {
            group.getChildren().add(region);
        } else {
            group.getChildren().add(region);
            selection.add(group);
        }

    }

}
