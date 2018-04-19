package server.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import server.entities.dto.UploadFile;
import server.entities.dto.user.User;

import java.util.List;

public interface FileRepository extends CrudRepository<UploadFile, Long> {
  @Query("FROM UploadFile a where a.user = :user")
  List<UploadFile> getUploadedFiles(@Param("user") User user);
}
