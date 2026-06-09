package backoffice.v1.dtos.billing;

import backoffice.v1.entities.enums.MemberStatusEnum;

public enum SubscriberBillingListStatusFilter {
  ALL,
  ACTIVE,
  DUE_SOON,
  OVERDUE,
  INACTIVE;

  public MemberStatusEnum resolveMemberStatusOrNull() {
    return switch (this) {
      case ALL -> null;
      case ACTIVE -> MemberStatusEnum.ACTIVE;
      case DUE_SOON -> MemberStatusEnum.DUE_SOON;
      case OVERDUE -> MemberStatusEnum.OVERDUE;
      case INACTIVE -> MemberStatusEnum.INACTIVE;
    };
  }
}
