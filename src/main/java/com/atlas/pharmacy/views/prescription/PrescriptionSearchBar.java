package com.atlas.pharmacy.views.prescription;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class PrescriptionSearchBar extends HorizontalLayout {

    private TextField searchField;
    private Button searchButton;

    public PrescriptionSearchBar() {
        setSpacing(true);
        setAlignItems(Alignment.END);
        createSearchField();
        createSearchButton();
    }

    private void createSearchField() {
        searchField = new TextField();
        searchField.setPlaceholder("Search by patient name...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        add(searchField);
    }

    private void createSearchButton() {
        searchButton = new Button("Search", VaadinIcon.SEARCH.create());
        add(searchButton);
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }
}
