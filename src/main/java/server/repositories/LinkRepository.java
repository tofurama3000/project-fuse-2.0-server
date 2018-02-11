package server.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.Link;

import java.util.List;

public interface LinkRepository extends CrudRepository<Link, Long> {
  @Query("FROM Link a where a.referencedId = :id and a.referencedType = :type ORDER BY a.name")
  List<Link> getLinksWithIdOfType(@Param("id") Long id, @Param("type") String type);
}
