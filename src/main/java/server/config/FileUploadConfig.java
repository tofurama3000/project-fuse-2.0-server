package server.config;

import org.hibernate.SessionFactory;
import server.repositories.FileRepository;
import server.repositories.FileRepositoryImp;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@SpringBootConfiguration
public class FileUploadConfig {

    @Autowired
    @Bean(name = "FileRepository")
    public FileRepository getUser(SessionFactory sessionFactory) {
        return new FileRepositoryImp(sessionFactory);
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getCommonsMultipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(104857600);  //100MB
        multipartResolver.setMaxInMemorySize(1048576); //1MB
        multipartResolver.setDefaultEncoding("UTF-8");
        multipartResolver.setResolveLazily(true);
        return multipartResolver;
    }
}
