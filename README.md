# Request Management System - Monad

This project was developed during my 20-day mandatory internship at **Monad Software and Consulting**, located in Hacettepe Teknokent. During my internship, I designed and implemented a Request Management System to address the company's need for a structured way to manage and prioritize software requests coming from the various hospitals they work with.

The system architecture and user roles were specifically designed to reflect the company's internal hierarchy and workflow:

- **Admin**: Approves new users and companies registering in the system.
- **Product Owner**: Approves or denies incoming requests. Evaluates the urgency, product importance, and customer relevance to calculate an initial priority score.
- **Software Supervisor**: Assesses the software impact to finalize the priority score, and assigns approved requests to developers.
- **Developers**: Complete and update the status of the tasks assigned to them.
- **Customers**: Submit new requests, add notes/attachments, and track the progress of their requests.

## How the System Works

```mermaid
flowchart TD
    %% --- RENK VE STİL TANIMLAMALARI ---
    classDef islem fill:#3498db,color:#ffffff,stroke:#2980b9,stroke-width:2px;
    classDef karar fill:#e67e22,color:#ffffff,stroke:#d35400,stroke-width:2px;
    classDef verigirisi fill:#8e30da,color:#ffffff,stroke:#46044a,stroke-width:2px;
    
    U101(Uygulama Açılır) --> U102{Daha önce giriş yapmış bir kullanıcı var mı?}:::karar
    U102 --> |evet| U103[Ana sayfaya devam et]:::islem
    U102 --> |hayır| U104{Kullanıcının hesabı var mı?}:::karar
    U104 --> |evet| U105[Login ekranına devam et]:::islem
    U105 --> U145[/Email ve şifre gir/]:::verigirisi
    U145 --> U106{Girilen Mail ve Şifre Sistemdeki Bir Kullanıcı İle Uyuşuyor mu?}:::karar
    U106 --> |evet| U103
    U106 --> |hayır| U107{{Şifre ve Kullanıcı Adı Hatalı}} 
    U107 --> U108{Kullanıcı Şifre ya da Mailini Bilmiyor Mu?}:::karar
    U108 --> |evet| U105
    U108 --> |hayır| U109["Şifremi Hatırlamıyorum?" seçeneğini işaretle"]:::islem
    U109 --> U110[/Kullanıcı mail adresini girer/]:::verigirisi
    U110 --> U111[Kullanıcı Mail Adresine Gelen Adrese Gider]:::islem
    U111 --> U112[/Yeni Şifre Girilir/]:::verigirisi
    U112 --> U113{Şifre İstenilen Kriterlere Uyuyor Mu?}:::karar
    U113 --> |evet| U105
    U113 --> |hayır| U114{{Şifre Hatalı}}
    U114 --> U112
    
    U104 --> |hayır| U115[Registration ekranına devam et]:::islem
    U115 --> U116[/Kullanıcı mail, şifre, ad soyad ve şirket ID'sini girer/]
    U116 --> U117{Bu maile sahip sistemde bir kullanıcı var mı?}:::karar
    U117 --> |evet| U118{Kullanıcı gerçekten yeni bir kullanıcı mı?}:::karar
    U118 --> |evet| U119[/Yeni mail gir/]:::verigirisi
    U119 --> U116
    U118 --> |hayır| U123[Bu mail adresi zaten kullanımda]:::islem --> U105
    U117 --> |hayır| U120[Doğrulama ekranına gelir]:::islem
    U120 --> U121[Kullanıcı admin tarafından doğrulananana kadar bu ekranda kalır]:::islem
    U121 --> U122[Admin tarafından doğrulanır]:::islem
    U122 --> U103
    
    U103 --> U124((.))
    U124 --> U201{Kullanıcı türü ne?}:::karar
    
    U201 --> |Admin| U202[Admin ana sayfası açılır]:::islem
    U201 --> |Customer| U203[Müşteri ana sayfası açılır]:::islem
    U201 --> |Ürün Sorumlusu| U204[Ürün Sorumlusu ana sayfası açılır]:::islem
    U201 --> |Yönetici| U205[Yönetici ana sayfası açılır]:::islem
    U201 --> |Yazılımcı| U206[Yazılımcı ana sayfası açılır]:::islem
    
    U202 --> U207((.))
    U207 --> U208[Kullanıcı Yöneticisini Aç]:::islem
    U207 --> U209[Talep Yönetim Sistemi Hataları]:::islem
    U207 --> U210[Profil Ayarlarını Aç]:::islem
    U207 --> U211[Çıkış Yap]:::islem
    U208 --> U212((.))
    U212 --> U213[Yeni Kullanıcıları Onayla]:::islem
    U212 --> U214[Geçmiş Kullanıcılarla İlgili Ayarlar]:::islem
    
    U203 --> U301((.))
    U301 --> U302[Yeni Talep Oluştur]:::islem
    U301 --> U303[Geçmiş Talepleri Görüntüle]:::islem
    U302 --> U304[/Talep Başlığı ve Açıklama Gir/]:::verigirisi
    U304 --> U305[Talep Gönder]:::islem
    U305 --> U306(Talep Tasks Tablosuna Düşer Öncelik Puanının Hesaplanması İçin Ürün ve Yazılım Yöneticisinin İşlemlerini Bekler)
    U303 --> U307[Müşterinin Karşısına 3 seçenek çıkar]:::islem
    U307 --> U308[Talep Detaylarını Görüntüleme]:::islem
    U308 --> U317(Kapat)
    U307 --> U309[Talep Detaylarını Değiştirme]:::islem
    U309 --> U314[Değişiklik Yapılmak İstenen Talebin Yanınadaki Butona Basılarak Talep Değiştirme Ekranı Pop Up Olarak Ekranda Açılır]:::islem
    U314 --> U315[Değişiklik Yapılacak Alanlarda Değişiklikler Yapılır / Yeni Dosyalar Eklenir]:::islem
    U315 --> U316(O taleple ilgilenen ürün yöneticisi ve yazılım yöneticiyle atandıysa o talebin atandığı yazılımcıya güncellenmiş verisyonu güncellendiği belirtilerek geri gönderilir)
    U301 --> U310[Admine Ulaş]:::islem
    U310 --> U311[/Hata detaylarını gir/]:::verigirisi
    U311 --> U312(Adminin ekranına ve mail ekranına talep gelir)
    U301 --> U313(Çıkış Yap)
    
    U204 --> U401((.))
    U401 --> U402[Talepler Ekranı Açılır]:::islem
    U402 --> U404[Ürün yöneticisi aciliyeti kimden geldiği, ürüne olan etkisi, ödemelere olan etikisi ve ürün kapsamında bulunup bulunmadığı üzerinden değerlendirerek bir ürün direktörü puanını verir ve müşteri ağırlığı puanını da belirler]:::islem
    U404 --> U405(Talep ile ilgili bu puanlar girilerek talep bu puanlarla birlikte yazılım yöneticisinin ekranına düşer)
    U401 --> U403[Geçmiş Talepleri Görüntüle]:::islem
    U403 --> U406[Daha önce gerekli bilgilerin girildiği talepler hakkındaki güncellemeleri mesela yazılım yöneticisinin girdiği skorları yönlendirildiyse yönlendirilen yazılımcıyı ve son hesaplanmış öncelik skorunu görüntüleyebilir]:::islem
    
    U205 --> U501((.))
    U501 --> U502[Talepleri Görüntüle]:::islem
    U501 --> U503[Talepleri Yazılımcıya Atama Ekranı]:::islem
    U502 --> U504[Seçilen Talebin uygulama ve yazılım bazında puanlandırılması yapılarak final öncelik puanı hesaplanır]:::islem
    U504 --> U505[Puanı hesaplanan talepler ekranda çıkar]:::islem
    U505 --> U506[Yazılımcılar şuan atanan taleplerle birlikte ekranda çıkar]:::islem
    U506 --> U507[Yazılımcılara tasklar sürükleyip bırakılarak atanır.]:::islem
    U507 --> U508(Taleplerin yazılımcılara atanması biter):::islem
    
    U206 --> U601((.))
    U601 --> U602[Yazılımcı ana ekranı açılır]:::islem
    U602 --> U603[Yönlendirilen taskler önceden hesaplanmış olan öncelik skoruyla ekranda sıralı bir şekilde görünür]:::islem
    U603 --> U604[Yapılan tasklar işaretlenir]:::islem
    U604 --> U605(Bitiş)
```

