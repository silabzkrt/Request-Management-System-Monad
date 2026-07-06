package org.vaadin.example.domain.supervisor.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.example.domain.developer.model.Developer;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.service.RequestService;
import org.vaadin.example.domain.supervisor.model.Supervisor;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.domain.workflow.model.Workflow;
import org.vaadin.example.domain.workflow.service.WorkflowService;
import org.vaadin.example.shared.enums.RequestStatus;
import org.vaadin.example.shared.enums.WorkStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.vaadin.example.domain.common.view.MainLayout;

@Route(value = "assign_task", layout = MainLayout.class)
@RolesAllowed({"YAZILIM_YONETICISI"})
@PageTitle("Yazılımcıya Görev Ata | Sürükle-Bırak")
public class AssignTaskPage extends VerticalLayout {

    private final RequestService requestService;
    private final UserService userService;
    private final WorkflowService workflowService;

    private VerticalLayout taskContainer;
    private FlexLayout developerContainer;

    public AssignTaskPage(RequestService requestService, UserService userService, WorkflowService workflowService) {
        this.requestService = requestService;
        this.userService = userService;
        this.workflowService = workflowService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f0f2f5");

        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();
        mainContent.setSpacing(true);

        // Sol taraf: Sürüklenebilir Talepler
        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setWidth("35%");
        leftLayout.setHeightFull();
        leftLayout.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("padding", "16px")
            .set("overflow-y", "auto");
            
        leftLayout.add(new H3("Bekleyen Talepler"));
        
        taskContainer = new VerticalLayout();
        taskContainer.setPadding(false);
        taskContainer.setSpacing(true);
        leftLayout.add(taskContainer);

        // Sağ taraf: Yazılımcılar
        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth("65%");
        rightLayout.setHeightFull();
        rightLayout.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("padding", "16px")
            .set("overflow-y", "auto");
            
        rightLayout.add(new H3("Yazılımcılar (Görevi Buraya Bırakın)"));

        developerContainer = new FlexLayout();
        developerContainer.setWidthFull();
        developerContainer.getStyle().set("flex-wrap", "wrap").set("gap", "16px");
        rightLayout.add(developerContainer);

        mainContent.add(leftLayout, rightLayout);
        add(mainContent);

        refreshData();
    }

    private void refreshData() {
        taskContainer.removeAll();
        developerContainer.removeAll();

        // Talepleri Yükle
        List<Request> unassignedReqs = new java.util.ArrayList<>(requestService.findByStatus(RequestStatus.UNASSIGNED));
                
        unassignedReqs.sort((r1, r2) -> {
            Double s1 = r1.getFinalPriorityScore() != null ? r1.getFinalPriorityScore() : 0.0;
            Double s2 = r2.getFinalPriorityScore() != null ? r2.getFinalPriorityScore() : 0.0;
            return s2.compareTo(s1);
        });

        for (Request req : unassignedReqs) {
            taskContainer.add(createTaskCard(req));
        }

        if (unassignedReqs.isEmpty()) {
            taskContainer.add(new Span("Atanacak görev bulunmuyor."));
        }

        // Geliştiricileri Yükle (Bunu Workflow'lardan ÖNCE çekmeliyiz ki Hibernate proxy hatası olmasın)
        List<Developer> developers = userService.findByRole("ROLE_YAZILIMCI").stream()
                .map(u -> (Developer) org.hibernate.Hibernate.unproxy(u))
                .collect(Collectors.toList());

        for (Developer dev : developers) {
            developerContainer.add(createDeveloperCard(dev));
        }
    }

    private String getPriorityColor(Double score) {
        if (score == null) return "#fef0cd";
        if (score >= 1000.0) return "#264653";

        double s = Math.max(1.0, Math.min(100.0, score));
        double hue = 120.0 - ((s - 1.0) / 99.0) * 120.0;
        double lightness = 85.0 - ((s - 1.0) / 99.0) * 20.0;
        return String.format("hsl(%d, 75%%, %d%%)", (int)hue, (int)lightness);
    }

    private VerticalLayout createTaskCard(Request req) {
        VerticalLayout card = new VerticalLayout();
        card.setId("task-" + req.getId());
        card.setWidth("250px");
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
            .set("border", "1px solid #d3d3d3")
            .set("border-radius", "8px")
            .set("background-color", "#ffffff")
            .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)")
            .set("cursor", "grab")
            .set("margin-bottom", "15px")
            .set("overflow", "hidden");

