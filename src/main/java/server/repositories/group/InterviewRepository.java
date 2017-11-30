package server.repositories.group;

import static server.constants.Availability.AVAILABLE;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.group.interview.Interview;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewRepository extends CrudRepository<Interview, Long> {
  @Query("FROM Interview a where a.groupId = :groupId and a.groupType = :groupType "
      + "and a.availability = '" + AVAILABLE + "' and a.startDateTime > :date")
  List<Interview> getAvailableInterviewsAfterDate(@Param("groupId") Long groupId, @Param("groupType") String groupType,
                                                  @Param("date") LocalDateTime date);
}
