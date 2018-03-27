package server.repositories.group;

import static server.constants.Availability.AVAILABLE;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewRepository extends CrudRepository<Interview, Long> {
  @Query("FROM Interview a where a.groupId = :groupId and a.groupType = :groupType "
      + "and a.availability = '" + AVAILABLE + "' and a.startDateTime > :date and a.cancelled = 0")
  List<Interview> getAvailableInterviewsAfterDate(@Param("groupId") Long groupId, @Param("groupType") String groupType,
                                                  @Param("date") LocalDateTime date);

  @Query("FROM Interview a where a.groupId = :groupId and a.groupType = :groupType "
      + " and a.startDateTime >= :start and a.endDateTime <= :end and a.cancelled = 0")
  List<Interview> getAllInterviewsBetweenDates(@Param("groupId") Long groupId, @Param("groupType") String groupType,
                                                  @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("FROM Interview a where a.groupId = :groupId and a.groupType = :groupType and a.user = :user and a.cancelled = 0")
  List<Interview> getAllByUserAndGroupTypeAndGroup(@Param("user") User user,
                                                   @Param("groupType") String type,
                                                   @Param("groupId") Long groupId);

}
