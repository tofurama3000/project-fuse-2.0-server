package server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class FileUploadConfig {

  @Bean(name = "multipartResolver")
  public CommonsMultipartResolver getCommonsMultipartResolver() {
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();

    multipartResolver.setMaxUploadSize(5242880);  //5MB
    multipartResolver.setMaxInMemorySize(5242880); //5MB
    multipartResolver.setDefaultEncoding("UTF-8");
    multipartResolver.setResolveLazily(true);
    return multipartResolver;
  }
}
