package com.atlas.pharmacy.views.patient;

import com.atlas.pharmacy.data.entity.Drug;
import com.atlas.pharmacy.data.entity.Patient;
import com.atlas.pharmacy.data.entity.Prescriber;
import com.atlas.pharmacy.data.entity.Prescription;
import com.atlas.pharmacy.data.service.PRMService;
import com.atlas.pharmacy.data.service.PatientService;
import com.atlas.pharmacy.views.MainLayout;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Patient")
@Route(value = "patient/:patientID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class PatientView extends Div implements BeforeEnterObserver {

    public static final String[] OCCUPATIONS = {"Insurance Clerk", "Mortarman", "Beer Coil Cleaner", "Scale Attendant"};
    public static final String[] ROLES = {"Worker", "Supervisor", "Manager", "External"};

    private final String PATIENT_ID = "patientID";
    private final String PATIENT_EDIT_ROUTE_TEMPLATE = "patient/%s/edit";

    private final PRMService prmService;
    private final PatientService patientService;
    private final Filters filters;
    private final Prescriptions prescriptions;

    private Patient patient;
    private final Grid<Patient> grid = new Grid<>(Patient.class, false);
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<Patient> binder;

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private DatePicker dateOfBirth;
    private ComboBox<String> occupation;
    private ComboBox<String> role;
    private TextField streetAddress;
    private TextField province;
    private TextField city;
    private TextField postalCode;
    private TextField allergy;
    private TextField healthCardId;

    public PatientView(PatientService PatientService, PRMService prmService) {
        this.patientService = PatientService;
        this.prmService = prmService;
        //setSizeFull();
        addClassNames("patient-view");

        prescriptions = new Prescriptions(prmService, () -> {
            clearForm();
            refreshGrid();
        });

        filters = new Filters(this::refreshGrid);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid(), createEditorLayout());
        //layout.setSizeFull();
        //layout.setPadding(false);
        //layout.setSpacing(false);
        add(layout);

        // Configure Form
        binder = new BeanValidationBinder<>(Patient.class);
        // Bind fields. This is where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            prescriptions.clear();
            clearForm();
            refreshGrid();
        });

        delete.addClickListener(e -> {
            if (this.patient == null) {
                return;
            }
            binder.removeBean();
            patientService.delete(patient.getId());
            prescriptions.clear();
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> savePatient());
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    private void savePatient() {
        try {
            if (patient == null) {
                patient = new Patient();
            }
            binder.writeBean(patient);
            patientService.update(patient);
            prescriptions.clear();
            clearForm();
            refreshGrid();
            Notification.show("Patient updated successfully!");
            UI.getCurrent().navigate(PatientView.class);
        } catch (ObjectOptimisticLockingFailureException exception) {
            Notification n = Notification.show(
                    "Error updating the Patient. Somebody else has updated the record while you were making changes.");
            n.setPosition(Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ValidationException validationException) {
            Notification.show("Failed to update the patient. Check again that all values are valid");
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<Long> patientId = beforeEnterEvent.getRouteParameters().get(PATIENT_ID).map(Long::parseLong);
        if (patientId.isPresent()) {
            Optional<Patient> patientFromBackEnd = patientService.get(patientId.get());
            if (patientFromBackEnd.isPresent()) {
                populateForm(patientFromBackEnd.get());
            }
            else {
                Notification.show(
                        String.format("The requested patient was not found, ID = %s", patientId.get()), 3000,
                        Notification.Position.BOTTOM_START
                );
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                beforeEnterEvent.forwardTo(PatientView.class);
            }
        }
    }

    public static final class Prescriptions extends VerticalLayout {

        private final PRMService prmService;
        private final Runnable func;
        private final BeanValidationBinder<Prescription> binder;

        private final ListBox<Prescription> listBox;
        private final Button refill;
        private final Button create;

        private final ComboBox<Patient> patient = new ComboBox<>("Patients");
        private final ComboBox<Drug> drug = new ComboBox<>("Drugs");
        private final ComboBox<Prescriber> prescriber = new ComboBox<>("Prescribers");
        private final DatePicker dispenseDate = new DatePicker("Dispense Date");
        private final TextField frequency = new TextField("Frequency");
        private final TextField quantity = new TextField("Quantity");
        private final TextField refills = new TextField("Refills");
        private final TextField daySupplyDuration = new TextField("Day Supply");

        private Prescription prescription;

        public Prescriptions(PRMService prmService, Runnable func) {
            this.prmService = prmService;
            this.func = func;
            this.listBox = new ListBox<>();
            this.binder = new BeanValidationBinder<>(Prescription.class);
            this.binder.bindInstanceFields(this);

            this.listBox.setRenderer(new ComponentRenderer<>(rx -> {
                HorizontalLayout row = new HorizontalLayout();
                row.setAlignItems(FlexComponent.Alignment.CENTER);

                Avatar avatar = new Avatar();
                avatar.setName(String.format("%s", rx.getDrug().getGenericName()));
                //avatar.setImage(rx.getPictureUrl());

                Span name = new Span(rx.getDrug().getGenericName());

                Span days = new Span(String.format("Days (%s)", rx.getActualDaysRemaining().orElse(0L)));
                days.getStyle()
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("font-size", "var(--lumo-font-size-s)");

                Span qty = new Span(String.format("Qty (%s)", rx.getQuantity()));
                qty.getStyle()
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("font-size", "var(--lumo-font-size-s)");

                VerticalLayout column = new VerticalLayout(name, qty, days);
                column.setPadding(false);
                column.setSpacing(false);

                row.add(avatar, column);
                row.getStyle().set("line-height", "var(--lumo-line-height-m)");
                return row;
            }));

            this.refill = new Button("Refill");
            this.create = new Button("New Prescription");

            this.create.addClickListener(event -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("New Prescription");

                VerticalLayout dialogLayout = createNewPrescriptionDialogLayout();
                dialog.add(dialogLayout);

                Button saveButton = createNewPrescriptionDialogSaveButton(dialog);
                Button cancelButton = new Button("Cancel", cancel -> dialog.close());
                dialog.getFooter().add(cancelButton);
                dialog.getFooter().add(saveButton);

                dialog.open();
            });


            add(this.listBox, this.refill, create);
            setSizeFull();
            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            getStyle().set("text-align", "center");
        }

        private VerticalLayout createNewPrescriptionDialogLayout() {
            Patient p = listBox.getListDataView()
                    .getItems()
                    .findFirst()
                    .map(Prescription::getPatient)
                    .orElseThrow();
            patient.setItems(p);
            patient.setValue(p);

            List<Drug> drugs = prmService.getDrugService().findAll();
            drug.setItems(drugs);

            List<Prescriber> prescribers = prmService.getPrescriberService().findAll();
            prescriber.setItems(prescribers);

            VerticalLayout dialogLayout = new VerticalLayout(patient, drug, prescriber, dispenseDate, frequency, quantity, refills, daySupplyDuration);
            dialogLayout.setPadding(false);
            dialogLayout.setSpacing(false);
            dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
            dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

            return dialogLayout;
        }

        private Button createNewPrescriptionDialogSaveButton(Dialog dialog) {
            Button saveButton = new Button("Add", e -> {
                savePrescription();
                clear();
                func.run();
                dialog.close();
            });
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            return saveButton;
        }

        private void savePrescription() {
            try {
                if (prescription == null) {
                    prescription = new Prescription();
                }
                binder.writeBean(prescription);
                prmService.getPrescriptionService().update(prescription);
                Notification.show("Saved prescription!");
                UI.getCurrent().navigate(PatientView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error saving prescription.");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to save the prescription. Check again that all values are valid");
            }
        }

        public void clear() {
            listBox.setItems(Collections.emptyList());
        }

        public void populate(List<Prescription> prescriptions) {
            if (prescriptions == null) {
                return;
            }
            listBox.setItems(prescriptions);
        }
    }

    public static final class Filters extends Div implements Specification<Patient> {

        private final TextField name = new TextField("Name");
        private final TextField phone = new TextField("Phone");
        private final DatePicker startDate = new DatePicker("Date of Birth");
        private final DatePicker endDate = new DatePicker();
        private final MultiSelectComboBox<String> occupations = new MultiSelectComboBox<>("Occupation");
        private final CheckboxGroup<String> roles = new CheckboxGroup<>("Role");

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            name.setPlaceholder("First or last name");

            occupations.setItems(OCCUPATIONS);

            roles.setItems(ROLES);
            roles.addClassName("double-width");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();
                phone.clear();
                startDate.clear();
                endDate.clear();
                occupations.clear();
                roles.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(name, phone, createDateRangeFilter(), occupations, roles, actions);
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");

            endDate.setPlaceholder("To");

            // For screen readers
            setAriaLabel(startDate, "From date");
            setAriaLabel(endDate, "To date");

            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        private void setAriaLabel(DatePicker datePicker, String label) {
            datePicker.getElement().executeJs("const input = this.inputElement;" //
                    + "input.setAttribute('aria-label', $0);" //
                    + "input.removeAttribute('aria-labelledby');", label);
        }

        @Override
        public Predicate toPredicate(Root<Patient> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!name.isEmpty()) {
                String lowerCaseFilter = name.getValue().toLowerCase();
                Predicate firstNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),
                        lowerCaseFilter + "%");
                Predicate lastNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }
            if (!phone.isEmpty()) {
                String databaseColumn = "phone";
                String ignore = "- ()";

                String lowerCaseFilter = ignoreCharacters(ignore, phone.getValue().toLowerCase());
                Predicate phoneMatch = criteriaBuilder.like(
                        ignoreCharacters(ignore, criteriaBuilder, criteriaBuilder.lower(root.get(databaseColumn))),
                        "%" + lowerCaseFilter + "%");
                predicates.add(phoneMatch);

            }
            if (startDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(databaseColumn),
                        criteriaBuilder.literal(startDate.getValue())));
            }
            if (endDate.getValue() != null) {
                String databaseColumn = "dateOfBirth";
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.literal(endDate.getValue()),
                        root.get(databaseColumn)));
            }
            if (!occupations.isEmpty()) {
                String databaseColumn = "occupation";
                List<Predicate> occupationPredicates = new ArrayList<>();
                for (String occupation : occupations.getValue()) {
                    occupationPredicates
                            .add(criteriaBuilder.equal(criteriaBuilder.literal(occupation), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(occupationPredicates.toArray(Predicate[]::new)));
            }
            if (!roles.isEmpty()) {
                String databaseColumn = "role";
                List<Predicate> rolePredicates = new ArrayList<>();
                for (String role : roles.getValue()) {
                    rolePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(role), root.get(databaseColumn)));
                }
                predicates.add(criteriaBuilder.or(rolePredicates.toArray(Predicate[]::new)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }
    }

    private Div createEditorLayout() {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        phone = new TextField("Phone");
        dateOfBirth = new DatePicker("Date of Birth");
        occupation = new ComboBox<>("Occupation");
        role = new ComboBox<>("Role");
        streetAddress = new TextField("Street Address");
        province = new TextField("Province");
        city = new TextField("City");
        postalCode = new TextField("Postal Code");
        allergy = new TextField("Allergy");
        healthCardId = new TextField("Health Card ID");

        occupation.setItems(OCCUPATIONS);
        role.setItems(ROLES);

        Tab profile = new Tab(VaadinIcon.USER.create(), new Span("Profile"));
        Tab work = new Tab(VaadinIcon.COG.create(), new Span("Work"));
        Tab health = new Tab(VaadinIcon.HEART.create(), new Span("Health"));
        Tab prescription = new Tab(VaadinIcon.PILLS.create(), new Span("Prescriptions"));

        TabSheet tabSheet = new TabSheet();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_BORDERED, TabSheetVariant.LUMO_TABS_EQUAL_WIDTH_TABS);
        tabSheet.add(profile, new FormLayout(firstName, lastName, email, phone, dateOfBirth, streetAddress, province, city, postalCode));
        tabSheet.add(work, new FormLayout(occupation, role));
        tabSheet.add(health, new FormLayout(allergy, healthCardId));
        tabSheet.add(prescription, prescriptions);

        editorDiv.add(tabSheet);
        createButtonLayout(editorLayoutDiv);
        editorLayoutDiv.setSizeFull();
        return editorLayoutDiv;
    }

    private Component createGrid() {
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        grid.addColumn("phone").setAutoWidth(true);
        grid.addColumn("dateOfBirth").setAutoWidth(true);
        grid.addColumn("occupation").setAutoWidth(true);
        grid.addColumn("role").setAutoWidth(true);
        grid.addColumn("streetAddress").setAutoWidth(true);
        grid.addColumn("province").setAutoWidth(true);
        grid.addColumn("city").setAutoWidth(true);
        grid.addColumn("postalCode").setAutoWidth(true);
        grid.addColumn("allergy").setAutoWidth(true);
        grid.addColumn("healthCardId").setAutoWidth(true);

        grid.setItems(query ->
                patientService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        // when a row is selected or deselected, populate forms
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Long id = event.getValue().getId();
                UI.getCurrent().navigate(String.format(PATIENT_EDIT_ROUTE_TEMPLATE, id));
                List<Prescription> prescriptionList = prmService.getPrescriptionService()
                        .findAll()
                        .stream()
                        .filter(prescription -> Objects.equals(prescription.getPatient().getId(), id))
                        .toList();
                prescriptions.populate(prescriptionList);
            }
            else {
                prescriptions.clear();
                clearForm();
                UI.getCurrent().navigate(PatientView.class);
            }
        });

        return grid;
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

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Patient value) {
        this.patient = value;
        binder.readBean(this.patient);
    }
}
