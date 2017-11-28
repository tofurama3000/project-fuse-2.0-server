package server.repositories;
import server.entities.dto.UploadFile;

public interface FileRepository{
    void save(UploadFile file);
}
