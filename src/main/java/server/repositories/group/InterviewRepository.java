package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.group.interview.Interview;

public interface InterviewRepository extends CrudRepository<Interview, Long> {
}
