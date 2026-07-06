package org.vaadin.example.auth.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.example.domain.company.dao.CompanyRepository;
import org.vaadin.example.domain.company.model.Company;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.shared.enums.UserTypes;

@Route("login")
@PageTitle("Giriş | Talep Yönetim Sistemi")
@AnonymousAllowed
@StyleSheet("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final CompanyRepository companyRepository;

    public LoginView(UserService userService, CompanyRepository companyRepository) {
        this.userService = userService;
        this.companyRepository = companyRepository;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        getStyle().set("background", "linear-gradient(to bottom, #ffffff 40%, #7da2cc 100%)");
        getStyle().set("font-family", "'Inter', sans-serif"); 

        Image logo = new Image("images/logo.png", "Monad Logo");
        logo.setMaxHeight("90px");
        
        Span subtitle = new Span("Monad Yazılım ve Danışmanlık'a Hoş Geldiniz");
        subtitle.getStyle().set("font-size", "15px").set("color", "#000").set("font-weight", "500").set("margin-top", "15px");
        
        H2 title = new H2("Talep Yönetim Sistemi");
        title.getStyle().set("margin-top", "5px").set("font-weight", "600").set("color", "#000");
        
        VerticalLayout header = new VerticalLayout(logo, subtitle, title);
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(false);
        header.getStyle().set("margin-bottom", "40px");

        H3 loginTitle = new H3("Giriş Yap");
        loginTitle.getStyle().set("margin-top", "0").set("font-weight", "normal");
        
        TextField loginMail = new TextField();
        loginMail.getElement().setAttribute("name", "username");
        makeTransparent(loginMail);
        
        PasswordField loginPassword = new PasswordField();
        loginPassword.getElement().setAttribute("name", "password");
        makeTransparent(loginPassword);

        FormLayout loginLayout = new FormLayout();
        loginLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
        loginLayout.addFormItem(loginMail, "E-posta");
        loginLayout.addFormItem(loginPassword, "Şifre");
        
        HtmlForm form = new HtmlForm();
        form.setAction("login");
        form.setMethod("post");
        
        NativeButton submitBtn = new NativeButton("Giriş Yap");
        submitBtn.getStyle()
            .set("background-color", "#3b6998")
            .set("color", "white")
            .set("border", "none")
            .set("padding", "10px 40px")
            .set("font-size", "16px")
            .set("cursor", "pointer")
            .set("margin-top", "30px");
            
        Anchor forgotPassword = new Anchor("#", "Şifremi Unuttum");
        forgotPassword.getStyle().set("font-size", "13px").set("color", "#3b6998").set("text-decoration", "underline").set("margin-top", "15px");
            
        VerticalLayout customLoginForm = new VerticalLayout(loginTitle, loginLayout, submitBtn, forgotPassword);
        customLoginForm.setAlignItems(Alignment.CENTER);
        customLoginForm.setWidth("380px");
        form.add(customLoginForm);

        Div divider = new Div();
        divider.getStyle().set("width", "1px").set("background-color", "#000").set("margin", "0 40px");

        H3 regTitle = new H3("Kayıt Ol");
        regTitle.getStyle().set("margin-top", "0").set("font-weight", "normal");
        
        TextField regName = new TextField();
        TextField regEmail = new TextField();
        TextField regCompany = new TextField(); 
        PasswordField regPassword = new PasswordField();

        makeTransparent(regName);
        makeTransparent(regEmail);
        makeTransparent(regCompany);
        makeTransparent(regPassword);

        FormLayout regLayout = new FormLayout();
        regLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
        regLayout.addFormItem(regName, "Ad Soyad");
        regLayout.addFormItem(regEmail, "E-posta");
        regLayout.addFormItem(regCompany, "Firma Adı"); 
        regLayout.addFormItem(regPassword, "Şifre");
        
        Button registerButton = new Button("Kayıt Ol");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.getStyle().set("background-color", "#3b6998").set("padding", "20px 40px").set("margin-top", "10px");
            
        registerButton.addClickListener(e -> {
            String name = regName.getValue();
            String email = regEmail.getValue();
            String compName = regCompany.getValue();
            UserTypes role = UserTypes.MUSTERI; // Default role
            String password = regPassword.getValue();

            if (name == null || name.isEmpty() || email == null || email.isEmpty() || 
                compName == null || compName.isEmpty() || password == null || password.isEmpty()) {
                Notification.show("Tüm alanları doldurunuz!", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            try {
                Company company = companyRepository.findByCompanyName(compName)
                    .orElseGet(() -> {
                        Company newComp = new Company();
                        newComp.setCompanyName(compName);
                        newComp.setCompanyPoints(0.0);
                        return companyRepository.save(newComp);
                    });

                User user;
                switch (role) {
                    case MUSTERI: user = new org.vaadin.example.domain.customer.model.Customer(); break;
                    case ADMIN: user = new org.vaadin.example.domain.admin.model.Admin(); break;
                    case YAZILIMCI: user = new org.vaadin.example.domain.developer.model.Developer(); break;
                    case URUN_SORUMLUSU: user = new org.vaadin.example.domain.productowner.model.ProductOwner(); break;
                    case YAZILIM_YONETICISI: user = new org.vaadin.example.domain.supervisor.model.Supervisor(); break;
                    default: user = new org.vaadin.example.domain.customer.model.Customer();
                }
                
                user.setNameSurname(name);
                user.setEmail(email);
                user.setRole(role.getSpringRole());
                user.setPassword(password);
                user.setCompany(company);

                userService.save(user);

                Notification.show("Kayıt başarılı! Şimdi giriş yapabilirsiniz.", 4000, Notification.Position.TOP_CENTER);
                
                // Formu temizle
                regName.clear();
                regEmail.clear();
                regCompany.clear();
                regPassword.clear();

            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Kayıt başarısız! E-posta adresi kullanılıyor olabilir.", 4000, Notification.Position.TOP_CENTER);
            }
        });
        
        VerticalLayout regCol = new VerticalLayout(regTitle, regLayout, registerButton);
        regCol.setAlignItems(Alignment.CENTER);
        regCol.setWidth("450px");
        
        HorizontalLayout splitLayout = new HorizontalLayout(form, divider, regCol);
        splitLayout.setAlignItems(Alignment.STRETCH);
        splitLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        
        add(header, splitLayout);
    }

    private void makeTransparent(com.vaadin.flow.component.Component field) {
        field.getStyle().set("--lumo-contrast-10pct", "transparent");
        field.getStyle().set("border-bottom", "1px solid #7da2cc");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            Notification.show("Hatalı e-posta veya şifre!", 4000, Notification.Position.TOP_CENTER);
        }
    }

    @com.vaadin.flow.component.Tag("form")
    public static class HtmlForm extends com.vaadin.flow.component.HtmlContainer {
        public HtmlForm() {}
        public void setAction(String action) { getElement().setAttribute("action", action); }
        public void setMethod(String method) { getElement().setAttribute("method", method); }
    }
}
