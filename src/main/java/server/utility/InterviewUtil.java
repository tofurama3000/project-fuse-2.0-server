package server.utility;

import server.entities.dto.group.interview.Interview;
import server.entities.user_to_group.permissions.UserToGroupPermission;

import java.time.LocalDateTime;
import java.util.List;

public class InterviewUtil {

  public static boolean isValidInterviewSlots(List<Interview> interviews, LocalDateTime currentDateTime, UserToGroupPermission permission) {

    if (interviews.size() <= 0) {
      return false;
    }

    if (!permission.canUpdate()) {
      return false;
    }

    for (Interview interview : interviews) {
      if (interview.getStartDateTime() == null || interview.getEndDateTime() == null) {
        return false;
      }
      if (interview.getStartDateTime().isAfter(interview.getEndDateTime())) {
        return false;
      }
      if (interview.getStartDateTime().isBefore(currentDateTime)) {
        return false;
      }
    }

    return true;
  }
}
