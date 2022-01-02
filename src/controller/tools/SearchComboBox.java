package controller.tools;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;

public class SearchComboBox<T> {

    private SearchComboBoxSkin searchComboBoxSkin;
    private final ComboBox comboBox;
    private final FilteredList<T> filterList;
    private BiPredicate<T, String> filter;

    public SearchComboBox(ComboBox comboBox, boolean search) {
        this(comboBox, FXCollections.observableArrayList(), search);
    }

    public SearchComboBox(ComboBox comboBox, ObservableList<T> items, boolean search) {
        this.comboBox = comboBox;
        this.filterList = new FilteredList<>(items);
        this.filter = (i, s) -> true;
        this.comboBox.setItems(items);
        this.searchComboBoxSkin = new SearchComboBoxSkin(this, search);
        this.comboBox.setSkin(searchComboBoxSkin);
    }

    public void setFilter(BiPredicate<T, String> filter) {
        this.filter = filter;
    }

    public BiPredicate<T, String> getFilter() {
        return filter;
    }

    public void setPredicateFilter(Predicate<T> predicate) {
        filterList.setPredicate(predicate);
    }

    public FilteredList<T> getFilterList() {
        return filterList;
    }

    public SearchComboBoxSkin getSearchComboBoxSkin() {
        return searchComboBoxSkin;
    }

    public ComboBox getComboBox() {
        return comboBox;
    }

}
