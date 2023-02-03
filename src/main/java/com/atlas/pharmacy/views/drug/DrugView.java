package com.atlas.pharmacy.views.drug;

import com.atlas.pharmacy.data.entity.Drug;
import com.atlas.pharmacy.data.service.DrugService;
import com.atlas.pharmacy.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

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
    private final BeanValidationBinder<Drug> binder;
    private final DrugService drugService;
    private TextField drugIdentificationNumber;
    private TextField dosage;
    private TextField manufacturer;
    private TextField brandName;
    private TextField genericName;
    private TextField description;
    private TextField form;
    private TextField unitCost;
    private TextField stockQuantity;
    private Drug drug;

    public DrugView(DrugService drugService) {
        this.drugService = drugService;
        addClassNames("drug-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("drugIdentificationNumber").setAutoWidth(true);
        grid.addColumn("dosage").setAutoWidth(true);
        grid.addColumn("manufacturer").setAutoWidth(true);
        grid.addColumn("brandName").setAutoWidth(true);
        grid.addColumn("genericName").setAutoWidth(true);
        grid.addColumn("description").setAutoWidth(true);
        grid.addColumn("form").setAutoWidth(true);
        grid.addColumn("unitCost").setAutoWidth(true);
        grid.addColumn("stockQuantity").setAutoWidth(true);

        grid.setItems(query ->
                drugService.list(
                                PageRequest.of(
                                        query.getPage(),
                                        query.getPageSize(),
                                        VaadinSpringDataHelpers.toSpringDataSort(query)
                                )
                        )
                        .stream()
        );

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(DRUG_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            }
            else {
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
           drugService.delete(drug.getId());
           clearForm();
           refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.drug == null) {
                    this.drug = new Drug();
                }
                binder.writeBean(this.drug);
                drugService.update(this.drug);
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
            Optional<Drug> drugFromBackEnd = drugService.get(drugId.get());
            if (drugFromBackEnd.isPresent()) {
                populateForm(drugFromBackEnd.get());
            }
            else {
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

        drugIdentificationNumber = new TextField("DIN");
        dosage = new TextField("Dosage");
        manufacturer = new TextField("Manufacturer");
        brandName = new TextField("Brand Name");
        genericName = new TextField("Generic Name");
        description = new TextField("Description");
        form = new TextField("Form");
        unitCost = new TextField("Unit Cost");
        stockQuantity = new TextField("Stock Quantity");

        formLayout.add(drugIdentificationNumber, dosage, manufacturer, brandName, genericName, description, form, unitCost, stockQuantity);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel, delete);
        editorLayoutDiv.add(buttonLayout);
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
}
