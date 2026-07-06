package org.vaadin.example.domain.priority.service;

import org.springframework.stereotype.Service;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.priority.model.Priority;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.shared.enums.RequestStatus;

/**
 * Calculates the final priority score for a Request.
 *
 * Formula:
 *   priority_score = (company_points    * 0.30)
 *                  + (customer_rank     * 4.0)
 *                  + (product_mgr_score * 6.0)
 *                  + (software_mgr_score * 6.0)
 *                  + (payment_priority  * 4.0)
 *
 * After calculation, the score is written to both:
 *  - Priority.priorityScore
 *  - Request.finalPriorityScore (for fast sorting without joining priorities table)
 *
 * Both manager scores (product_mgr_score + software_mgr_score) must be present
 * before calling calculate() — check Priority.isReadyForFinalScore() first.
 */
@Service
public class PriorityCalculationService {

    private final UserNotificationService userNotificationService;

    public PriorityCalculationService(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    public static double getTaskTypeMultiplier(org.vaadin.example.shared.enums.TaskType type) {
        if (type == null) return 1.0;
        return switch (type) {
            case SİSTEM_ÇÖKÜŞÜ, VERİ_KAYBI -> 5.0;
            case BUG_DÜZELTME, ÖDEME -> 3.0;
            case ENTEGRASYON_SIKINTILARI -> 2.5;
            case YENİ_ÖZELLİK_EKLEME, GELİŞTİRME -> 1.5;
            default -> 1.0;
        };
    }

    /**
     * Attempts to compute and store the final priority score.
     * If not ready, it simply returns false without throwing.
     */
    public boolean attemptCalculate(Priority priority) {
        if (!priority.isReadyForFinalScore()) {
            return false;
        }
        
        Request request = priority.getRequest();

        if (Boolean.TRUE.equals(priority.getHighestPriority())) {
            priority.setPriorityScore(1000.0);
            if (request != null) {
                request.setFinalPriorityScore(1000.0);
                request.setStatus(RequestStatus.UNASSIGNED);
                userNotificationService.notifySubmitter(request, "onaylandı (En Yüksek Öncelik)");
            }
            return true;
        }
        
        double isEtkisi = priority.getProductMgrScore() != null ? priority.getProductMgrScore() : 1.0;
        double aciliyet = priority.getSoftwareMgrScore() != null ? priority.getSoftwareMgrScore() : 1.0;
        double isTipiKatsayisi = getTaskTypeMultiplier(request != null ? request.getType() : null);
        
        double musteriPuani = priority.getCompanyPoints() != null ? priority.getCompanyPoints() : 0.0;
        double bekleme = priority.getCustomerRank() != null ? priority.getCustomerRank() : 0.0;
        double mudahale = priority.getPaymentPriority() != null ? priority.getPaymentPriority() : 0.0;

        // FORMÜL
        double baseLog = 1 + (Math.log(1 + (musteriPuani / 6.25)) / Math.log(2));
        double bracket = baseLog + bekleme + mudahale;
        
        double score = isEtkisi * aciliyet * isTipiKatsayisi * bracket;

        // Normalize to 1-100
        double normalized = (score / 2000.0) * 100.0;
        if (normalized > 100.0) normalized = 100.0;
        if (normalized < 1.0) normalized = 1.0;

        double rounded = Math.round(normalized * 10.0) / 10.0;

        priority.setPriorityScore(rounded);

        if (request != null) {
            request.setFinalPriorityScore(rounded);
            request.setStatus(RequestStatus.UNASSIGNED);
            userNotificationService.notifySubmitter(request, "onaylandı");
        }
        
        return true;
    }
}
