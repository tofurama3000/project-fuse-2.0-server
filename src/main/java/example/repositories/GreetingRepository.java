package example.repositories;

import example.dto.GreetingDTO;
import org.springframework.data.repository.CrudRepository;

public interface GreetingRepository extends CrudRepository<GreetingDTO, Long> {
}
