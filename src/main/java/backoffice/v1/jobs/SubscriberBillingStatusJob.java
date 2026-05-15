package backoffice.v1.jobs;

import backoffice.v1.services.MemberBillingService;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
public class SubscriberBillingStatusJob {

  @Inject
  MemberBillingService memberBillingService;

  @Scheduled(cron = "${backoffice.billing.status-cron}", concurrentExecution = ConcurrentExecution.SKIP)
  void refreshStatuses() {

    memberBillingService.refreshBillingStatuses();
  }
}
