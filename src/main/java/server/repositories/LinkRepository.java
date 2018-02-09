package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.Link;

public interface LinkRepository extends CrudRepository<Link, Long> {

}