## Technologies Used

This project was developed using a combination of **Java (Maven and Spring Boot)** for the robust backend infrastructure and **Vaadin** for the frontend UI design and components.

## File Structure

To ensure modularity and ease of management, this project was architected using **Domain-Driven Design (DDD)**. Each business domain encapsulates its own models, repositories, services, and views.

```text
Request-Management-System/
├── frontend/                     
│   └── styles/
│       └── styles.css            
├── src/
│   ├── main/
│   │   ├── java/org/vaadin/example/
│   │   │   ├── auth/            
│   │   │   ├── config/          
│   │   │   ├── domain/          
│   │   │   │   ├── admin/       
│   │   │   │   ├── company/     
│   │   │   │   ├── contactadmin/ 
│   │   │   │   ├── customer/     
│   │   │   │   ├── developer/    
│   │   │   │   ├── notification/ 
│   │   │   │   ├── priority/    
│   │   │   │   ├── productowner/
│   │   │   │   ├── request/     
│   │   │   │   ├── supervisor/   
│   │   │   │   ├── user/         
│   │   │   │   └── workflow/    
│   │   │   ├── shared/           
│   │   │   └── Application.java 
│   │   └── resources/
│   │       ├── application.properties 
│   │       └── data.sql          
│   └── test/                   
├── pom.xml                       
└── .gitignore
```
