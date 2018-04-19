package server.repositories.group;

import org.springframework.data.repository.CrudRepository;
import server.entities.dto.UploadFile;

public interface FileDownloadRepository extends CrudRepository<UploadFile, Long> {
}
