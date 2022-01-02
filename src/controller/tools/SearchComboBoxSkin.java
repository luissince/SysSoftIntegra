package controller.tools;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SearchComboBoxSkin<T> extends ComboBoxListViewSkin {

    private VBox box;
    private final TextField searchBox;
    private final ListView<T> itemView;
    private boolean clickSelection = false;

    public SearchComboBoxSkin(SearchComboBox searchComboBox, boolean search) {
        super(searchComboBox.getComboBox());

        searchBox = new TextField();
        searchBox.getStyleClass().add("text-field-normal");
        searchBox.setPromptText("Ingrese los datos a buscar");
        searchBox.textProperty().addListener((ObservableValue<? extends String> p, String o, String text) -> {
            searchComboBox.setPredicateFilter(item -> text.isEmpty() ? true : searchComboBox.getFilter().test(item, text));
        });

        itemView = new ListView<>();
        itemView.setPrefHeight(240);
        itemView.setItems(searchComboBox.getFilterList());

        // cambia el foco del TextField al ListView usando las teclas ENTER y ESC
        if (search) {
            searchBox.setOnKeyPressed(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    if (!itemView.getItems().isEmpty()) {
                        itemView.getSelectionModel().select(0);
                        itemView.requestFocus();
                    }
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    searchComboBox.getComboBox().hide();
                }
            });
        }

        // se ha hecho click sobre el ListView
        itemView.addEventFilter(MouseEvent.ANY, me -> clickSelection = me.getEventType().equals(MouseEvent.MOUSE_PRESSED));

        searchComboBox.getComboBox().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchComboBox.getComboBox().show();
            }
        });
    }

    @Override
    protected PopupControl getPopup() {
        super.getPopup().setSkin(new Skin<Skinnable>() {
            @Override
            public Skinnable getSkinnable() {
                return null;
            }

            @Override
            public Node getNode() {
                return createPopupContent();
            }

            @Override
            public void dispose() {
            }
        });
        return super.getPopup();
    }

    private Node createPopupContent() {
        box = new VBox(searchBox, itemView);
        box.setSpacing(4.0);
        box.setPadding(new Insets(4.0));
        box.setStyle("-fx-background-color:white;-fx-border-color:#999;-fx-border-width:1px;");
        box.setMinWidth(getSkinnable().getWidth());
        box.setPrefWidth(getSkinnable().getWidth());
        box.setMaxWidth(Control.USE_COMPUTED_SIZE);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        if ("SHOWING".equals(p)) {
            ComboBox<T> scb = ((ComboBox) getSkinnable());
            box.setMinWidth(scb.getWidth());
            box.setPrefWidth(scb.getWidth());
            if (scb.isShowing()) {
                searchBox.clear();
                searchBox.requestFocus();
                itemView.getSelectionModel().select(scb.getValue());
            }
        }
    }

    public ListView<T> getItemView() {
        return itemView;
    }

    public TextField getSearchBox() {
        return searchBox;
    }

    public boolean isClickSelection() {
        return clickSelection;
    }

}
