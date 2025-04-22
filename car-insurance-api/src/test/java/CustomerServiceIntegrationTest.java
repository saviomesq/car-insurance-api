import com.car.insurance.api.domain.Customer;
import com.car.insurance.api.domain.Driver;
import com.car.insurance.api.domain.repository.CustomerRepository;
import com.car.insurance.api.domain.repository.DriverRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional //garante que ao final do teste as modificações no banco serão restauradas
public class CustomerServiceIntegrationTest {

    @Autowired 
    private CustomerRepository customerRepository;
    // cria uma versão funcional de Customer, com uma série de métodos

    @Autowired
    private DriverRepository driverRepository;

    @Test
    @DisplayName("Deve cadastrar e persistir um novo cliente com sucesso")
    void createCustomer_WithValidData_SavesSuccessfully() {
        // cria um novo driver, insere no banco e o utiliza para criar um novo customer
        Driver driver = new Driver(null, "99988877766", java.time.LocalDate.of(1990, 3, 15));
        driver = driverRepository.save(driver);

        Customer customer = new Customer(null, "Cliente Teste", driver);

        // salva o customer no banco
        Customer savedCustomer = customerRepository.save(customer);

        // realiza a verificação
        assertNotNull(savedCustomer.getId());
        assertEquals("Cliente Teste", savedCustomer.getName());
        assertEquals(driver.getId(), savedCustomer.getDriver().getId());
    }

    @Test
    @DisplayName("Deve atualizar o nome do cliente com sucesso")
    void updateCustomerName_ShouldUpdateSuccessfully() {
        // cria um novo driver e um novo customer
        Driver driver = new Driver(null, "12345678900", java.time.LocalDate.of(1990, 4, 10));
        driver = driverRepository.save(driver);

        Customer customer = new Customer(null, "Cliente Original", driver);
        customer = customerRepository.save(customer);

        // recria o objeto com novo nome e mesmo ID
        Customer updatedCustomer = Customer.builder()
                .id(customer.getId())
                .name("Cliente Atualizado")
                .driver(driver)
                .build();

        updatedCustomer = customerRepository.save(updatedCustomer);

        // realiza a verificação
        assertEquals("Cliente Atualizado", updatedCustomer.getName());
        assertEquals(driver.getId(), updatedCustomer.getDriver().getId());
    }

}
