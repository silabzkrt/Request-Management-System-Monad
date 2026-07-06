package org.vaadin.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public AdminUserInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        String email = "silaabzkrtt@gmail.com";
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users_sila WHERE email = ?", Integer.class, email);
        
        if (count != null && count == 0) {
            jdbcTemplate.update(
                "INSERT INTO users_sila (user_type, is_approved, company_id, email, generated_id, name_surname, password, role) VALUES ('ADMIN', 1, 1, ?, '999999', 'Sıla Bozkurt', '123', 'ROLE_ADMIN')",
                email
            );
            System.out.println("Admin user created: " + email);
        }

        // Fix sequence desyncs that might have occurred from previous manual inserts
        try {
            jdbcTemplate.execute("ALTER TABLE users_sila ALTER COLUMN user_id RESTART WITH (SELECT COALESCE(MAX(user_id), 0) + 1 FROM users_sila)");
            jdbcTemplate.execute("ALTER TABLE company_sila ALTER COLUMN company_id RESTART WITH (SELECT COALESCE(MAX(company_id), 0) + 1 FROM company_sila)");
            System.out.println("Database sequences synced successfully.");
        } catch (Exception e) {
            System.err.println("Sequence sync failed: " + e.getMessage());
        }
    }
}
