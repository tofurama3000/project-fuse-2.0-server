package server.controllers.rest;

import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.Notification;
import server.repositories.NotificationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/feeds")
@Api(value = "Feed Endpoints")
public class FeedController {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  NotificationRepository notificationRepository;


  @GetMapping
  @ResponseBody
  public GeneralResponse getFeed(@ApiParam(value = "The page of results to pull")
                                 @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                 @ApiParam(value = "The number of results per page")
                                 @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    List<Notification> list = notificationRepository.getNotifications(session.get().getUser());
    List<Notification> returnList = new ArrayList<Notification>();
    for (int i = page * pageSize; i < (page * pageSize) + pageSize; i++) {
      if (i >= list.size()) {
        break;
      }
      returnList.add(list.get(i));
    }
    return new GeneralResponse(response, BaseResponse.Status.OK, null, returnList);
  }

}
