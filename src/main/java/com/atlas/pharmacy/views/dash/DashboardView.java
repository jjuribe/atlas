package com.atlas.pharmacy.views.dash;

import com.atlas.pharmacy.data.service.PRMService;
import com.atlas.pharmacy.views.MainLayout;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.Chart;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.Map;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Atlas")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final PRMService prmService;

    public DashboardView(PRMService prmService) {
        this.prmService = prmService;

        // Set the view settings
        setSizeFull();
        setPadding(true);
        setSpacing(true);

    }
}


