package org.vaadin.example.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.example.domain.priority.model.Priority;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.dao.RequestRepository;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.shared.enums.RequestStatus;
import org.vaadin.example.shared.enums.TaskType;

import java.util.List;
import java.util.Random;

@Component
public class DummyDataRunner implements CommandLineRunner {

    private final RequestRepository requestRepository;
    private final UserService userService;

    public DummyDataRunner(RequestRepository requestRepository, UserService userService) {
        this.requestRepository = requestRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (requestRepository.count() >= 30) {
            return; // Only seed if there are few requests
        }

        List<User> customers = userService.findByRole("ROLE_MUSTERI");
        if (customers.isEmpty()) {
            return;
        }

        Random random = new Random();
        TaskType[] types = TaskType.values();
        
        String[] titles = {
            "Sistem Çökmesi (Nucleus Error 500)",
            "Performans Sorunu - Hastane Modülü",
            "Yeni Fatura Entegrasyonu Talebi",
            "Raporlama Ekranında Veri Uyuşmazlığı",
            "Kullanıcı Yetkilendirme Hatası",
            "Veritabanı Yedekleme Başarısız",
            "Sunucu Bağlantı Hatası (Timeout)",
            "Mobil Uygulama Giriş Yapılamıyor",
            "API Limit Aşımı ve Optimizasyon İhtiyacı",
            "Hasta Kayıt Formu Validasyon Hatası"
        };
        
        String[] descriptions = {
            "Nucleus ana ekranında yoğun kullanım saatlerinde (özellikle sabah 09:00 - 11:00 arası) 500 Internal Server Error alıyoruz. Loglarda memory leak görünüyor, acil incelenmesi lazım.",
            "Raporlama modülünde 1 aylık veri çekerken sistem 30 saniyeden uzun süre yanıt vermiyor ve bazen timeout'a düşüyor. İndekslerin kontrol edilmesi gerekiyor.",
            "Yeni SGK mevzuatına uygun olarak fatura entegrasyonu parametrelerinin güncellenmesi gerekiyor. 1 hafta içinde canlıya alınması şart.",
            "Gece çalışan batch job'lar bazı hasta kayıtlarını çiftliyor (duplicate). Veri kaybı veya yanlış veri oluşumu riskine karşı acil düzeltilmeli.",
            "Doktor rolündeki kullanıcılar, başhekim yetkisi gerektiren ekranlara erişebiliyor. Güvenlik açığı var!",
            "Sistem loglarında sürekli deadlock uyarıları görüyoruz, özellikle randevu alma modülünde aynı anda işlem yapmaya çalışan hastalarda çakışma oluyor.",
            "Kredi kartı ile online ödeme altyapısında 3D secure adımında callback URL yanıt vermediği için ödemeler askıda kalıyor.",
            "Laboratuvar sonuçlarının sisteme işlenmesinde HL7 mesajları parse edilirken NullPointerException alınıyor. Hastalar sonuçlarını göremiyor.",
            "Nucleus'un e-Nabız entegrasyonunda gönderilen bazı aşı verileri reddediliyor. Veri formatının e-Nabız v2 dokümantasyonuna göre güncellenmesi gerek.",
            "Sunucu disk alanı son 3 gündür %95'in üzerinde seyrediyor, log rotasyon mekanizmasının çalışmadığını düşünüyoruz."
        };

        for (int i = 0; i < 40; i++) {
            User creator = customers.get(random.nextInt(customers.size()));
            
            Request req = new Request();
            req.setCreator(creator);
            req.setTitle(titles[random.nextInt(titles.length)] + " - #" + (i+100));
            req.setDescription(descriptions[random.nextInt(descriptions.length)] + "\n\nEk detaylar: Lütfen aciliyetle ilgilenin.");
            req.setType(types[random.nextInt(types.length)]);
            req.setStatus(RequestStatus.PENDING);
            
            Double companyPoints = creator.getCompany() != null && creator.getCompany().getCompanyPoints() != null 
                ? creator.getCompany().getCompanyPoints() 
                : 50.0;
                
            Priority priority = new Priority(req, companyPoints);
            req.setPriority(priority);
            
            requestRepository.save(req);
        }
        
        System.out.println("DummyDataRunner: 40 dummy requests inserted successfully.");
    }
}
