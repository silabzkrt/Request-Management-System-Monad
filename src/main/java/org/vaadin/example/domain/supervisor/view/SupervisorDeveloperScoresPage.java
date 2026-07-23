package org.vaadin.example.domain.supervisor.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.example.domain.common.view.MainLayout;
import org.vaadin.example.domain.supervisor.service.DeveloperPerformanceDTO;
import org.vaadin.example.domain.supervisor.service.DeveloperScoreService;
import org.vaadin.example.domain.workflow.model.Workflow;

import java.util.List;

@Route(value = "supervisor_developer_scores", layout = MainLayout.class)
@RolesAllowed("YAZILIM_YONETICISI")
@PageTitle("Yazılımcı Performansları | Supervisor")
public class SupervisorDeveloperScoresPage extends VerticalLayout {

    private final DeveloperScoreService scoreService;

    public SupervisorDeveloperScoresPage(DeveloperScoreService scoreService) {
        this.scoreService = scoreService;

        setSizeFull();
        getStyle().set("background", "linear-gradient(to bottom, #ffffff 40%, #7da2cc 100%)");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        
        Button backBtn = new Button("Geri Dön", e -> UI.getCurrent().navigate(Software_SMainPage.class));
        backBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        H2 title = new H2("Yazılımcı Başarı Sıralaması");
        
        header.add(backBtn, title);
        header.expand(title);

        add(header);

        List<DeveloperPerformanceDTO> scores = scoreService.calculateAllDeveloperScores();

        Grid<DeveloperPerformanceDTO> grid = new Grid<>();
        grid.setItems(scores);
        grid.getStyle().set("background", "rgba(255, 255, 255, 0.8)").set("border-radius", "8px");

        grid.addColumn(dto -> scores.indexOf(dto) + 1).setHeader("Sıra").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(dto -> dto.getDeveloper() != null && dto.getDeveloper().getNameSurname() != null ? dto.getDeveloper().getNameSurname() : "Bilinmeyen Yazılımcı").setHeader("Yazılımcı Adı").setAutoWidth(true);
        grid.addColumn(DeveloperPerformanceDTO::getSuccessScore).setHeader("Başarı Puanı").setAutoWidth(true);

        grid.addItemClickListener(event -> {
            openDeveloperDetails(event.getItem(), scores.indexOf(event.getItem()) + 1);
        });

        add(grid);
    }

    private void openDeveloperDetails(DeveloperPerformanceDTO dto, int rank) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setMaxHeight("90vh");
        
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        
        String devName = dto.getDeveloper() != null && dto.getDeveloper().getNameSurname() != null ? dto.getDeveloper().getNameSurname() : "Bilinmeyen Yazılımcı";
        H3 title = new H3(devName + " - Performans Detayları (Sıra: " + rank + ")");
        
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        statsLayout.add(new Span("Tamamlanan Görev Sayısı: " + dto.getCompletedTaskCount()));
        statsLayout.add(new Span("Ortalama Öncelik Skoru: " + String.format("%.1f", dto.getAveragePriorityScore())));
        statsLayout.add(new Span("Ort. Çözüm Süresi (Saat): " + String.format("%.1f", dto.getAverageCompletionTimeHours())));
        
        statsLayout.getStyle()
                .set("background-color", "#f8f9fa")
                .set("padding", "15px")
                .set("border-radius", "5px")
                .set("font-weight", "500")
                .set("margin-bottom", "15px");
                
        H3 gridTitle = new H3("Tamamladığı Geçmiş Görevler");
        gridTitle.getStyle().set("font-size", "1.2em");
        
        Grid<Workflow> wfGrid = new Grid<>(Workflow.class, false);
        wfGrid.setItems(dto.getPastWorkflows());
        wfGrid.addColumn(wf -> wf.getRequest() != null ? wf.getRequest().getGeneratedId() : "-").setHeader("Görev ID").setAutoWidth(true);
        wfGrid.addColumn(wf -> wf.getRequest() != null ? wf.getRequest().getTitle() : "-").setHeader("Başlık").setAutoWidth(true);
        wfGrid.addColumn(wf -> wf.getRequest() != null && wf.getRequest().getFinalPriorityScore() != null ? wf.getRequest().getFinalPriorityScore() : "-").setHeader("Priority Skoru").setAutoWidth(true);
        wfGrid.addColumn(wf -> wf.getAssignedAt() != null ? wf.getAssignedAt().toString().replace("T", " ").substring(0, 16) : "-").setHeader("Atanma").setAutoWidth(true);
        wfGrid.addColumn(wf -> wf.getResolvedAt() != null ? wf.getResolvedAt().toString().replace("T", " ").substring(0, 16) : "-").setHeader("Çözülme").setAutoWidth(true);
        
        Button closeBtn = new Button("Kapat", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        content.add(title, statsLayout, gridTitle, wfGrid);
        dialog.add(content);
        dialog.getFooter().add(closeBtn);
        
        dialog.open();
    }
}
