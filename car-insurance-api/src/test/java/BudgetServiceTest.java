
import com.car.insurance.api.domain.Budget;
import com.car.insurance.api.domain.Car;
import com.car.insurance.api.domain.Customer;
import com.car.insurance.api.domain.Driver;
import com.car.insurance.api.domain.CarDriver;
import com.car.insurance.api.domain.dto.BudgetRequestDTO;
import com.car.insurance.api.domain.repository.BudgetRepository;
import com.car.insurance.api.domain.repository.CarRepository;
import com.car.insurance.api.domain.repository.CustomerRepository;
import com.car.insurance.api.domain.repository.DriverRepository;
import com.car.insurance.api.domain.repository.CarDriverRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//CAMILLY
@SpringBootTest
@Transactional
public class BudgetServiceTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarDriverRepository carDriverRepository;

    private Car car;
    private Customer customer;
    private Driver driver;
    private CarDriver carDriver;

    @BeforeEach
    public void setup() {

        budgetRepository.deleteAll();

        //criar e persistir um motorista
        driver = new Driver(null, "Documento Teste", LocalDate.of(1990, 1, 1));
        driver = driverRepository.save(driver);

        //criar e persistir um carro
        car = new Car(null, "Modelo Teste", "Fabricante Teste", "2020", 50000F, null);
        car = carRepository.save(car);

        //criar e persistir um cliente
        customer = new Customer(null, "Cliente Teste", driver);
        customer = customerRepository.save(customer);

        //criar e persistir um CarDriver (motorista principal para o carro)
        carDriver = new CarDriver(null, driver, car, null, true);
        carDriver = carDriverRepository.save(carDriver);


        List<CarDriver> carDrivers = new ArrayList<>();
        carDrivers.add(carDriver);
        car.setCarDriver(carDrivers);
        car = carRepository.save(car);
    }

    @Test
    public void testCreateBudget() throws Exception {
        //verificar quantidade inicial de orçamentos
        int initialCount = budgetRepository.findAll().size();

        //criar DTO de requisição
        BudgetRequestDTO budgetDTO = new BudgetRequestDTO();
        budgetDTO.setCustomerId(customer.getId());
        budgetDTO.setCarId(car.getId());



        int newCount = budgetRepository.findAll().size();
        assertEquals(initialCount + 1, newCount, "Deveria ter criado um novo orçamento");

        //verificar se o orçamento foi criado com os dados corretos
        Budget createdBudget = budgetRepository.findAll().get(newCount - 1);
        assertNotNull(createdBudget, "O orçamento não deveria ser nulo");
        assertEquals(car.getId(), createdBudget.getCar().getId(), "O carro do orçamento deve ser o esperado");
        assertEquals(customer.getId(), createdBudget.getCustomer().getId(), "O cliente do orçamento deve ser o esperado");
    }
}