package org.vaadin.example.domain.developer.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.example.domain.developer.model.Developer;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.domain.workflow.model.Workflow;
import org.vaadin.example.domain.workflow.service.WorkflowService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.html.Div;
import org.vaadin.example.domain.common.view.MainLayout;
import org.vaadin.example.domain.request.model.Request;

@Route(value = "developer_past", layout = MainLayout.class)
@RolesAllowed("YAZILIMCI")
@PageTitle("Geçmiş İşler | Monad")
public class DeveloperPastRequests extends VerticalLayout {

    private final WorkflowService workflowService;
    private final UserService userService;
    private Developer currentUser;

    private FlexLayout cardContainer;

    public DeveloperPastRequests(WorkflowService workflowService, UserService userService) {
        this.workflowService = workflowService;
        this.userService = userService;

        setSizeFull();
        setPadding(false);

        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userService.findByEmail(email).orElse(null);
        if (u instanceof Developer) {
            currentUser = (Developer) u;
        } else {
            add(new H2("Erişim Reddedildi."));
            return;
        }

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.add(new H2("Geçmiş İşler"));
        content.add(new Span("Tamamlanmış olan görevlerinizi aşağıda görebilirsiniz."));

        cardContainer = new FlexLayout();
        cardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardContainer.getStyle().set("gap", "20px");
        cardContainer.setWidthFull();
        cardContainer.getStyle().set("padding", "10px");

        content.add(cardContainer);

        add(content);
        refreshGrid();
    }

    private String getPriorityColor(Double score) {
        if (score == null) return "#fef0cd";
        if (score >= 1000.0) return "#264653";

        double s = Math.max(1.0, Math.min(100.0, score));
        double hue = 120.0 - ((s - 1.0) / 99.0) * 120.0;
        double lightness = 85.0 - ((s - 1.0) / 99.0) * 20.0;
        return String.format("hsl(%d, 75%%, %d%%)", (int)hue, (int)lightness);
    }

    private VerticalLayout createTaskCard(Workflow wf) {
        Request req = wf.getRequest();
        VerticalLayout card = new VerticalLayout();
        card.setId("wf-" + wf.getId());
        card.setWidth("300px");
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
            .set("border", "1px solid #e0e0e0")
            .set("border-radius", "12px")
            .set("background-color", "#ffffff")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.08)")
            .set("margin-bottom", "15px")
            .set("overflow", "hidden")
            .set("font-family", "Inter, sans-serif");

        // Header
        Div header = new Div();
        header.setText("Task ID " + (req != null ? req.getGeneratedId() : "N/A"));
        header.setWidthFull();
        header.getStyle()
            .set("background-color", "#b73a27")
            .set("color", "white")
            .set("text-align", "center")
            .set("padding", "12px 0")
            .set("font-weight", "600")
            .set("font-size", "15px");

        // Priority Score Box
        VerticalLayout priorityWrapper = new VerticalLayout();
        priorityWrapper.setAlignItems(Alignment.CENTER);
        priorityWrapper.setPadding(true);
        priorityWrapper.setSpacing(false);
        
        VerticalLayout priorityBox = new VerticalLayout();
        priorityBox.setAlignItems(Alignment.CENTER);
        priorityBox.setJustifyContentMode(JustifyContentMode.CENTER);
        String bgColor = getPriorityColor(req != null ? req.getFinalPriorityScore() : null);
        priorityBox.getStyle()
            .set("background-color", bgColor)
            .set("border-radius", "6px")
            .set("padding", "20px 0")
            .set("width", "100%");
            
        Div priorityTitle = new Div();
        priorityTitle.setText("Priority Score");
        priorityTitle.getStyle()
            .set("font-size", "14px")
            .set("font-weight", "500")
            .set("color", (req != null && req.getFinalPriorityScore() != null && req.getFinalPriorityScore() >= 1000.0) ? "white" : "#333");

        Div priorityValue = new Div();
        priorityValue.setText(req != null && req.getFinalPriorityScore() != null ? String.valueOf(req.getFinalPriorityScore().intValue()) : "-");
        priorityValue.getStyle()
            .set("font-size", "56px")
            .set("font-weight", "bold")
            .set("color", "white")
            .set("text-shadow", "1px 1px 3px rgba(0,0,0,0.2)");

        priorityBox.add(priorityTitle, priorityValue);
        priorityWrapper.add(priorityBox);

        // Details
        VerticalLayout details = new VerticalLayout();
        details.setPadding(true);
        details.setSpacing(false);
        details.getStyle().set("font-size", "13px").set("color", "#444");

        Div coDiv = new Div(); coDiv.setText("Şirket: " + (req != null && req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyName() : "-"));
        Div typeDiv = new Div(); typeDiv.setText("Talep Tipi: " + (req != null && req.getType() != null ? req.getType().name() : "-"));
        Div emergDiv = new Div(); emergDiv.setText("PO Skoru: " + (req != null && req.getPriority() != null && req.getPriority().getProductMgrScore() != null ? req.getPriority().getProductMgrScore() : "-"));
        Div dateDiv = new Div(); 
        dateDiv.setText("Gönderme: " + (req != null && req.getCreatedAt() != null ? req.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yy")) : "00/00/00"));
        
        String descText = req != null && req.getDescription() != null ? req.getDescription() : "";
        if (descText.length() > 100) descText = descText.substring(0, 100) + "...";
        Div descDiv = new Div(); descDiv.setText("Açıklama: " + descText);
        descDiv.getStyle().set("margin-top", "6px").set("font-style", "italic");

        coDiv.getStyle().set("margin-bottom", "6px");
        typeDiv.getStyle().set("margin-bottom", "6px");
        emergDiv.getStyle().set("margin-bottom", "6px");

        details.add(coDiv, typeDiv, emergDiv, dateDiv, descDiv);

        // Buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.CENTER);
        actions.setPadding(true);
        actions.getStyle().set("padding-top", "0").set("gap", "6px").set("flex-wrap", "wrap");
        
        Button detailBtn = new Button("Detay");
        detailBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        detailBtn.getStyle().set("background-color", "#3b699e").set("color", "white").set("border-radius", "6px").set("cursor", "pointer").set("font-size", "12px");
        
        actions.add(detailBtn);

        card.add(header, priorityWrapper, details, actions);

        return card;
    }

    private void refreshGrid() {
        if (currentUser == null) return;
        List<Workflow> resolvedWorkflows = workflowService.findResolvedByDeveloper(currentUser);
        cardContainer.removeAll();
        for(Workflow w : resolvedWorkflows) {
            cardContainer.add(createTaskCard(w));
        }
    }
}
