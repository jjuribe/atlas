package com.atlas.pharmacy.views.prescriber;

import com.atlas.pharmacy.data.entity.Prescriber;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

import static com.atlas.pharmacy.api.UITool.createButtonLayout;

@PageTitle("Prescriber")
@Route(value = "prescriber/:prescriberID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class PrescriberView extends Div implements BeforeEnterObserver {

    private final String PRESCRIBER_ID = "prescriberID";
    private final String PRESCRIBER_EDIT_ROUTE_TEMPLATE = "prescriber/%s/edit";

    private final PRMService prmService;
    private final BeanValidationBinder<Prescriber> binder;
    private final Grid<Prescriber> grid = new Grid<>(Prescriber.class, false);
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private TextField licenseIdentificationNumber;
    private TextField practice;
    private TextField firstName;
    private TextField lastName;
    private TextField officeAddress;
    private TextField postalCode;
    private Prescriber prescriber;

    public PrescriberView(PRMService prmService) {
        this.prmService = prmService;
        addClassName("prescriber-view");

        setSizeFull();

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        createGrid(splitLayout);
        createEditor(splitLayout);
        add(splitLayout);

        grid.addColumn("licenseIdentificationNumber").setAutoWidth(true);
        grid.addColumn("practice").setAutoWidth(true);
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("officeAddress").setAutoWidth(true);
        grid.addColumn("postalCode").setAutoWidth(true);

        grid.setItems(query -> prmService.getPrescriberService().list(
                        PageRequest.of(
                                query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)
                        )
                ).stream()
        );

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PRESCRIBER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            }
            else {
                clearForm();
                UI.getCurrent().navigate(PrescriberView.class);
            }
        });

        binder = new BeanValidationBinder<>(Prescriber.class);
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        delete.addClickListener(e -> {
            if (prescriber == null) {
                return;
            }
            binder.removeBean();
            prmService.getPrescriberService().delete(prescriber.getId());
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (prescriber == null) {
                    prescriber = new Prescriber();
                }
                binder.writeBean(prescriber);
                prmService.getPrescriberService().update(prescriber);
                clearForm();
                refreshGrid();
                Notification.show("Prescriber updated successfully!");
                UI.getCurrent().navigate(PrescriberView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the Prescriber. Somebody else has updated the record while you were making changes.");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the Prescriber. Check again that all values are valid");
            }
        });
    }

    private void createGrid(SplitLayout layout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        layout.addToPrimary(wrapper);
        wrapper.setWidthFull();
        wrapper.add(grid);
    }

    private void createEditor(SplitLayout layout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();

        licenseIdentificationNumber = new TextField("License Number");
        practice = new TextField("Practice");
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        officeAddress = new TextField("Office Address");
        postalCode = new TextField("Postal Code");

        formLayout.add(licenseIdentificationNumber, practice, firstName, lastName, officeAddress, postalCode);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv, delete, cancel, save);
        layout.addToSecondary(editorLayoutDiv);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Prescriber value) {
        this.prescriber = value;
        binder.readBean(this.prescriber);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> prescriberId = event.getRouteParameters().get(PRESCRIBER_ID).map(Long::parseLong);
        if (prescriberId.isEmpty()) {
            return;
        }
        Optional<Prescriber> prescriberFromBackEnd = prmService.getPrescriberService().get(prescriberId.get());
        if (prescriberFromBackEnd.isPresent()) {
            populateForm(prescriberFromBackEnd.get());
        }
        else {
            Notification.show(
                    String.format("The requested prescriber was not found, ID = %s", prescriberId.get()), 3000,
                    Notification.Position.BOTTOM_START
            );
            refreshGrid();
            event.forwardTo(PrescriberView.class);
        }
    }
}
