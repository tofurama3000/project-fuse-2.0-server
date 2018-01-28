package server.repositories;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.UploadFile;

public interface FileRepository extends CrudRepository<UploadFile, Long> {
}
