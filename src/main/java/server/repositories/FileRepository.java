package server.repositories;
import org.springframework.data.repository.CrudRepository;
import server.entities.dto.UploadFile;
import server.entities.dto.User;

public interface FileRepository extends CrudRepository<UploadFile, Long> {
}
