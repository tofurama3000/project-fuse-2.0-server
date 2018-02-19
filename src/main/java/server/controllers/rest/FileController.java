package server.controllers.rest;

import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.ERROR;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;

import com.google.common.hash.Hashing;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.UploadFile;
import server.entities.dto.user.User;
import server.repositories.FileRepository;
import server.repositories.group.FileDownloadRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/files")
@Api(value = "File Endpoints")
public class FileController {
  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private FileDownloadRepository fileDownloadRepository;

  @Value("${fuse.fileUploadPath}")
  private String fileUploadPath;

  @PostMapping(path = "/upload")
  @ResponseBody
  @ApiOperation(value = "Uploads a new file",
      notes = "Max file size is 5MB")
  public TypedResponse<UploadFile> fileUpload(@RequestParam("file") MultipartFile fileToUpload, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<String> errors = new ArrayList<>();

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }
    User currentUser = session.get().getUser();
    if (fileToUpload != null) {
      if (fileToUpload.getSize() > 0 && fileToUpload.getName().equals("file")) {
        UploadFile savedResult = saveFile(fileToUpload, errors, currentUser);
        if (savedResult == null) {
          return new TypedResponse<>(response, ERROR, errors);
        }
        return new TypedResponse<>(response, OK, null, savedResult);
      }
    }
    errors.add("Invalid file, unable to save");
    return new TypedResponse<>(response, BAD_DATA, errors);
  }

  @GetMapping(path = "/download/{id}")
  @ResponseBody
  @ApiOperation(value = "Downloads a file",
      notes = "Will download as an attachment")
  public ResponseEntity<Resource> fileDownload(@PathVariable(value = "id") Long id, HttpServletResponse response, HttpServletRequest request) throws Exception {

    UploadFile fileToFind = fileDownloadRepository.findOne(id);
    if (fileToFind == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    String originalFileName = fileToFind.getFileName();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + originalFileName);
    String fileName = fileToFind.getHash() + "." + fileToFind.getUpload_time().getTime() + "." + fileToFind.getUser().getId();
    Path path = Paths.get(fileUploadPath, fileName);
    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
    File file = new File(fileUploadPath, fileName);

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(file.length())
        .contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
  }

  @GetMapping
  @ResponseBody
  public TypedResponse<Iterable<UploadFile>> getFiles(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, GeneralResponse.Status.DENIED, errors);
    }

    return new TypedResponse<>(response, OK, null, fileRepository.getUploadedFiles(session.get().getUser()));
  }

  public UploadFile saveFile(MultipartFile fileToUpload, List<String> errors, User currentUser) throws IOException {
    UploadFile uploadFile = new UploadFile();

    String hash = Hashing.sha256()
        .hashString(fileToUpload.getOriginalFilename(), StandardCharsets.UTF_8)
        .toString();
    Timestamp ts = new Timestamp(System.currentTimeMillis());
    long timestamp = ((ts.getTime()) / 1000) * 1000;
    String fileName = hash + "." + timestamp + "." + currentUser.getId().toString();
    File fileToSave = new File(fileUploadPath, fileName);
    if (!fileToSave.createNewFile()) {
      errors.add("Unable to create file.");
      return null;
    }
    fileToUpload.transferTo(fileToSave);
    uploadFile.setHash(hash);
    uploadFile.setUpload_time(new Timestamp(timestamp));
    uploadFile.setFile_size(fileToUpload.getSize());
    uploadFile.setFileName(fileToUpload.getOriginalFilename());
    uploadFile.setMime_type(fileToUpload.getContentType());
    uploadFile.setUser(currentUser);
    return fileRepository.save(uploadFile);
  }
}
