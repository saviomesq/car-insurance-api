
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

//VITOR

@SpringBootTest(classes = com.car.insurance.api.CarInsuranceApiApplication.class)
@AutoConfigureMockMvc
@Transactional // Reverte as alterações no banco após cada teste
public class InvalidDataBudgetServiceImplTest {

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
    public void testSearchBudget_InvalidId_ShouldReturnNotFound() throws Exception {
        // Tentar buscar um orçamento com um ID inexistente
        int invalidId = 999; // ID inválido
        mockMvc.perform(get("/api/v1/insurance/budget/{id}", invalidId)
                .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isNotFound()); // Espera o status HTTP 404
    }

    @Test
    public void testDeleteBudget_InvalidId_ShouldReturnNotFound() throws Exception {
        // Tentar deletar um orçamento com um ID inválido
        int invalidId = 999; // Um ID que não existe no banco

        mockMvc.perform(delete("/api/v1/insurance/budget/{id}", invalidId)
                .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isNotFound());// Espera o status HTTP 404
    }

    @Test
    public void testCreateBudget_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Suponha que o payload esteja vazio
        Map<String, Object> emptyBudgetPayload = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyBudgetPayload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

    @Test
    public void testUpdateBudget_InvalidBudgetIdInBody_ShouldReturnNotFound() throws Exception {
        // ID inexistente no banco
        int invalidBudgetId = 999;

        Map<String, Object> payload = new HashMap<>();
        payload.put("budgetId", invalidBudgetId);
        payload.put("carId", 1);
        payload.put("customerId", 1);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(put("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound()); // Espera o status HTTP 404
    }

    @Test
    public void testCreateBudget_NullFields_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", null);
        payload.put("customerId", null);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // Espera o status HTTP 400
    }

}