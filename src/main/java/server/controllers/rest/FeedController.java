package server.controllers.rest;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.Notification;
import server.repositories.NotificationRepository;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;

@Controller
@RequestMapping(value = "/feeds")
@Api(value = "Feed Endpoints")
public class FeedController {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  NotificationRepository notificationRepository;


  @GetMapping(path = "/{page}/{size}")
  @ResponseBody
  public GeneralResponse getFeed(@PathVariable(value = "page") int page, @PathVariable(value = "size") int size, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    List<Notification> list = notificationRepository.getNotifications(session.get().getUser());
    List<Notification> returnList = new ArrayList<Notification>();
    for(int i = page*size; i<(page*size)+size;i++){
      if(i>=list.size()){
        break;
      }
      returnList.add(list.get(i));
    }
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, returnList);
  }

}
