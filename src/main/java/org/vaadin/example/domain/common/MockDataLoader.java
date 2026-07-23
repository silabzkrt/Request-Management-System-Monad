package org.vaadin.example.domain.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class MockDataLoader {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/api/run-mock")
    public String runMock() {
        try {
            System.out.println("====== EXECUTING MOCK DATA SCRIPT ======");
            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/mock_data.sql")));
            String[] statements = sql.split(";");
            int count = 0;
            for (String stmt : statements) {
                if (stmt.trim().length() > 0) {
                    jdbcTemplate.execute(stmt.trim());
                    count++;
                }
            }
            return "SUCCESS. Executed " + count + " statements.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
}

