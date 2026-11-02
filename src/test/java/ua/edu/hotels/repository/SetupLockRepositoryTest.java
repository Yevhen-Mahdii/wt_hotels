package ua.edu.hotels.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Verifies the real database lock across independent transactions, including rollback. */
@SpringBootTest(properties = {
  "spring.datasource.url=jdbc:h2:mem:setup-lock;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000",
  "app.jwt-key-file=./target/setup-lock-test-jwt.key"
})
class SetupLockRepositoryTest {
  @Autowired SetupLockRepository repository;
  @Autowired PlatformTransactionManager transactionManager;

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void serializesTransactionsUntilCommitOrRollback(boolean rollback) throws Exception {
    var transactions = new TransactionTemplate(transactionManager);
    var acquired = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    var contenderStarted = new CountDownLatch(1);
    var executor = Executors.newFixedThreadPool(2);
    try {
      var holder = executor.submit(() -> transactions.executeWithoutResult(status -> {
        repository.lock();
        acquired.countDown();
        await(release);
        if (rollback) status.setRollbackOnly();
      }));
      assertThat(acquired.await(5, TimeUnit.SECONDS)).isTrue();
      var contender = executor.submit(() -> transactions.executeWithoutResult(status -> {
        contenderStarted.countDown();
        repository.lock();
      }));
      assertThat(contenderStarted.await(5, TimeUnit.SECONDS)).isTrue();
      assertThatThrownBy(() -> contender.get(250, TimeUnit.MILLISECONDS))
          .isInstanceOf(TimeoutException.class);
      release.countDown();
      holder.get(5, TimeUnit.SECONDS);
      contender.get(5, TimeUnit.SECONDS);
    } finally {
      release.countDown();
      executor.shutdownNow();
      assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS)) throw new AssertionError("Lock release timed out");
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError(exception);
    }
  }
}
