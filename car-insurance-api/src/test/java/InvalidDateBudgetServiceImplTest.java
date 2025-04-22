import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = com.car.insurance.api.CarInsuranceApiApplication.class)
@AutoConfigureMockMvc
@Transactional // Reverte as alterações no banco após cada teste
public class InvalidDateBudgetServiceImplTest {

    @Autowired
    private MockMvc mockMvc;

    private String bearerToken;

    @BeforeEach
    public void setUp() throws Exception {
        // Login com usuário e senha já existentes para obter o token
        String response = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "financialclient@email.com")
                .param("senha", "password"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseMap = objectMapper.readValue(response, Map.class);
        bearerToken = responseMap.get("token");// Armazena o token para uso nos testes

        System.out.println("Token: " + bearerToken);

        if (bearerToken.isEmpty()) {
            throw new IllegalStateException("O token está vazio. Por favor, forneça um token válido.");
        }
    }

    @Test
    public void testCreateBudget_InvalidStartDate_ShouldReturnBadRequest() throws Exception {
        // Data de início inválida (formato errado)
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2025-99-99"); // Data inválida

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_InvalidEndDate_ShouldReturnBadRequest() throws Exception {
        // Data de término inválida (formato errado)
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2025-01-01");
        payload.put("endDate", "invalid-date-format"); // Data inválida

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_EndDateBeforeStartDate_ShouldReturnBadRequest() throws Exception {
        // Data de término antes da data de início
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2025-06-01");
        payload.put("endDate", "2025-05-01"); // Anterior à data de início

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_StartDateInPast_ShouldReturnBadRequest() throws Exception {
        // Data de início no passado
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2020-01-01"); // Data no passado
        payload.put("endDate", "2026-01-01");

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_DateRangeTooLong_ShouldReturnBadRequest() throws Exception {
        // Período de cobertura muito longo (mais de 2 anos)
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2025-05-01");
        payload.put("endDate", "2028-05-01"); // 3 anos depois

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_MissingStartDate_ShouldReturnBadRequest() throws Exception {
        // Ausência da data de início
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        // Não inclui startDate
        payload.put("endDate", "2026-01-01");

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_InvalidDateTimeZone_ShouldReturnBadRequest() throws Exception {
        // Data com fuso horário inválido
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2025-05-01T00:00:00+99:99"); // Fuso horário inválido
        payload.put("endDate", "2026-05-01");

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_InvalidMonth_ShouldReturnBadRequest() throws Exception {
        // Mês inválido na data
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2025-13-01"); // Mês 13 é inválido
        payload.put("endDate", "2026-01-01");

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testCreateBudget_InvalidDay_ShouldReturnBadRequest() throws Exception {
        // Dia inválido na data
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);
        payload.put("startDate", "2025-02-30"); // 30 de fevereiro é inválido
        payload.put("endDate", "2026-01-01");

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    
}