        // Header
        Div header = new Div();
        header.setText("Task ID " + (req != null ? req.getGeneratedId() : "N/A"));
        header.setWidthFull();
        header.getStyle()
            .set("background-color", "#b73a27")
            .set("color", "white")
            .set("text-align", "center")
            .set("padding", "10px 0")
            .set("font-family", "monospace")
            .set("font-size", "16px");

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
            .set("font-family", "monospace")
            .set("font-size", "14px")
            .set("color", (req != null && req.getFinalPriorityScore() != null && req.getFinalPriorityScore() >= 1000.0) ? "white" : "black");

        Div priorityValue = new Div();
        priorityValue.setText(req != null && req.getFinalPriorityScore() != null ? String.valueOf(req.getFinalPriorityScore().intValue()) : "-");
        priorityValue.getStyle()
            .set("font-size", "64px")
            .set("color", "white")
            .set("text-shadow", "1px 1px 3px rgba(0,0,0,0.3)")
            .set("font-family", "sans-serif");

        priorityBox.add(priorityTitle, priorityValue);
        priorityWrapper.add(priorityBox);

        // Details
        VerticalLayout details = new VerticalLayout();
        details.setPadding(true);
        details.setSpacing(false);
        details.getStyle().set("font-family", "monospace").set("font-size", "12px").set("color", "black");

