package com.atlas.pharmacy.views.prescription;

import com.atlas.pharmacy.data.entity.Prescription;
import com.atlas.pharmacy.data.service.PRMService;
import com.atlas.pharmacy.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

@PageTitle("Prescription")
@Route(value = "prescription", layout = MainLayout.class)
@PermitAll
public class PrescriptionView extends Div {

    private final PRMService prmService;
    private PrescriptionSearchBar searchBar;
    private Grid<Prescription> prescriptionGrid;
    private GridLazyDataView<Prescription> prescriptionGridListDataView;
    private Grid.Column<Prescription> patientColumn;
    private Grid.Column<Prescription> drugColumn;
    private Grid.Column<Prescription> prescriberColumn;
    private Grid.Column<Prescription> dispenseDateColumn;
    private Grid.Column<Prescription> frequencyColumn;
    private Grid.Column<Prescription> quantityColumn;
    private Grid.Column<Prescription> refillsColumn;
    private Grid.Column<Prescription> daySupplyColumn;
    private Grid.Column<Prescription> totalCostColumn;
    private Grid.Column<Prescription> daysRemainingColumn;
    private Grid.Column<Prescription> nextRefillDateColumn;

    public PrescriptionView(PRMService prmService) {
        this.prmService = prmService;
        addClassName("prescription-view");
        setSizeFull();
        createSearchBar();
        createGrid();
        add(searchBar, prescriptionGrid);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
    }

    private void createGridComponent() {
        prescriptionGrid = new Grid<>();
        prescriptionGrid.setSelectionMode(SelectionMode.MULTI);
        prescriptionGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);
        prescriptionGrid.setHeight("100%");

        updateGridDataProvider("");
    }

    public void updateGridDataProvider(String searchString) {
        prescriptionGridListDataView = prescriptionGrid.setItems(query -> {
            return prmService.getPrescriptionService().list(
                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                    searchString
            ).stream();
        });
    }

    private void createSearchBar() {
        searchBar = new PrescriptionSearchBar();
        searchBar.getSearchField().addValueChangeListener(event -> {
            String searchText = event.getValue();
            updateGridDataProvider(searchText);
        });
        searchBar.getSearchButton().addClickListener(event -> {
            String searchText = searchBar.getSearchField().getValue();
            updateGridDataProvider(searchText);
        });
    }

    private void addColumnsToGrid() {
        createPatientColumn();
        createDrugColumn();
        createPrescriberColumn();
        createDispenseDateColumn();
        createFrequencyColumn();
        createQuantityColumn();
        createRefillsColumn();
        createDaySupplyColumn();
        //createTotalCostColumn();
        createDaysRemainingColumn();
        createNextRefillDateColumn();
    }

    private void createPatientColumn() {
        patientColumn = prescriptionGrid.addColumn(new ComponentRenderer<>(prescription -> {
                    HorizontalLayout hl = new HorizontalLayout();
                    hl.setAlignItems(Alignment.CENTER);
                    Span span = new Span();
                    span.setClassName("name");
                    span.setText(prescription.getPatient().getFullName().orElseThrow());
                    hl.add(span);
                    return hl;
                })).setComparator(prescription -> prescription.getPatient().getFullName().orElseThrow())
                .setHeader("Patient");
    }

    private void createDrugColumn() {
        drugColumn = prescriptionGrid.addColumn(prescription -> prescription.getDrug().getCompleteName().orElseThrow())
                .setComparator(prescription -> prescription.getDrug().getCompleteName().orElseThrow())
                .setHeader("Drug");
    }

    private void createPrescriberColumn() {
        prescriberColumn = prescriptionGrid.addColumn(prescription -> prescription.getPrescriber().getCompleteName().orElseThrow())
                .setComparator(prescription -> prescription.getPrescriber().getCompleteName().orElseThrow())
                .setHeader("Prescriber");
    }

    private void createDispenseDateColumn() {
        dispenseDateColumn = prescriptionGrid.addColumn(Prescription::getDispenseDate)
                .setComparator(Prescription::getDispenseDate)
                .setHeader("Dispense Date")
                .setWidth("180px")
                .setFlexGrow(0);
    }

    private void createFrequencyColumn() {
        frequencyColumn = prescriptionGrid.addColumn(Prescription::getFrequency)
                .setComparator(Prescription::getFrequency)
                .setHeader("Frequency");
    }

    private void createQuantityColumn() {
        quantityColumn = prescriptionGrid.addColumn(Prescription::getQuantity)
                .setComparator(Prescription::getQuantity)
                .setHeader("Quantity");
    }

    private void createRefillsColumn() {
        refillsColumn = prescriptionGrid.addColumn(Prescription::getRefills)
                .setComparator(Prescription::getRefills)
                .setHeader("Refills");
    }

    private void createDaySupplyColumn() {
        daySupplyColumn = prescriptionGrid.addColumn(Prescription::getDaySupplyDuration)
                .setComparator(Prescription::getDaySupplyDuration)
                .setHeader("Day Supply");
    }

//    private void createTotalCostColumn() {
//        totalCostColumn = prescriptionGridPro.addColumn(prescription -> prescription.getTotalCost().orElse(0.0))
//                .setHeader("Total Cost");
//    }

    private void createDaysRemainingColumn() {
        daysRemainingColumn = prescriptionGrid.addColumn(prescription -> prescription.getActualDaysRemaining().orElse(0L))
                .setHeader("Days Left");
    }

    private void createNextRefillDateColumn() {
        nextRefillDateColumn = prescriptionGrid.addColumn(prescription -> prescription.getNextRefillDate().orElse(LocalDate.now()))
                .setHeader("Next Refill Date");
    }
}
