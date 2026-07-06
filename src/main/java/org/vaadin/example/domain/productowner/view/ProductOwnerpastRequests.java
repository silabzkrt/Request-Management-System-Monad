package org.vaadin.example.domain.productowner.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.example.domain.common.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.example.domain.priority.model.Priority;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.service.RequestNoteService;
import org.vaadin.example.domain.request.view.components.RequestNoteComponent;
import org.vaadin.example.domain.request.service.RequestService;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import com.vaadin.flow.component.dependency.CssImport;

import org.vaadin.example.domain.common.view.MainLayout;

@Route(value = "po_past_requests", layout = MainLayout.class)
@CssImport("./styles/styles.css")
@CssImport(value = "./styles/grid-styles.css", themeFor = "vaadin-grid")
@RolesAllowed("URUN_SORUMLUSU")
@PageTitle("Geçmiş Talepler | PO")
public class ProductOwnerpastRequests extends VerticalLayout {

    private final RequestService requestService;
    private final RequestNoteService requestNoteService;
    private final UserService userService;
    private final org.vaadin.example.domain.priority.service.PriorityCalculationService priorityCalculationService;
    private User currentUser;
    private Grid<Request> requestGrid;

    public ProductOwnerpastRequests(RequestService requestService, RequestNoteService requestNoteService, UserService userService, org.vaadin.example.domain.priority.service.PriorityCalculationService priorityCalculationService) {
        this.requestService = requestService;
        this.requestNoteService = requestNoteService;
        this.userService = userService;
        this.priorityCalculationService = priorityCalculationService;

        setSizeFull();
        getStyle().set("background", "linear-gradient(to bottom, #ffffff 40%, #7da2cc 100%)");

        loadCurrentUser();

        if (currentUser == null) {
            add(new H2("Kullanıcı bulunamadı."));
            return;
        }

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        
        Button backBtn = new Button("Geri Dön", e -> UI.getCurrent().navigate(ProductOwnerMainPage.class));
        backBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        H2 title = new H2("Değerlendirdiğim Talepler (Geçmiş)");
        
        header.add(backBtn, title);
        header.expand(title);

        requestGrid = new Grid<>(Request.class, false);
        requestGrid.getStyle().set("background", "rgba(255, 255, 255, 0.8)").set("border-radius", "8px");

        requestGrid.addColumn(req -> req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyName() : "-").setHeader("Şirket").setAutoWidth(true);
        requestGrid.addColumn(Request::getTitle).setHeader("Başlık").setAutoWidth(true);
        requestGrid.addColumn(req -> req.getType() != null ? req.getType().name() : "-").setHeader("Tip").setAutoWidth(true);
        
        requestGrid.addColumn(new com.vaadin.flow.data.renderer.ComponentRenderer<>(req -> {
            Span statusSpan = new Span(req.getStatus() != null ? req.getStatus().name() : "UNKNOWN");
            statusSpan.getStyle().set("padding", "4px 8px").set("border-radius", "4px").set("font-weight", "600");
            statusSpan.getStyle().set("background-color", "#e2e3e5").set("color", "#383d41");
            return statusSpan;
        })).setHeader("Durum").setAutoWidth(true);

        requestGrid.addColumn(req -> {
            Priority p = req.getPriority();
            return p != null && p.getProductMgrScore() != null ? p.getProductMgrScore().toString() : "-";
        }).setHeader("Verdiğim Puan").setAutoWidth(true);
        
        requestGrid.addComponentColumn(req -> {
            Button detailsBtn = new Button("Detay / Not Oku", e -> openDetailsDialog(req));
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            return detailsBtn;
        }).setHeader("İşlem").setAutoWidth(true);

        updateGrid();

        add(header, requestGrid);
    }