        Div coDiv = new Div(); coDiv.setText("Şirket: " + (req != null && req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyName() : "-"));
        Div typeDiv = new Div(); typeDiv.setText("Talep Tipi: " + (req != null && req.getType() != null ? req.getType().name() : "-"));
        Div emergDiv = new Div(); emergDiv.setText("PO Skoru: " + (req != null && req.getPriority() != null && req.getPriority().getProductMgrScore() != null ? req.getPriority().getProductMgrScore() : "-"));
        Div dateDiv = new Div(); 
        dateDiv.setText("Gönderme Tarihi: " + (req.getCreatedAt() != null ? req.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy")) : "00/00/00"));
        
        coDiv.getStyle().set("margin-bottom", "4px");
        typeDiv.getStyle().set("margin-bottom", "4px");
        emergDiv.getStyle().set("margin-bottom", "4px");

        details.add(coDiv, typeDiv, emergDiv, dateDiv);

        Optional<Workflow> wf = workflowService.findAll().stream().filter(w -> w.getRequest().getId().equals(req.getId())).findFirst();
        if (wf.isPresent() && wf.get().getWorkflowStatus() == WorkStatus.RESENT) {
            Div resentSpan = new Div(); resentSpan.setText("REDDEDİLDİ");
            resentSpan.getStyle().set("color", "red").set("font-weight", "bold").set("text-align", "center").set("padding", "4px").set("width", "100%");
            details.add(resentSpan);
            card.getStyle().set("border", "2px solid #ea4335");
        }

        card.add(header, priorityWrapper, details);

        DragSource<VerticalLayout> dragSource = DragSource.create(card);
        dragSource.setDraggable(true);
        dragSource.addDragStartListener(e -> card.getStyle().set("opacity", "0.5"));
        dragSource.addDragEndListener(e -> card.getStyle().set("opacity", "1"));

        return card;
    }

    private VerticalLayout createDeveloperCard(Developer dev) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("250px");
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
            .set("border", "none")
            .set("border-radius", "8px")
            .set("background", "linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)")
            .set("box-shadow", "0 6px 12px rgba(0,0,0,0.15)")
            .set("margin-bottom", "15px")
            .set("overflow", "hidden");

        Div header = new Div();
        header.setText("DEVELOPER");
        header.setWidthFull();
        header.getStyle()
            .set("background-color", "rgba(0, 0, 0, 0.2)")
            .set("color", "white")
            .set("text-align", "center")
            .set("padding", "10px 0")
            .set("font-family", "sans-serif")
            .set("font-weight", "bold")
            .set("font-size", "14px")
            .set("letter-spacing", "1px");

        VerticalLayout avatarContainer = new VerticalLayout();
        avatarContainer.setAlignItems(Alignment.CENTER);
        avatarContainer.setPadding(true);
        avatarContainer.getStyle().set("padding-top", "20px").set("padding-bottom", "10px");

        Div avatar = new Div();
        avatar.getStyle()
            .set("width", "80px")
            .set("height", "80px")
            .set("border-radius", "50%")
            .set("background-color", "rgba(255, 255, 255, 0.2)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("border", "2px solid rgba(255, 255, 255, 0.5)");
            
        Div avatarInner = new Div();
        avatarInner.getStyle()
            .set("width", "40px")
            .set("height", "40px")
            .set("background-color", "rgba(255, 255, 255, 0.8)")
            .set("border-radius", "50%");
        avatar.add(avatarInner);
        avatarContainer.add(avatar);

        VerticalLayout info = new VerticalLayout();
        info.setPadding(true);
        info.setSpacing(false);
        info.getStyle()
            .set("background-color", "rgba(255, 255, 255, 0.9)")
            .set("color", "#333")
            .set("font-family", "sans-serif")
            .set("font-size", "13px")
            .set("border-radius", "0 0 8px 8px");

        List<Workflow> allWorkflows = workflowService.findAll();
        long activeCount = allWorkflows.stream().filter(w -> w.getDeveloper() != null && w.getDeveloper().getId().equals(dev.getId()) && w.getWorkflowStatus() == WorkStatus.ASSIGNED).count();
        long completedCount = allWorkflows.stream().filter(w -> w.getDeveloper() != null && w.getDeveloper().getId().equals(dev.getId()) && w.getWorkflowStatus() == WorkStatus.RESOLVED).count();

        Div nameDiv = new Div(); nameDiv.setText("Name: " + dev.getNameSurname());
        nameDiv.getStyle().set("font-weight", "bold").set("font-size", "14px").set("margin-bottom", "5px");
        
        Div idDiv = new Div(); idDiv.setText("ID: " + dev.getId());
        idDiv.getStyle().set("margin-bottom", "15px").set("color", "#666");
        
        Div activeDiv = new Div(); activeDiv.setText("Active Tasks: " + activeCount);
        activeDiv.getStyle().set("margin-bottom", "3px");
        Div compDiv = new Div(); compDiv.setText("Completed Tasks: " + completedCount);

        info.add(nameDiv, idDiv, activeDiv, compDiv);
        card.add(header, avatarContainer, info);

        // Drop Target
        DropTarget<VerticalLayout> dropTarget = DropTarget.create(card);
        dropTarget.setActive(true);

        dropTarget.addDropListener(event -> {
            System.out.println("Drop event fired on developer " + dev.getNameSurname());
            event.getDragSourceComponent().ifPresent(draggedComp -> {
                draggedComp.getId().ifPresent(id -> {
                    if (id.startsWith("task-")) {
                        Long reqId = Long.valueOf(id.replace("task-", ""));
                        requestService.findById(reqId).ifPresent(req -> {
                            assignTaskToDeveloper(req, dev);
                        });
                    }
                });
            });
            
            // Eğer verisiz bir event ise
            if (event.getDragSourceComponent().isEmpty()) {
                com.vaadin.flow.component.notification.Notification.show("HATA: Sürüklenen obje tespit edilemedi!", 3000, com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
            }
        });

        return card;
    }

    private void assignTaskToDeveloper(Request req, Developer dev) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByEmail(email).orElse(null);
        Supervisor supervisor = null;
        if (currentUser instanceof Supervisor) {
            supervisor = (Supervisor) currentUser;
        }

        Optional<Workflow> existingWf = workflowService.findByRequestId(req.getId());

        Workflow workflow;
        if (existingWf.isPresent()) {
            workflow = existingWf.get();
            workflowService.assign(workflow, dev, supervisor);
        } else {
            workflow = workflowService.createAndAssignForRequest(req, dev, supervisor);
        }

        com.vaadin.flow.component.notification.Notification.show(req.getTitle() + " görevi " + dev.getNameSurname() + " kullanıcısına başarıyla atandı!", 3000, com.vaadin.flow.component.notification.Notification.Position.TOP_CENTER);

        // Arayüzü güncelle
        refreshData();
    }
    
    @com.vaadin.flow.component.ClientCallable
    public void triggerRefresh() {
        refreshData();
    }
}
