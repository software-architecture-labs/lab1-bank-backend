package com.udea.lab1banco.service;

import com.udea.lab1banco.dto.TransactionDTO;
import com.udea.lab1banco.entity.Customer;
import com.udea.lab1banco.entity.Transaction;
import com.udea.lab1banco.repository.CustomerRepository;
import com.udea.lab1banco.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionService transactionService;

    // Clientes y transacciones de prueba
    private Customer sender;
    private Customer receiver;
    private Transaction transaction;
    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        // Cuenta origen con saldo suficiente para las pruebas normales
        sender = new Customer();
        sender.setId(1L);
        sender.setFirstName("Ana");
        sender.setLastName("García");
        sender.setAccountNumber("ACC-001");
        sender.setBalance(5000.0);

        // Cuenta destino
        receiver = new Customer();
        receiver.setId(2L);
        receiver.setFirstName("Carlos");
        receiver.setLastName("López");
        receiver.setAccountNumber("ACC-002");
        receiver.setBalance(1000.0);

        // Entidad que representa una transacción guardada en BD
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSenderAccountNumber("ACC-001");
        transaction.setReceiverAccountNumber("ACC-002");
        transaction.setAmount(500.0);
        transaction.setTimestamp(LocalDateTime.now());

        // DTO que llega desde el cliente HTTP (el "request")
        transactionDTO = new TransactionDTO();
        transactionDTO.setSenderAccountNumber("ACC-001");
        transactionDTO.setReceiverAccountNumber("ACC-002");
        transactionDTO.setAmount(500.0);
        transactionDTO.setTimestamp(LocalDateTime.now());
    }

    // ---------------------------------------------------------------
    // TEST 1: Transferencia exitosa (camino feliz)
    // Este es el caso más importante: verifica que el dinero se
    // descuenta del remitente y se acredita al receptor.
    // ---------------------------------------------------------------
    @Test
    void transferMoney_CuandoHaySaldo_DebeTransferir() {
        // GIVEN
        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("ACC-002")).thenReturn(Optional.of(receiver));
        // save() de customerRepository puede recibir cualquier Customer
        when(customerRepository.save(any(Customer.class))).thenReturn(sender);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // WHEN
        TransactionDTO resultado = transactionService.transferMoney(transactionDTO);

        // THEN: verificamos que la transacción se creó correctamente
        assertNotNull(resultado);
        assertEquals("ACC-001", resultado.getSenderAccountNumber());
        assertEquals("ACC-002", resultado.getReceiverAccountNumber());
        assertEquals(500.0, resultado.getAmount());

        // -------------------------------------------------------
        // VERIFICACIÓN CRUCIAL: que los saldos se actualizaron
        // El remitente tenía 5000 y transfirió 500 → debe quedar 4500
        // El receptor tenía 1000 y recibió 500 → debe quedar 1500
        // -------------------------------------------------------
        assertEquals(4500.0, sender.getBalance());
        assertEquals(1500.0, receiver.getBalance());

        // Verificamos que se guardaron ambas cuentas (2 llamadas a save)
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // ---------------------------------------------------------------
    // TEST 2: Saldo insuficiente → debe rechazar la transferencia
    // Si el remitente no tiene fondos, la transferencia no debe
    // ejecutarse y los saldos deben quedar intactos.
    // ---------------------------------------------------------------
    @Test
    void transferMoney_CuandoSaldoInsuficiente_DebeLanzarExcepcion() {
        // GIVEN: modificamos el saldo del remitente para que sea menor al monto
        sender.setBalance(100.0); // tiene 100, quiere transferir 500

        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("ACC-002")).thenReturn(Optional.of(receiver));

        // WHEN & THEN
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transferMoney(transactionDTO)
        );

        assertTrue(excepcion.getMessage().contains("Saldo insuficiente"));

        // Verificamos que NADA se guardó en BD (rollback implícito)
        verify(customerRepository, never()).save(any(Customer.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ---------------------------------------------------------------
    // TEST 3: Cuenta de origen no existe
    // ---------------------------------------------------------------
    @Test
    void transferMoney_CuandoCuentaOrigenNoExiste_DebeLanzarExcepcion() {
        // GIVEN: el repositorio no encuentra la cuenta origen
        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.empty());

        // WHEN & THEN
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transferMoney(transactionDTO)
        );

        assertTrue(excepcion.getMessage().contains("remitente"));
    }

    // ---------------------------------------------------------------
    // TEST 4: Cuenta de destino no existe
    // ---------------------------------------------------------------
    @Test
    void transferMoney_CuandoCuentaDestinoNoExiste_DebeLanzarExcepcion() {
        // GIVEN: la cuenta origen sí existe, pero la destino no
        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("ACC-002")).thenReturn(Optional.empty());

        // WHEN & THEN
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transferMoney(transactionDTO)
        );

        assertTrue(excepcion.getMessage().contains("receptor"));
    }

    // ---------------------------------------------------------------
    // TEST 5: Obtener todas las transacciones
    // ---------------------------------------------------------------
    @Test
    void getAllTransactions_DebeRetornarLista() {
        // GIVEN
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        // WHEN
        List<TransactionDTO> resultado = transactionService.getAllTransactions();

        // THEN
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals(500.0, resultado.get(0).getAmount());
    }

    // ---------------------------------------------------------------
    // TEST 6: Obtener transacción por ID → existe
    // ---------------------------------------------------------------
    @Test
    void getTransactionById_CuandoExiste_DebeRetornarDTO() {
        // GIVEN
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        // WHEN
        TransactionDTO resultado = transactionService.getTransactionById(1L);

        // THEN
        assertNotNull(resultado);
        assertEquals(500.0, resultado.getAmount());
    }

    // ---------------------------------------------------------------
    // TEST 7: Obtener transacción por ID → NO existe
    // ---------------------------------------------------------------
    @Test
    void getTransactionById_CuandoNoExiste_DebeLanzarExcepcion() {
        // GIVEN
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getTransactionById(99L)
        );

        assertTrue(excepcion.getMessage().contains("no encontrada"));
    }

    // ---------------------------------------------------------------
    // TEST 8: Buscar transacciones por número de cuenta
    // ---------------------------------------------------------------
    @Test
    void getTransactionsByAccount_DebeRetornarTransaccionesFiltradas() {
        // GIVEN
        when(transactionRepository
                .findBySenderAccountNumberOrReceiverAccountNumber("ACC-001", "ACC-001"))
                .thenReturn(List.of(transaction));

        // WHEN
        List<TransactionDTO> resultado = transactionService.getTransactionsByAccount("ACC-001");

        // THEN
        assertEquals(1, resultado.size());
        assertEquals("ACC-001", resultado.get(0).getSenderAccountNumber());
    }

    // ---------------------------------------------------------------
    // TEST 9: Eliminar transacción → existe
    // ---------------------------------------------------------------
    @Test
    void deleteTransaction_CuandoExiste_DebeEliminar() {
        // GIVEN
        when(transactionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(1L);

        // WHEN & THEN
        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L));
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    // ---------------------------------------------------------------
    // TEST 10: Test de performance básico
    // Verifica que getAllTransactions no tarde más de 1 segundo.
    // En el lab2, el profesor incluye un test similar con 2 segundos.
    // ---------------------------------------------------------------
    @Test
    void getAllTransactions_DebeSerRapido() {
        // GIVEN
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));

        // WHEN
        long inicio = System.currentTimeMillis();
        transactionService.getAllTransactions();
        long duracion = System.currentTimeMillis() - inicio;

        // THEN
        System.out.println("Tiempo de ejecución: " + duracion + "ms");
        assertTrue(duracion < 1000, "El método tardó más de 1 segundo: " + duracion + "ms");
    }
}
