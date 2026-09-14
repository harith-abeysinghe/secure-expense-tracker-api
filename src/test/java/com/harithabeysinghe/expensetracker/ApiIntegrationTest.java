package com.harithabeysinghe.expensetracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.secret=integration-test-secret-that-is-longer-than-thirty-two-bytes",
        "app.bootstrap-admin.email=admin@example.com",
        "app.bootstrap-admin.password=administrator-password",
        "app.bootstrap-admin.display-name=Portfolio Admin",
        "app.bootstrap-admin.currency=USD"
})
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.6-alpine")
            .withDatabaseName("expense_tracker")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void completeUserAndAdminWorkflowEnforcesIsolationAndTokenRotation() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api/v1/expenses")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad\",\"password\":\"short\",\"displayName\":\"\",\"currency\":\"XX\"}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        var userOne = register("one@example.com", "First Portfolio User", "USD");
        var userTwo = register("two@example.com", "Second Portfolio User", "EUR");
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("ONE@example.com", "Duplicate", "USD")))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"one@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(userOne.access())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currency").value("USD"));

        var categories = body(mvc.perform(get("/api/v1/categories").header("Authorization", bearer(userOne.access())))
                .andExpect(status().isOk()).andReturn());
        var groceries = categories.get(0).get("id").asText();

        var personal = body(mvc.perform(post("/api/v1/categories").header("Authorization", bearer(userOne.access()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Coffee\"}"))
                .andExpect(status().isCreated()).andReturn());
        var personalId = personal.get("id").asText();
        mvc.perform(post("/api/v1/categories").header("Authorization", bearer(userOne.access()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Groceries\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/v1/categories/{id}", personalId).header("Authorization", bearer(userOne.access()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Coffee shops\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Coffee shops"));

        var expense = body(mvc.perform(post("/api/v1/expenses").header("Authorization", bearer(userOne.access()))
                        .contentType(MediaType.APPLICATION_JSON).content(expenseJson(groceries, "42.50", "2026-09-14")))
                .andExpect(status().isCreated()).andReturn());
        var expenseId = expense.get("id").asText();
        mvc.perform(get("/api/v1/expenses/{id}", expenseId).header("Authorization", bearer(userOne.access())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(42.50));
        mvc.perform(get("/api/v1/expenses/{id}", expenseId).header("Authorization", bearer(userTwo.access())))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/expenses").param("from", "2026-10-01").param("to", "2026-09-01")
                        .header("Authorization", bearer(userOne.access())))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/expenses/{id}", expenseId).header("Authorization", bearer(userOne.access()))
                        .contentType(MediaType.APPLICATION_JSON).content(expenseJson(groceries, "50.00", "2026-09-14")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(50.00));
        mvc.perform(get("/api/v1/expenses").param("categoryId", groceries).param("from", "2026-09-01")
                        .param("to", "2026-09-30").header("Authorization", bearer(userOne.access())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(put("/api/v1/budgets/2026/9/categories/{id}", groceries)
                        .header("Authorization", bearer(userOne.access())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":40.00}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/budgets").param("year", "2026").param("month", "9")
                        .header("Authorization", bearer(userOne.access())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].amount").value(40.00));
        mvc.perform(get("/api/v1/reports/monthly/2026/9").header("Authorization", bearer(userOne.access())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("OVER_BUDGET"))
                .andExpect(jsonPath("$.totalSpent").value(50.00));

        mvc.perform(delete("/api/v1/categories/{id}", personalId).header("Authorization", bearer(userOne.access())))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/expenses").header("Authorization", bearer(userOne.access()))
                        .contentType(MediaType.APPLICATION_JSON).content(expenseJson(personalId, "5.00", "2026-09-14")))
                .andExpect(status().isNotFound());

        var refreshed = body(mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + userOne.refresh() + "\"}"))
                .andExpect(status().isOk()).andReturn());
        var rotatedRefresh = refreshed.get("refreshToken").asText();
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + userOne.refresh() + "\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rotatedRefresh + "\"}"))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rotatedRefresh + "\"}"))
                .andExpect(status().isUnauthorized());

        var admin = login("admin@example.com", "administrator-password");
        mvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(userTwo.access())))
                .andExpect(status().isForbidden());
        var userList = body(mvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(admin.access())))
                .andExpect(status().isOk()).andReturn());
        var secondId = findUserId(userList, "two@example.com");
        var adminId = findUserId(userList, "admin@example.com");
        mvc.perform(patch("/api/v1/admin/users/{id}/status", adminId).header("Authorization", bearer(admin.access()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/v1/admin/users/{id}/status", secondId).header("Authorization", bearer(admin.access()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DISABLED"));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("two@example.com", "correct-horse-battery")))
                .andExpect(status().isUnauthorized());

        var global = body(mvc.perform(post("/api/v1/admin/categories").header("Authorization", bearer(admin.access()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Education\"}"))
                .andExpect(status().isCreated()).andReturn());
        mvc.perform(put("/api/v1/admin/categories/{id}", global.get("id").asText())
                        .header("Authorization", bearer(admin.access())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Learning\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/v1/admin/categories/{id}", global.get("id").asText())
                        .header("Authorization", bearer(admin.access())))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/expenses").header("Authorization", bearer(admin.access())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(delete("/api/v1/budgets/2026/9/categories/{id}", groceries)
                        .header("Authorization", bearer(userOne.access())))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/expenses/{id}", expenseId).header("Authorization", bearer(userOne.access())))
                .andExpect(status().isNoContent());
    }

    private Tokens register(String email, String displayName, String currency) throws Exception {
        var node = body(mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(email, displayName, currency)))
                .andExpect(status().isCreated()).andReturn());
        return tokens(node);
    }

    private Tokens login(String email, String password) throws Exception {
        return tokens(body(mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk()).andReturn()));
    }

    private String userJson(String email, String displayName, String currency) {
        return "{\"email\":\"" + email + "\",\"password\":\"correct-horse-battery\",\"displayName\":\"" +
                displayName + "\",\"currency\":\"" + currency + "\"}";
    }

    private String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String expenseJson(String category, String amount, String date) {
        return "{\"categoryId\":\"" + category + "\",\"amount\":" + amount +
                ",\"expenseDate\":\"" + date + "\",\"description\":\"Weekly groceries\"}";
    }

    private JsonNode body(org.springframework.test.web.servlet.MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }

    private Tokens tokens(JsonNode node) {
        return new Tokens(node.get("accessToken").asText(), node.get("refreshToken").asText());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String findUserId(JsonNode page, String email) {
        for (var user : page.get("content"))
            if (email.equals(user.get("email").asText())) return user.get("id").asText();
        throw new AssertionError("User not found: " + email);
    }

    private record Tokens(String access, String refresh) {
    }
}

