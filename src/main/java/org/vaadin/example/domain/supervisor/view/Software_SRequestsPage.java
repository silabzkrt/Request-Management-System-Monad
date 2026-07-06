package org.vaadin.example.domain.supervisor.view;

/**
 * Yazılım Yöneticisinin gelen talepleri değerlendirdiği ve puanladığı ekran.
 * Sorumluluklar (flowchart):
 *  - Ürün sorumlusundan gelen talepleri listele
 *  - software_mgr_score (1-5) gir → final öncelik skoru hesaplanır
 *  - Puanlanan talepler atama ekranına geçer
 */
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.example.domain.common.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "supervisor_requests", layout = MainLayout.class)
@RolesAllowed("YAZILIM_YONETICISI")
public class Software_SRequestsPage extends VerticalLayout {
    public Software_SRequestsPage() {
        add("Tüm Talepler");
    }
}