    private void loadCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.findByEmail(email);
        userOpt.ifPresent(user -> this.currentUser = user);
    }

    private void updateGrid() {
        requestGrid.setItems(requestService.findPastForPO());
    }

    private void openDetailsDialog(Request reqArg) {
        Request req = requestService.findByIdWithDetails(reqArg.getId()).orElse(reqArg);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Geçmiş Talep Detayları (ID: " + req.getId() + ")");
        dialog.setWidth("800px");
        dialog.setMaxHeight("90vh");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setWidth("50%");
        detailsLayout.getStyle().set("background", "#f8f9fa").set("padding", "15px").set("border-radius", "8px");

        detailsLayout.add(new H3("Talep Bilgileri"));
        detailsLayout.add(new Div(new Span("Başlık: "), new Span(req.getTitle())));
        detailsLayout.add(new Div(new Span("Müşteri: "), new Span(req.getCreator() != null ? req.getCreator().getNameSurname() : "-")));
        detailsLayout.add(new Div(new Span("Durum: "), new Span(req.getStatus() != null ? req.getStatus().name() : "-")));
        
        Priority p = req.getPriority();
        detailsLayout.add(new H3("Puanlama Durumu"));
        detailsLayout.add(new Div(new Span("PO Puanı: "), new Span(p != null && p.getProductMgrScore() != null ? p.getProductMgrScore().toString() : "-")));
        detailsLayout.add(new Div(new Span("Supervisor Puanı: "), new Span(p != null && p.getSoftwareMgrScore() != null ? p.getSoftwareMgrScore().toString() : "-")));
        detailsLayout.add(new Div(new Span("Nihai Puan: "), new Span(p != null && p.getPriorityScore() != null ? p.getPriorityScore().toString() : "Hesaplanmadı")));


        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth("50%");
        rightLayout.setPadding(false);

        // Notlar Kısmı (Bileşen ile)
        RequestNoteComponent noteComponent = new RequestNoteComponent(requestNoteService, req, currentUser);

        if (req.getStatus() == org.vaadin.example.shared.enums.RequestStatus.REJECTED || req.getStatus() == org.vaadin.example.shared.enums.RequestStatus.UNASSIGNED) {
            VerticalLayout scoringArea = new VerticalLayout();
            scoringArea.setPadding(false);
            scoringArea.getStyle().set("margin-top", "20px");

            com.vaadin.flow.component.textfield.IntegerField customerRankField = new com.vaadin.flow.component.textfield.IntegerField("Customer Rank (Örn: 1-10)");
            customerRankField.setValue(p != null ? p.getCustomerRank() : null);

            com.vaadin.flow.component.textfield.IntegerField productMgrScoreField = new com.vaadin.flow.component.textfield.IntegerField("Product Manager Score (1-5)");
            productMgrScoreField.setMin(1);
            productMgrScoreField.setMax(5);
            productMgrScoreField.setValue(p != null ? p.getProductMgrScore() : null);

            com.vaadin.flow.component.textfield.NumberField paymentPriorityField = new com.vaadin.flow.component.textfield.NumberField("Payment Priority");
            paymentPriorityField.setValue(p != null ? p.getPaymentPriority() : null);

            scoringArea.add(new H3("Yeniden Puanlama"), customerRankField, productMgrScoreField, paymentPriorityField);
            
            Button updateScoreBtn = new Button("Puanı Güncelle", e -> {
                if (productMgrScoreField.getValue() == null || productMgrScoreField.getValue() < 1 || productMgrScoreField.getValue() > 5) {
                    Notification.show("Product Manager Score 1 ile 5 arasında olmalıdır!");
                    return;
                }
                Priority prio = req.getPriority();
                if (prio == null) {
                    prio = new Priority(req, req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyPoints() : null);
                    req.setPriority(prio);
                }
                prio.setCustomerRank(customerRankField.getValue());
                prio.setProductMgrScore(productMgrScoreField.getValue());
                prio.setPaymentPriority(paymentPriorityField.getValue());
                
                if (req.getStatus() == org.vaadin.example.shared.enums.RequestStatus.REJECTED) {
                    req.setStatus(org.vaadin.example.shared.enums.RequestStatus.PENDING);
                }

                priorityCalculationService.attemptCalculate(prio);
                requestService.save(req);
                Notification.show("Puan başarıyla güncellendi.");
                updateGrid();
                dialog.close();
            });
            updateScoreBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(updateScoreBtn);

            rightLayout.add(new H3("Notlar (Herkes Görebilir)"), noteComponent, scoringArea);
        } else {
            rightLayout.add(new H3("Notlar (Herkes Görebilir)"), noteComponent);
        }

        mainLayout.add(detailsLayout, rightLayout);

        dialog.add(mainLayout);

        Button closeBtn = new Button("Kapat", e -> dialog.close());
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }
}
