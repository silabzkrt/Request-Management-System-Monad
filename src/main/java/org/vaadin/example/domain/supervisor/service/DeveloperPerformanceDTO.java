package org.vaadin.example.domain.supervisor.service;

import org.vaadin.example.domain.developer.model.Developer;
import org.vaadin.example.domain.workflow.model.Workflow;
import java.util.List;

public class DeveloperPerformanceDTO {
    private Developer developer;
    private int completedTaskCount;
    private double averagePriorityScore;
    private double averageCompletionTimeHours;
    private double successScore;
    private List<Workflow> pastWorkflows;

    public DeveloperPerformanceDTO(Developer developer) {
        this.developer = developer;
    }

    public Developer getDeveloper() { return developer; }
    public int getCompletedTaskCount() { return completedTaskCount; }
    public void setCompletedTaskCount(int completedTaskCount) { this.completedTaskCount = completedTaskCount; }
    public double getAveragePriorityScore() { return averagePriorityScore; }
    public void setAveragePriorityScore(double averagePriorityScore) { this.averagePriorityScore = averagePriorityScore; }
    public double getAverageCompletionTimeHours() { return averageCompletionTimeHours; }
    public void setAverageCompletionTimeHours(double averageCompletionTimeHours) { this.averageCompletionTimeHours = averageCompletionTimeHours; }
    public double getSuccessScore() { return successScore; }
    public void setSuccessScore(double successScore) { this.successScore = successScore; }
    public List<Workflow> getPastWorkflows() { return pastWorkflows; }
    public void setPastWorkflows(List<Workflow> pastWorkflows) { this.pastWorkflows = pastWorkflows; }
}
