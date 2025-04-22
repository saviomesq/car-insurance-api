
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.car.insurance.api.domain.*;
import com.car.insurance.api.domain.dto.BudgetRequestDTO;
import com.car.insurance.api.domain.dto.BudgetResponseDto;
import com.car.insurance.api.domain.exception.BudgetNotFoundException;
import com.car.insurance.api.domain.exception.CustomBusinessException;
import com.car.insurance.api.domain.exception.NoMainDriverRegisteredException;
import com.car.insurance.api.domain.repository.*;
import com.car.insurance.api.domain.service.BudgetService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

//FELYPE

@SpringBootTest(classes = com.car.insurance.api.CarInsuranceApiApplication.class)
@Transactional
public class BudgetServiceIntegrationTest {
        @Autowired
        private BudgetService budgetService;

        @Autowired
        private BudgetRepository budgetRepository;

        @Autowired
        private CarRepository carRepository;

        @Autowired
        private DriverRepository driverRepository;

        @Autowired
        private CustomerRepository customerRepository;

        @Autowired
        private CarDriverRepository carDriverRepository;

        @Autowired
        private ClaimRepository claimRepository;

        private Integer existingBudgetId;
        private Integer budgetWithClaimsId;
        private Integer youngDriverBudgetId;
        private Integer carWithoutMainDriverId;
        private Integer multipleClaimsBudgetId;

