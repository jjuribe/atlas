package com.atlas.pharmacy.views.prescription;

import com.atlas.pharmacy.data.entity.Prescription;
import com.atlas.pharmacy.data.service.PRMService;
import com.atlas.pharmacy.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;

@PageTitle("Prescription")
@Route(value = "prescription", layout = MainLayout.class)
@PermitAll
public class PrescriptionView extends Div {

    private static final String USER_PICTURE = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png";

    private GridPro<Prescription> prescriptionGridPro;
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

    private final PRMService prmService;

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
        addFiltersToGrid();
    }

    private void createGridComponent() {
        prescriptionGridPro = new GridPro<>();
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
            Image img = new Image(USER_PICTURE, "");
            Span span = new Span();
            span.setClassName("name");
            span.setText(prescription.getPatient().getFullName().orElseThrow());
            hl.add(img, span);
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
        frequencyColumn = prescriptionGridPro.addEditColumn(Prescription::getFrequency)
                .text(Prescription::setFrequency)
                .setComparator(Prescription::getFrequency)
                .setHeader("Frequency");
    }

    private void createQuantityColumn() {
        quantityColumn = prescriptionGridPro.addEditColumn(Prescription::getQuantity)
                .text((item, newValue) -> item.setQuantity(Double.parseDouble(newValue)))
                .setComparator(Prescription::getQuantity)
                .setHeader("Quantity");
    }

    private void createRefillsColumn() {
        refillsColumn = prescriptionGridPro.addEditColumn(Prescription::getRefills)
                .text((item, newValue) -> item.setRefills(Integer.parseInt(newValue)))
                .setComparator(Prescription::getRefills)
                .setHeader("Refills");
    }

    private void createDaySupplyColumn() {
        daySupplyColumn = prescriptionGridPro.addEditColumn(Prescription::getDaySupplyDuration)
                .text((item, newValue) -> item.setDaySupplyDuration(Integer.parseInt(newValue)))
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

//    private void createStatusColumn() {
//        statusColumn = grid.addEditColumn(Client::getClient, new ComponentRenderer<>(client -> {
//            Span span = new Span();
//            span.setText(client.getStatus());
//            span.getElement().setAttribute("theme", "badge " + client.getStatus().toLowerCase());
//            return span;
//        })).select((item, newValue) -> item.setStatus(newValue), Arrays.asList("Pending", "Success", "Error"))
//                .setComparator(client -> client.getStatus()).setHeader("Status");
//    }

    private void addFiltersToGrid() {
//        HeaderRow filterRow = grid.appendHeaderRow();
//
//        TextField clientFilter = new TextField();
//        clientFilter.setPlaceholder("Filter");
//        clientFilter.setClearButtonVisible(true);
//        clientFilter.setWidth("100%");
//        clientFilter.setValueChangeMode(ValueChangeMode.EAGER);
//        clientFilter.addValueChangeListener(event -> gridListDataView
//                .addFilter(client -> StringUtils.containsIgnoreCase(client.getClient(), clientFilter.getValue())));
//        filterRow.getCell(clientColumn).setComponent(clientFilter);
//
//        TextField amountFilter = new TextField();
//        amountFilter.setPlaceholder("Filter");
//        amountFilter.setClearButtonVisible(true);
//        amountFilter.setWidth("100%");
//        amountFilter.setValueChangeMode(ValueChangeMode.EAGER);
//        amountFilter.addValueChangeListener(event -> gridListDataView.addFilter(client -> StringUtils
//                .containsIgnoreCase(Double.toString(client.getAmount()), amountFilter.getValue())));
//        filterRow.getCell(amountColumn).setComponent(amountFilter);
//
//        ComboBox<String> statusFilter = new ComboBox<>();
//        statusFilter.setItems(Arrays.asList("Pending", "Success", "Error"));
//        statusFilter.setPlaceholder("Filter");
//        statusFilter.setClearButtonVisible(true);
//        statusFilter.setWidth("100%");
//        statusFilter.addValueChangeListener(
//                event -> gridListDataView.addFilter(client -> areStatusesEqual(client, statusFilter)));
//        filterRow.getCell(statusColumn).setComponent(statusFilter);
//
//        DatePicker dateFilter = new DatePicker();
//        dateFilter.setPlaceholder("Filter");
//        dateFilter.setClearButtonVisible(true);
//        dateFilter.setWidth("100%");
//        dateFilter.addValueChangeListener(
//                event -> gridListDataView.addFilter(client -> areDatesEqual(client, dateFilter)));
//        filterRow.getCell(dateColumn).setComponent(dateFilter);
    }

    private boolean areStatusesEqual(Client client, ComboBox<String> statusFilter) {
        String statusFilterValue = statusFilter.getValue();
        if (statusFilterValue != null) {
            return StringUtils.equals(client.getStatus(), statusFilterValue);
        }
        return true;
    }

    private boolean areDatesEqual(Client client, DatePicker dateFilter) {
        LocalDate dateFilterValue = dateFilter.getValue();
        if (dateFilterValue != null) {
            LocalDate clientDate = LocalDate.parse(client.getDate());
            return dateFilterValue.equals(clientDate);
        }
        return true;
    }

    private List<Client> getClients() {
        return Arrays.asList(
                createClient(4957, "https://randomuser.me/api/portraits/women/42.jpg", "Amarachi Nkechi", 47427.0,
                        "Success", "2019-05-09"),
                createClient(675, "https://randomuser.me/api/portraits/women/24.jpg", "Bonelwa Ngqawana", 70503.0,
                        "Success", "2019-05-09"),
                createClient(6816, "https://randomuser.me/api/portraits/men/42.jpg", "Debashis Bhuiyan", 58931.0,
                        "Success", "2019-05-07"),
                createClient(5144, "https://randomuser.me/api/portraits/women/76.jpg", "Jacqueline Asong", 25053.0,
                        "Pending", "2019-04-25"),
                createClient(9800, "https://randomuser.me/api/portraits/men/24.jpg", "Kobus van de Vegte", 7319.0,
                        "Pending", "2019-04-22"),
                createClient(3599, "https://randomuser.me/api/portraits/women/94.jpg", "Mattie Blooman", 18441.0,
                        "Error", "2019-04-17"),
                createClient(3989, "https://randomuser.me/api/portraits/men/76.jpg", "Oea Romana", 33376.0, "Pending",
                        "2019-04-17"),
                createClient(1077, "https://randomuser.me/api/portraits/men/94.jpg", "Stephanus Huggins", 75774.0,
                        "Success", "2019-02-26"),
                createClient(8942, "https://randomuser.me/api/portraits/men/16.jpg", "Torsten Paulsson", 82531.0,
                        "Pending", "2019-02-21"));
    }

    private Client createClient(int id, String img, String client, double amount, String status, String date) {
        Client c = new Client();
        c.setId(id);
        c.setImg(img);
        c.setClient(client);
        c.setAmount(amount);
        c.setStatus(status);
        c.setDate(date);

        return c;
    }
}
