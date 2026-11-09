package ua.edu.hotels.service;

import org.slf4j.Logger;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Logs successful mutations only after their transaction commits. */
final class TransactionLog {
  private TransactionLog() {}

  static void info(Logger logger, String message, Object... arguments) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        logger.info(message, arguments);
      }
    });
  }
}