        @BeforeEach
        void setUp() {
                // Criação do primeiro orçamento (caso padrão)
                Driver driver = new Driver(
                                null,
                                "12345678900",
                                LocalDate.of(1980, 1, 1));
                driver = driverRepository.save(driver);

                Customer customer = new Customer(
                                null,
                                "Cliente Teste",
                                driver);
                customer = customerRepository.save(customer);

                Car car = new Car(
                                null,
                                "Fiat Uno",
                                "Fiat",
                                "2020",
                                35000.0f,
                                new ArrayList<>());
                car = carRepository.save(car);

                CarDriver carDriver = new CarDriver(
                                null,
                                driver,
                                car,
                                null,
                                true);
                carDriver = carDriverRepository.save(carDriver);

                car.getCarDriver().add(carDriver);
                carRepository.save(car);

                Budget budget = new Budget(
                                null,
                                car,
                                customer,
                                2100.0);
                budget = budgetRepository.save(budget);
                existingBudgetId = budget.getId();

                // Criação do segundo orçamento (com sinistros)
                Driver driverWithClaim = new Driver(
                                null,
                                "98765432100",
                                LocalDate.of(1975, 6, 15));
                driverWithClaim = driverRepository.save(driverWithClaim);

                Customer customerWithClaim = new Customer(
                                null,
                                "Cliente Com Sinistro",
                                driverWithClaim);
                customerWithClaim = customerRepository.save(customerWithClaim);

                Car carWithClaim = new Car(
                                null,
                                "Honda Civic",
                                "Honda",
                                "2021",
                                80000.0f,
                                new ArrayList<>());
                carWithClaim = carRepository.save(carWithClaim);

                CarDriver carDriverWithClaim = new CarDriver(
                                null,
                                driverWithClaim,
                                carWithClaim,
                                null,
                                true);
                carDriverWithClaim = carDriverRepository.save(carDriverWithClaim);

                carWithClaim.getCarDriver().add(carDriverWithClaim);
                carRepository.save(carWithClaim);

                // Adicionar sinistro para o motorista
                Claim claim = new Claim(
                                null,
                                LocalDateTime.now().minusMonths(3),
                                driverWithClaim,
                                carWithClaim);
                claimRepository.save(claim);

                Budget budgetWithClaims = new Budget(
                                null,
                                carWithClaim,
                                customerWithClaim,
                                // Valor incorreto (deveria ser 8% do valor FIPE) para causar falha no teste
                                7000.0);
                budgetWithClaims = budgetRepository.save(budgetWithClaims);
                budgetWithClaimsId = budgetWithClaims.getId();

                // Criação do terceiro orçamento (motorista jovem)
                Driver youngDriver = new Driver(
                                null,
                                "11122233344",
                                LocalDate.now().minusYears(22));
                youngDriver = driverRepository.save(youngDriver);

                Customer youngCustomer = new Customer(
                                null,
                                "Cliente Jovem",
                                youngDriver);
                youngCustomer = customerRepository.save(youngCustomer);

                Car carYoungDriver = new Car(
                                null,
                                "Renault Kwid",
                                "Renault",
                                "2022",
                                45000.0f,
                                new ArrayList<>());
                carYoungDriver = carRepository.save(carYoungDriver);

                CarDriver carDriverYoung = new CarDriver(
                                null,
                                youngDriver,
                                carYoungDriver,
                                null,
                                true);
                carDriverYoung = carDriverRepository.save(carDriverYoung);

                carYoungDriver.getCarDriver().add(carDriverYoung);
                carRepository.save(carYoungDriver);

                Budget youngDriverBudget = new Budget(
                                null,
                                carYoungDriver,
                                youngCustomer,
                                3600.0); // 8% do valor FIPE (6% base + 2% por ser jovem)
                youngDriverBudget = budgetRepository.save(youngDriverBudget);
                youngDriverBudgetId = youngDriverBudget.getId();

                // Criação de um carro sem motorista principal
                Car carWithoutMainDriver = new Car(
                                null,
                                "Chevrolet Onix",
                                "Chevrolet",
                                "2023",
                                55000.0f,
                                new ArrayList<>());
                carWithoutMainDriver = carRepository.save(carWithoutMainDriver);

                Driver secondaryDriver = new Driver(
                                null,
                                "55566677788",
                                LocalDate.of(1995, 3, 10));
                secondaryDriver = driverRepository.save(secondaryDriver);

                CarDriver secondaryCarDriver = new CarDriver(
                                null,
                                secondaryDriver,
                                carWithoutMainDriver,
                                null,
                                false); // Sem motorista principal
                secondaryCarDriver = carDriverRepository.save(secondaryCarDriver);

                carWithoutMainDriver.getCarDriver().add(secondaryCarDriver);
                carRepository.save(carWithoutMainDriver);

                carWithoutMainDriverId = carWithoutMainDriver.getId();

                // Criação do quinto orçamento (com múltiplos sinistros - novo teste)
                Driver driverMultipleClaims = new Driver(
                                null,
                                "44433322211",
                                LocalDate.of(1982, 10, 5));
                driverMultipleClaims = driverRepository.save(driverMultipleClaims);

                Customer customerMultipleClaims = new Customer(
                                null,
                                "Cliente Múltiplos Sinistros",
                                driverMultipleClaims);
                customerMultipleClaims = customerRepository.save(customerMultipleClaims);

                Car carMultipleClaims = new Car(
                                null,
                                "Toyota Corolla",
                                "Toyota",
                                "2022",
                                100000.0f,
                                new ArrayList<>());
                carMultipleClaims = carRepository.save(carMultipleClaims);

                CarDriver carDriverMultipleClaims = new CarDriver(
                                null,
                                driverMultipleClaims,
                                carMultipleClaims,
                                null,
                                true);
                carDriverMultipleClaims = carDriverRepository.save(carDriverMultipleClaims);

                carMultipleClaims.getCarDriver().add(carDriverMultipleClaims);
                carRepository.save(carMultipleClaims);

                // Adicionar múltiplos sinistros para o motorista e o carro
                Claim claim1 = new Claim(
                                null,
                                LocalDateTime.now().minusMonths(2),
                                driverMultipleClaims,
                                carMultipleClaims);
                claimRepository.save(claim1);

                Claim claim2 = new Claim(
                                null,
                                LocalDateTime.now().minusMonths(6),
                                driverMultipleClaims,
                                carMultipleClaims);
                claimRepository.save(claim2);

                Budget multipleClaimsBudget = new Budget(
                                null,
                                carMultipleClaims,
                                customerMultipleClaims,
                                10000.0); // 10% do valor FIPE (6% base + 4% pelos dois sinistros)
                multipleClaimsBudget = budgetRepository.save(multipleClaimsBudget);
                multipleClaimsBudgetId = multipleClaimsBudget.getId();
        }

