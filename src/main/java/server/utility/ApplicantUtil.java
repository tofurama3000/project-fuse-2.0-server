package server.utility;

import server.entities.dto.group.GroupApplicant;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicantUtil {
  public static <T extends GroupApplicant> List<T> filterApplicants(List<T> applicants, final String not_status) {
    return applicants.stream()
        .filter(projectApplicant -> projectApplicant.getStatus().compareToIgnoreCase(not_status) != 0)
        .sorted((o1, o2) -> {
          final Integer status1 = GroupApplicant.GetStatusOrder(o1.getStatus());
          final Integer status2 = GroupApplicant.GetStatusOrder(o2.getStatus());
          final int statusComp = status1.compareTo(status2);
          if (statusComp != 0) {
            return statusComp;
          }
          return o1.getGroup().getId().compareTo(o2.getGroup().getId());
        })
        .filter(StreamUtil.uniqueByFunction(projectApplicant -> projectApplicant.getGroup().getId()))
        .collect(Collectors.toList());
  }
}
