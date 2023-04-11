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

    private static final String USER_PICTURE = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png";
    private final PRMService prmService;
    private Grid<Prescription> prescriptionGridPro;
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
        createGrid();
        add(prescriptionGridPro);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
    }

    private void createGridComponent() {
        prescriptionGridPro = new Grid<>();
        prescriptionGridPro.setSelectionMode(SelectionMode.MULTI);
        prescriptionGridPro.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);
        prescriptionGridPro.setHeight("100%");

        prescriptionGridListDataView = prescriptionGridPro.setItems(query -> {
            return prmService.getPrescriptionService().list(
                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))
            ).stream();
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
        createTotalCostColumn();
        createDaysRemainingColumn();
        createNextRefillDateColumn();
    }

    private void createPatientColumn() {
        patientColumn = prescriptionGridPro.addColumn(new ComponentRenderer<>(prescription -> {
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
        drugColumn = prescriptionGridPro.addColumn(prescription -> prescription.getDrug().getCompleteName().orElseThrow())
                .setComparator(prescription -> prescription.getDrug().getCompleteName().orElseThrow())
                .setHeader("Drug");
    }

    private void createPrescriberColumn() {
        prescriberColumn = prescriptionGridPro.addColumn(prescription -> prescription.getPrescriber().getCompleteName().orElseThrow())
                .setComparator(prescription -> prescription.getPrescriber().getCompleteName().orElseThrow())
                .setHeader("Prescriber");
    }

    private void createDispenseDateColumn() {
        dispenseDateColumn = prescriptionGridPro.addColumn(Prescription::getDispenseDate)
                .setComparator(Prescription::getDispenseDate)
                .setHeader("Dispense Date")
                .setWidth("180px")
                .setFlexGrow(0);
    }

    private void createFrequencyColumn() {
        frequencyColumn = prescriptionGridPro.addColumn(Prescription::getFrequency)
                .setComparator(Prescription::getFrequency)
                .setHeader("Frequency");
    }

    private void createQuantityColumn() {
        quantityColumn = prescriptionGridPro.addColumn(Prescription::getQuantity)
                .setComparator(Prescription::getQuantity)
                .setHeader("Quantity");
    }

    private void createRefillsColumn() {
        refillsColumn = prescriptionGridPro.addColumn(Prescription::getRefills)
                .setComparator(Prescription::getRefills)
                .setHeader("Refills");
    }

    private void createDaySupplyColumn() {
        daySupplyColumn = prescriptionGridPro.addColumn(Prescription::getDaySupplyDuration)
                .setComparator(Prescription::getDaySupplyDuration)
                .setHeader("Day Supply");
    }

    private void createTotalCostColumn() {
        totalCostColumn = prescriptionGridPro.addColumn(prescription -> prescription.getTotalCost().orElse(0.0))
                .setHeader("Total Cost");
    }

    private void createDaysRemainingColumn() {
        daysRemainingColumn = prescriptionGridPro.addColumn(prescription -> prescription.getActualDaysRemaining().orElse(0L))
                .setHeader("Days Left");
    }

    private void createNextRefillDateColumn() {
        nextRefillDateColumn = prescriptionGridPro.addColumn(prescription -> prescription.getNextRefillDate().orElse(LocalDate.now()))
                .setHeader("Next Refill Date");
    }
}
