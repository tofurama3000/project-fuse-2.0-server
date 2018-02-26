package server.controllers.rest;

import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.FRIEND_FOUND;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.user.Friend;
import server.entities.dto.user.User;
import server.repositories.FriendRepository;
import server.repositories.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping(value = "/friends")
@Api(value = "Friend Endpoints")
@SuppressWarnings("unused")
public class FriendController {
  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private FriendRepository friendRepository;

  @Autowired
  private UserRepository userRepository;

  private Logger logger = LoggerFactory.getLogger(FriendController.class);

  @Autowired
  private NotificationController notificationController;

  @ApiOperation("Get all friends with paginate option")
  @GetMapping
  @ResponseBody
  public TypedResponse<List<Friend>> getFriends(@ApiParam(value = "The page of results to pull")
                                                @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                @ApiParam(value = "The number of results per page")
                                                @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }

    List<Friend> list = friendRepository.getFriends(session.get().getUser());
    List<Friend> returnList = new ArrayList<>();
    for (int i = page * pageSize; i < (page * pageSize) + pageSize; i++) {
      if (i >= list.size()) {
        break;
      }
      returnList.add(list.get(i));
    }

    return new TypedResponse<>(response, BaseResponse.Status.OK, null, returnList);
  }

  @ApiOperation("Get all friends")
  @GetMapping("/all")
  @ResponseBody
  public TypedResponse<List<Friend>> getFriendIds(HttpServletRequest request,
                                                  HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }
    List<Friend> list = friendRepository.getAllFriends(session.get().getUser());
    return new TypedResponse<>(response, BaseResponse.Status.OK, null, list);
  }

  @ApiOperation("Get all friend applicants")
  @GetMapping(path = "/applicants")
  @ResponseBody
  public TypedResponse<List<Friend>> getFriendRequests(@ApiParam(value = "The page of results to pull")
                                                       @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                       @ApiParam(value = "The number of results per page")
                                                       @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize,
                                                       HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }
    List<Friend> list = friendRepository.getFriendApplicant(session.get().getUser());
    List<Friend> returnList = new ArrayList<>();
    for (int i = page * pageSize; i < (page * pageSize) + pageSize; i++) {
      if (i >= list.size()) {
        break;
      }
      returnList.add(list.get(i));
    }
    return new TypedResponse<>(response, BaseResponse.Status.OK, null, returnList);
  }

  @CrossOrigin
  @ApiOperation("Accept a friend invite")
  @PutMapping(path = "/accept/{id}")
  @ResponseBody
  public BaseResponse acceptFriend(@ApiParam(name = "ID of pending friendship to accept") @PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if (friend == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!friend.getReceiver().getId().equals(session.get().getUser().getId())) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!friend.getStatus().equals("applied")) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friend.setStatus("accepted");
    try {
      notificationController.sendNotification(friend.getSender(), friend.getReceiver().getName() + " has accepted your friend request", "Friend:Accepted", friend.getId());
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @ApiOperation("Decline a pending friend invite")
  @PutMapping(path = "/declined/{id}")
  @ResponseBody
  public BaseResponse declineFriend(@ApiParam("ID of pending friendship to decline") @PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if (friend == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!friend.getReceiver().getId().equals(session.get().getUser().getId())) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!friend.getStatus().equals("applied")) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friend.setStatus("declined");
    return new GeneralResponse(response, OK);
  }

  @ApiOperation(value = "Delete Friend")
  @CrossOrigin
  @PutMapping(path = "/delete/{id}")
  @ResponseBody
  public BaseResponse deleteFriend(
      @ApiParam("ID of user to delete")
      @PathVariable(value = "id") Long id,
      HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if (friend == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED);
    }
    if (!friend.getStatus().equals("accepted")) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friend.setStatus("deleted");
    friendRepository.save(friend);
    return new GeneralResponse(response, OK);
  }


  @ApiOperation("Send a friend invite")
  @PostMapping(path = "/{id}")
  @ResponseBody
  public BaseResponse applyFriend(@ApiParam(name = "ID of user to send a request to") @PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    User sender = session.get().getUser();
    if (Objects.equals(sender.getId(), id)) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (isFriend(sender, id)) {
      errors.add(FRIEND_FOUND);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    User receiver = userRepository.findOne(id);
    Friend friend = new Friend();
    friend.setSender(sender);
    friend.setStatus("applied");
    friend.setReceiver(receiver);
    friendRepository.save(friend);
    try {
      notificationController.sendNotification(receiver, sender.getName() + " wants to be your friend!",
          "Friend:Request",
          sender.getId()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new GeneralResponse(response, OK);
  }

  private boolean isFriend(User user, long id) {
    List<Friend> list = friendRepository.getFriends(user);
    for (Friend f : list) {
      if (f.getReceiver().getId() == id || f.getSender().getId() == id) {
        if (f.getStatus().equals("accepted")) {
          return true;
        }
      }
    }
    return false;
  }
}
