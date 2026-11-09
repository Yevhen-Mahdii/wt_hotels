package ua.edu.hotels.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.*;

class TransactionLogTest {
  private final Logger logger = mock(Logger.class);

  @AfterEach
  void cleanUp() {
    if (TransactionSynchronizationManager.isSynchronizationActive())
      TransactionSynchronizationManager.clearSynchronization();
  }

  @Test
  void logsOnlyAfterCommit() {
    TransactionSynchronizationManager.initSynchronization();
    TransactionLog.info(logger, "Booking created: bookingId={}", 42L);
    verifyNoInteractions(logger);
    TransactionSynchronizationManager.getSynchronizations().forEach(
      TransactionSynchronization::afterCommit);
    verify(logger).info("Booking created: bookingId={}", new Object[] {42L});
  }

  @Test
  void doesNotReportRolledBackMutationAsSuccessful() {
    TransactionSynchronizationManager.initSynchronization();
    TransactionLog.info(logger, "Booking created: bookingId={}", 42L);
    TransactionSynchronizationManager.getSynchronizations().forEach(
      synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
    verifyNoInteractions(logger);
  }

  @Test
  void doesNotReportSuccessWithoutTransaction() {
    TransactionLog.info(logger, "Booking created: bookingId={}", 42L);
    verifyNoInteractions(logger);
  }
}
