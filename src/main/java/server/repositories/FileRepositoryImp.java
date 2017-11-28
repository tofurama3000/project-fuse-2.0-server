package server.repositories;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.UploadFile;
import server.repositories.FileRepository;

@Repository
public class FileRepositoryImp implements FileRepository {

    @Autowired
    private SessionFactory sessionFactory;

    public FileRepositoryImp() {

    }

    public FileRepositoryImp(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public void save(UploadFile file) {
        sessionFactory.getCurrentSession().save(file);
    }
}
