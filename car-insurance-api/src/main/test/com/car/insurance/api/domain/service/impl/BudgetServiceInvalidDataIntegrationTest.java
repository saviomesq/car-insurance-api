package com.car.insurance.api.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.car.insurance.api.domain.Car;
import com.car.insurance.api.domain.CarDriver;
import com.car.insurance.api.domain.Customer;
import com.car.insurance.api.domain.Driver;
import com.car.insurance.api.domain.dto.BudgetRequestDTO;
import com.car.insurance.api.domain.exception.CustomBusinessException;
import com.car.insurance.api.domain.exception.NoMainDriverRegisteredException;
import com.car.insurance.api.domain.repository.CarDriverRepository;
import com.car.insurance.api.domain.repository.CarRepository;
import com.car.insurance.api.domain.repository.CustomerRepository;
import com.car.insurance.api.domain.repository.DriverRepository;
import com.car.insurance.api.domain.service.BudgetService;

import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BudgetServiceInvalidDataIntegrationTest {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private CarDriverRepository carDriverRepository;

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar orçamento com ID de carro inexistente")
    public void shouldThrowExceptionWhenCarIdDoesNotExist() {
        // Arrange
        Customer customer = createAndSaveCustomer();
        BudgetRequestDTO requestDTO = BudgetRequestDTO.builder()
                .carId(999999) // ID inexistente
                .customerId(customer.getId())
                .build();
        
        // Act & Assert
        assertThrows(CustomBusinessException.class, () -> {
            budgetService.createBudget(requestDTO);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar orçamento com ID de cliente inexistente")
    public void shouldThrowExceptionWhenCustomerIdDoesNotExist() {
        // Arrange
        Car car = createCarWithMainDriver();
        BudgetRequestDTO requestDTO = BudgetRequestDTO.builder()
                .carId(car.getId())
                .customerId(999999) // ID inexistente
                .build();
        
        // Act & Assert
        assertThrows(CustomBusinessException.class, () -> {
            budgetService.createBudget(requestDTO);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar orçamento para carro sem condutor principal")
    public void shouldThrowExceptionWhenCarHasNoMainDriver() {
        // Arrange
        Car car = createCarWithoutMainDriver();
        Customer customer = createAndSaveCustomer();
        
        BudgetRequestDTO requestDTO = BudgetRequestDTO.builder()
                .carId(car.getId())
                .customerId(customer.getId())
                .build();
        
        // Act & Assert
        assertThrows(NoMainDriverRegisteredException.class, () -> {
            budgetService.createBudget(requestDTO);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar orçamento inexistente")
    public void shouldThrowExceptionWhenUpdatingNonExistentBudget() {
        // Arrange
        Car car = createCarWithMainDriver();
        
        BudgetRequestDTO requestDTO = BudgetRequestDTO.builder()
                .budgetId(999999) // ID inexistente
                .carId(car.getId())
                .build();
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            budgetService.updateBudget(requestDTO);
        });
    }

    // Métodos auxiliares para criar entidades para os testes
    
    private Customer createAndSaveCustomer() {
        Customer customer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .document("12345678900")
                .build();
        
        return customerRepository.save(customer);
    }
    
    private Car createCarWithMainDriver() {
        // Criar e salvar o carro
        Car car = Car.builder()
                .manufacturer("Toyota")
                .model("Corolla")
                .releaseYear(2020)
                .fipeValue(60000.0)
                .build();
        car = carRepository.save(car);
        
        // Criar e salvar o motorista
        Driver driver = Driver.builder()
                .name("Main Driver")
                .document("98765432100")
                .birthdate(LocalDate.of(1990, 5, 15))
                .build();
        driver = driverRepository.save(driver);
        
        // Associar o motorista ao carro como condutor principal
        CarDriver carDriver = CarDriver.builder()
                .car(car)
                .driver(driver)
                .mainDriver(true)
                .build();
        carDriverRepository.save(carDriver);
        
        // Atualizar o objeto carro com a relação
        car = carRepository.findById(car.getId()).get();
        return car;
    }
    
    private Car createCarWithoutMainDriver() {
        // Criar e salvar o carro
        Car car = Car.builder()
                .manufacturer("Honda")
                .model("Civic")
                .releaseYear(2021)
                .fipeValue(70000.0)
                .build();
        car = carRepository.save(car);
        
        // Criar e salvar o motorista
        Driver driver = Driver.builder()
                .name("Secondary Driver")
                .document("11122233344")
                .birthdate(LocalDate.of(1985, 10, 20))
                .build();
        driver = driverRepository.save(driver);
        
        // Associar o motorista ao carro como condutor secundário (não principal)
        CarDriver carDriver = CarDriver.builder()
                .car(car)
                .driver(driver)
                .mainDriver(false) // Não é o condutor principal
                .build();
        carDriverRepository.save(carDriver);
        
        // Atualizar o objeto carro com a relação
        car = carRepository.findById(car.getId()).get();
        return car;
    }
}