        @Test
        @DisplayName("Deve retornar detalhes do orçamento quando ID válido")
        void getBudget_WithValidId_ReturnsBudgetDetails() throws CustomBusinessException {
                // Act
                BudgetResponseDto response = budgetService.getBudget(existingBudgetId);

                // Assert
                assertNotNull(response);
                assertEquals(2100.0, response.getAmount());
                assertEquals("Fiat Uno", response.getCar().getCarModel());
                assertEquals("Fiat", response.getCar().getCarManufacturer());
                assertEquals("2020", response.getCar().getCarYear());
                assertEquals(1, response.getDrivers().size());
                assertEquals("12345678900", response.getDrivers().get(0).getDriverDocument());
                assertTrue(response.getDrivers().get(0).getMainDriver());
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID do orçamento não existe")
        void getBudget_WithInvalidId_ThrowsException() {
                // Act & Assert
                assertThrows(BudgetNotFoundException.class, () -> {
                        budgetService.getBudget(9999);
                });
        }

        @Test
        @DisplayName("Deve retornar orçamento com valor maior para carro com sinistro")
        void getBudget_WithCarHavingClaims_ReturnsHigherAmount() throws CustomBusinessException {
                // Act
                BudgetResponseDto response = budgetService.getBudget(budgetWithClaimsId);

                // Assert
                assertNotNull(response);
                // Teste falha aqui - o valor esperado seria 6400.0 (8% de 80000.0), mas o valor
                // real no banco é 7000.0
                assertEquals(6400.0, response.getAmount());
                assertEquals("Honda Civic", response.getCar().getCarModel());
                assertEquals("Honda", response.getCar().getCarManufacturer());
                assertEquals(1, response.getDrivers().size());
                assertEquals("98765432100", response.getDrivers().get(0).getDriverDocument());
        }

        @Test
        @DisplayName("Deve retornar orçamento com valor maior para motorista jovem")
        void getBudget_WithYoungDriver_ReturnsHigherAmount() throws CustomBusinessException {
                // Act
                BudgetResponseDto response = budgetService.getBudget(youngDriverBudgetId);

                // Assert
                assertNotNull(response);
                assertEquals(3600.0, response.getAmount());
                assertEquals("Renault Kwid", response.getCar().getCarModel());
                assertEquals("Renault", response.getCar().getCarManufacturer());
                assertEquals(1, response.getDrivers().size());
                assertEquals("11122233344", response.getDrivers().get(0).getDriverDocument());
                assertEquals(LocalDate.now().minusYears(22), response.getDrivers().get(0).getDriverBirthdate());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar criar orçamento para carro sem motorista principal")
        void createBudget_WithCarWithoutMainDriver_ThrowsException() {
                // Arrange
                BudgetRequestDTO requestDTO = new BudgetRequestDTO();
                requestDTO.setCarId(carWithoutMainDriverId);
                requestDTO.setCustomerId(1); // Qualquer customer existente

                // Act & Assert
                assertThrows(NoMainDriverRegisteredException.class, () -> {
                        budgetService.createBudget(requestDTO);
                });
        }

        @Test
        @DisplayName("Deve calcular corretamente o orçamento com múltiplos fatores de risco")
        void getBudget_WithMultipleClaimsHistory_ReturnsCumulativeRiskAmount() throws CustomBusinessException {
                // Act
                BudgetResponseDto response = budgetService.getBudget(multipleClaimsBudgetId);

                // Assert
                assertNotNull(response);
                // Teste falha aqui - esperamos um valor de 12000.0 (12% de 100000.0), mas o
                // valor real no banco é 10000.0
                // Assumindo que cada fator de risco adiciona 2% e há um total de 3 fatores:
                // 6% (base) + 2% (sinistro no carro) + 2% (sinistro no motorista) + 2%
                // (sinistro adicional) = 12%
                assertEquals(12000.0, response.getAmount());
                assertEquals("Toyota Corolla", response.getCar().getCarModel());
                assertEquals("Toyota", response.getCar().getCarManufacturer());
                assertEquals(1, response.getDrivers().size());
                assertEquals("44433322211", response.getDrivers().get(0).getDriverDocument());
        }
}