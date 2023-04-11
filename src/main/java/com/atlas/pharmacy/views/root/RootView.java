package com.atlas.pharmacy.views.root;

import com.atlas.pharmacy.views.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Root")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class RootView extends VerticalLayout {

    public RootView() {
        setSpacing(false);

        Image img = new Image("icons/icon.png", "atlas-greek-god");
        img.setWidth("200px");
        add(img);

        add(new H2("Atlas Pharmaceuticals"));
        add(new Paragraph("Simply built with Java ☕ and ❤️"));

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
