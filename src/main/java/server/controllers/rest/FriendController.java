package server.controllers.rest;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.GeneralResponse;
import server.entities.PossibleError;
import server.entities.dto.Friend;
import server.entities.dto.FuseSession;
import server.entities.dto.Notification;
import server.entities.dto.User;
import server.repositories.FriendRepository;
import server.repositories.UserRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static server.controllers.rest.response.CannedResponse.Friend_FOUND;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.GeneralResponse.Status.OK;

@Controller
@RequestMapping(value = "/friends")
@Api(value = "Friend Endpoints")
public class FriendController {
  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  FriendRepository friendRepository;

  @Autowired
  UserRepository userRepository;


  @Autowired
  NotificationController notificationController;

  @GetMapping
  @ResponseBody
  public GeneralResponse getFriends(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, friendRepository.getFriends(session.get().getUser()));
  }

  @GetMapping(path = "/applicants")
  @ResponseBody
  public GeneralResponse getFriendRequests(HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    return new GeneralResponse(response, GeneralResponse.Status.OK, null, friendRepository.getFriendApplicant(session.get().getUser()));
  }

  @CrossOrigin
  @PutMapping(path = "/accept/{id}")
  @ResponseBody
  public GeneralResponse acceptFriend(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if(friend==null){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,GeneralResponse.Status.DENIED , null);
    }
    friend.setStatus("accepted");
    return new GeneralResponse(response, OK, null);
  }

  @CrossOrigin
  @PutMapping(path = "/declined/{id}")
  @ResponseBody
  public GeneralResponse declineFriend(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if(friend==null){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,GeneralResponse.Status.DENIED , null);
    }
    friend.setStatus("declined");
    return new GeneralResponse(response, OK, null);
  }

  @CrossOrigin
  @PutMapping(path = "/delete/{id}")
  @ResponseBody
  public GeneralResponse deleteFriend(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if(friend==null){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,GeneralResponse.Status.DENIED , null);
    }
    friend.setDeleted(true);
    return new GeneralResponse(response, OK, null);
  }


  @PostMapping(path = "/{id}")
  @ResponseBody
  public GeneralResponse applyFriend(@PathVariable("id") Long id,  HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
    }
  User sender =session.get().getUser();

    List<Friend> list = friendRepository.getFriends(sender);
    for(Friend f : list){
      if(f.getReceiver().getId()==id||f.getSender().getId()==id){
        if(f.getStatus().equals("accepted")) {
          errors.add(Friend_FOUND);
          return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
        }
        if(f.getStatus().equals("applied")) {
          return new GeneralResponse(response, OK, null);
        }
      }
    }

    User receiver = userRepository.findOne(id);
    Friend friend = new Friend();
    friend.setSender(sender);
    friend.setDeleted(false);
    friend.setStatus("applied");
    friend.setReceiver(receiver);
    friend = friendRepository.save(friend);
    notificationController.sendNotification(receiver,sender +" wants to be your friend!","Friend",friend.getId());
    return new GeneralResponse(response, OK, null);
  }
}
