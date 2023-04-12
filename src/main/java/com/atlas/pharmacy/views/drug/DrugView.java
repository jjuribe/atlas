package com.atlas.pharmacy.views.drug;

import com.atlas.pharmacy.api.UITool;
import com.atlas.pharmacy.data.entity.Drug;
import com.atlas.pharmacy.data.service.PRMService;
import com.atlas.pharmacy.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

@PageTitle("Drug")
@Route(value = "drug/:drugID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class DrugView extends Div implements BeforeEnterObserver {

    private final String DRUG_ID = "drugID";
    private final String DRUG_EDIT_ROUTE_TEMPLATE = "drug/%s/edit";

    private final Grid<Drug> grid = new Grid<>(Drug.class, false);
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private TextField searchField;
    private Button searchButton;

    private final BeanValidationBinder<Drug> binder;
    private final PRMService prmService;

    private TextField drug_code;
    private TextField dosage_unit;
    private TextField dosage_value;
    private TextField ingredient_name;
    private TextField strength;
    private TextField strength_unit;

    private Drug drug;

    public DrugView(PRMService prmService) {
        this.prmService = prmService;
        addClassNames("drug-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("drug_code").setAutoWidth(true);
        grid.addColumn("dosage_unit").setAutoWidth(true);
        grid.addColumn("dosage_value").setAutoWidth(true);
        grid.addColumn("ingredient_name").setAutoWidth(true);
        grid.addColumn("strength").setAutoWidth(true);
        grid.addColumn("strength_unit").setAutoWidth(true);

        grid.setItems(prmService.getDrugService().fetchDrugs());

//        grid.setItems(query ->
//                prmService.getDrugService().list(
//                                PageRequest.of(
//                                        query.getPage(),
//                                        query.getPageSize(),
//                                        VaadinSpringDataHelpers.toSpringDataSort(query)
//                                )
//                        )
//                        .stream()
//        );

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(DRUG_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(DrugView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Drug.class);
        // Bind fields. This is where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        delete.addClickListener(e -> {
            if (this.drug == null) {
                return;
            }
            binder.removeBean();
            prmService.getDrugService().delete(drug.getId());
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.drug == null) {
                    this.drug = new Drug();
                }
                binder.writeBean(this.drug);
                prmService.getDrugService().update(this.drug);
                clearForm();
                refreshGrid();
                Notification.show("Drug updated successfully!");
                UI.getCurrent().navigate(DrugView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the drug. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the drug. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> drugId = event.getRouteParameters().get(DRUG_ID).map(Long::parseLong);
        if (drugId.isPresent()) {
            Optional<Drug> drugFromBackEnd = prmService.getDrugService().get(drugId.get());
            if (drugFromBackEnd.isPresent()) {
                populateForm(drugFromBackEnd.get());
            } else {
                Notification.show(
                        String.format("The requested drug was not found, ID = %s", drugId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(DrugView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();

        searchField = new TextField("Search by drug code or ingredient name");
        searchButton = new Button("Search");
        searchButton.addClickListener(e -> searchDrugs());
        formLayout.add(searchField, searchButton);

        drug_code = new TextField("Drug Code");
        dosage_unit = new TextField("Dosage Unit");
        dosage_value = new TextField("Dosage value");
        ingredient_name = new TextField("Ingredient Name");
        strength = new TextField("Strength");
        strength_unit = new TextField("Strength Unit");

        formLayout.add(drug_code, dosage_unit, dosage_value, ingredient_name, strength, strength_unit);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        UITool.createButtonLayout(editorLayoutDiv, delete, cancel, save);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Drug value) {
        this.drug = value;
        binder.readBean(this.drug);
    }

    private void searchDrugs() {
        String query = searchField.getValue();
        if (query.isEmpty()) {
            grid.setItems(prmService.getDrugService().fetchDrugs());
        } else {
            grid.setItems(prmService.getDrugService().searchDrugs(query));
        }
    }
}
