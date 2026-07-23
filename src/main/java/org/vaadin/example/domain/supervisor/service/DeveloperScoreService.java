package org.vaadin.example.domain.supervisor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.example.domain.developer.model.Developer;
import org.vaadin.example.domain.user.dao.UserRepository;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.workflow.dao.WorkflowRepository;
import org.vaadin.example.domain.workflow.model.Workflow;
import org.vaadin.example.shared.enums.WorkStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DeveloperScoreService {

    private final UserRepository userRepository;
    private final WorkflowRepository workflowRepository;

    public DeveloperScoreService(UserRepository userRepository, WorkflowRepository workflowRepository) {
        this.userRepository = userRepository;
        this.workflowRepository = workflowRepository;
    }

    public List<DeveloperPerformanceDTO> calculateAllDeveloperScores() {
        List<User> developers = userRepository.findByRole("ROLE_YAZILIMCI");
        List<DeveloperPerformanceDTO> results = new ArrayList<>();

        for (User user : developers) {
            if (user instanceof Developer dev) {
                DeveloperPerformanceDTO dto = calculateScoreForDeveloper(dev);
                results.add(dto);
            }
        }

        // Sort by success score descending
        results.sort((a, b) -> Double.compare(b.getSuccessScore(), a.getSuccessScore()));
        return results;
    }

    private DeveloperPerformanceDTO calculateScoreForDeveloper(Developer developer) {
        DeveloperPerformanceDTO dto = new DeveloperPerformanceDTO(developer);
        List<Workflow> allWorkflows = workflowRepository.findByDeveloper(developer);
        
        List<Workflow> resolvedWorkflows = new ArrayList<>();
        double totalPriority = 0.0;
        double totalHours = 0.0;
        double totalTaskScore = 0.0;

        for (Workflow wf : allWorkflows) {
            if (wf.getWorkflowStatus() == WorkStatus.RESOLVED && wf.getAssignedAt() != null && wf.getResolvedAt() != null) {
                resolvedWorkflows.add(wf);
                
                double priority = (wf.getRequest() != null && wf.getRequest().getFinalPriorityScore() != null) 
                        ? wf.getRequest().getFinalPriorityScore() 
                        : 0.0;
                        
                double hours = Duration.between(wf.getAssignedAt(), wf.getResolvedAt()).toMinutes() / 60.0;
                if (hours < 0.1) hours = 0.1; // minimum threshold to avoid division by zero
                
                totalPriority += priority;
                totalHours += hours;
                
                // Task score: Priority / Time (capped or weighted as necessary)
                totalTaskScore += (priority / hours);
            }
        }

        int completedCount = resolvedWorkflows.size();
        dto.setCompletedTaskCount(completedCount);
        dto.setPastWorkflows(resolvedWorkflows);

        if (completedCount > 0) {
            dto.setAveragePriorityScore(totalPriority / completedCount);
            dto.setAverageCompletionTimeHours(totalHours / completedCount);
            
            // Ortalama(Görev Priority / Süre) * Tamamlanan Görev Sayısı
            double avgTaskScore = totalTaskScore / completedCount;
            double finalScore = avgTaskScore * completedCount;
            
            // Scale it down to make it readable, maybe divide by 10 or 100
            dto.setSuccessScore(Math.round(finalScore * 10.0) / 100.0);
        } else {
            dto.setAveragePriorityScore(0.0);
            dto.setAverageCompletionTimeHours(0.0);
            dto.setSuccessScore(0.0);
        }

        return dto;
    }
}
