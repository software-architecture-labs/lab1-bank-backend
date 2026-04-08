package com.udea.lab1banco.service;

// =====================================================================
// IMPORTS
// JUnit 5: el framework de pruebas. @Test marca cada método como prueba.
// Mockito: nos permite crear "dobles" de nuestros repositorios para que
//          los tests no necesiten una BD real corriendo.
// =====================================================================
import com.udea.lab1banco.dto.CustomerDTO;
import com.udea.lab1banco.entity.Customer;
import com.udea.lab1banco.mapper.CustomerMapper;
import com.udea.lab1banco.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): le dice a JUnit que use Mockito
// para inicializar los @Mock y @InjectMocks antes de cada test.
// Sin esto, los campos anotados con @Mock serían null.
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    // ---------------------------------------------------------------
    // @Mock: Mockito crea un "doble" (fake) del repositorio.
    // Cuando el servicio llame a customerRepository.findById(1L),
    // en vez de ir a la BD, Mockito devolverá lo que nosotros
    // configuremos con "when(...).thenReturn(...)".
    // ---------------------------------------------------------------
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    // ---------------------------------------------------------------
    // @InjectMocks: Mockito crea una instancia REAL del servicio e
    // inyecta los @Mock anteriores como si fueran sus dependencias.
    // Es el equivalente a hacer "new CustomerService(repo, mapper)"
    // pero de forma automática.
    // ---------------------------------------------------------------
    @InjectMocks
    private CustomerService customerService;

    // Datos de prueba que reutilizamos en varios tests
    private Customer customer;
    private CustomerDTO customerDTO;

    // @BeforeEach: este método corre ANTES de cada @Test.
    // Aquí preparamos el estado inicial para no repetir código.
    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Juan");
        customer.setLastName("Pérez");
        customer.setAccountNumber("ACC-001");
        customer.setBalance(1000.0);

        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setFirstName("Juan");
        customerDTO.setLastName("Pérez");
        customerDTO.setAccountNumber("ACC-001");
        customerDTO.setBalance(1000.0);
    }

    // ---------------------------------------------------------------
    // TEST 1: Obtener todos los clientes (camino feliz)
    // Verifica que getAllCustomers() convierte la lista de entidades
    // a DTOs correctamente.
    // ---------------------------------------------------------------
    @Test
    void getAllCustomers_DebeRetornarListaDeDTOs() {
        // GIVEN (dado que...): configuramos el mock
        // "cuando el repo llame findAll(), devuelve esta lista"
        when(customerRepository.findAll()).thenReturn(List.of(customer));
        // "cuando el mapper convierta el customer, devuelve el DTO"
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        // WHEN (cuando...): ejecutamos el método que queremos probar
        List<CustomerDTO> resultado = customerService.getAllCustomers();

        // THEN (entonces...): verificamos que el resultado sea correcto
        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getFirstName());

        // Verificamos que el repositorio fue llamado exactamente 1 vez
        verify(customerRepository, times(1)).findAll();
    }

    // ---------------------------------------------------------------
    // TEST 2: Buscar cliente por ID que SÍ existe
    // ---------------------------------------------------------------
    @Test
    void getCustomerById_CuandoExiste_DebeRetornarDTO() {
        // GIVEN
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        // WHEN
        CustomerDTO resultado = customerService.getCustomerById(1L);

        // THEN
        assertNotNull(resultado);
        assertEquals("ACC-001", resultado.getAccountNumber());
    }

    // ---------------------------------------------------------------
    // TEST 3: Buscar cliente por ID que NO existe → debe lanzar excepción
    // Este test verifica el camino de error (muy importante en CI/CD).
    // assertThrows verifica que el método lanza la excepción esperada.
    // ---------------------------------------------------------------
    @Test
    void getCustomerById_CuandoNoExiste_DebeLanzarExcepcion() {
        // GIVEN: el repositorio devuelve un Optional vacío
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN: verificamos que se lanza RuntimeException
        RuntimeException excepcion = assertThrows(
                RuntimeException.class,
                () -> customerService.getCustomerById(99L)
        );

        // Verificamos también el mensaje de la excepción
        assertTrue(excepcion.getMessage().contains("Cliente no encontrado"));
    }

    // ---------------------------------------------------------------
    // TEST 4: Crear un cliente nuevo
    // ---------------------------------------------------------------
    @Test
    void createCustomer_DebeGuardarYRetornarDTO() {
        // GIVEN
        // "cualquier Customer" → any(Customer.class) es un matcher de Mockito
        // que acepta cualquier objeto de ese tipo, sin importar sus valores
        when(customerMapper.toEntity(customerDTO)).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        // WHEN
        CustomerDTO resultado = customerService.createCustomer(customerDTO);

        // THEN
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getFirstName());
        assertEquals(1000.0, resultado.getBalance());

        // Verificamos que save() fue llamado una vez
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    // ---------------------------------------------------------------
    // TEST 5: Eliminar cliente que SÍ existe
    // ---------------------------------------------------------------
    @Test
    void deleteCustomer_CuandoExiste_DebeEliminar() {
        // GIVEN
        when(customerRepository.existsById(1L)).thenReturn(true);
        // doNothing() le dice a Mockito que no haga nada cuando se llame deleteById
        // (porque el método es void, no podemos usar thenReturn)
        doNothing().when(customerRepository).deleteById(1L);

        // WHEN: no esperamos ningún valor de retorno, solo que no lance excepción
        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));

        // THEN: verificamos que deleteById fue llamado exactamente 1 vez
        verify(customerRepository, times(1)).deleteById(1L);
    }

    // ---------------------------------------------------------------
    // TEST 6: Eliminar cliente que NO existe → debe lanzar excepción
    // ---------------------------------------------------------------
    @Test
    void deleteCustomer_CuandoNoExiste_DebeLanzarExcepcion() {
        // GIVEN
        when(customerRepository.existsById(99L)).thenReturn(false);

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(
                RuntimeException.class,
                () -> customerService.deleteCustomer(99L)
        );

        assertTrue(excepcion.getMessage().contains("99"));

        // Verificamos que deleteById NUNCA fue llamado (evitamos borrar algo que no existe)
        verify(customerRepository, never()).deleteById(anyLong());
    }
}