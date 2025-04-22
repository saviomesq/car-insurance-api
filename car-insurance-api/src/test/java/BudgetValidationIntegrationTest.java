import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//SAVIO

@SpringBootTest(classes = com.car.insurance.api.CarInsuranceApiApplication.class)
@AutoConfigureMockMvc
@Transactional
public class BudgetValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String bearerToken;

    @BeforeEach
    void setUp() throws Exception {
        String response = mockMvc.perform(post("/api/v1/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "financialclient@email.com")
                .param("senha", "password"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> tokenMap = mapper.readValue(response, Map.class);
        bearerToken = tokenMap.get("token");
    }

    @Test
    @DisplayName("❌ Deve retornar 400 quando customerId é inválido")
    void deveRetornar400ParaCustomerIdInvalido() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", -1); 

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Deve retornar 400 quando carId é nulo")
    void deveRetornar400QuandoCarIdForNulo() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", null); 
        payload.put("customerId", 1);

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ Deve cadastrar orçamento com dados válidos")
    void deveCadastrarBudgetComSucesso() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("carId", 1);
        payload.put("customerId", 1);

        mockMvc.perform(post("/api/v1/insurance/budget")
                .header("Authorization", "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(payload)))
                .andExpect(status().isCreated());
    }
}