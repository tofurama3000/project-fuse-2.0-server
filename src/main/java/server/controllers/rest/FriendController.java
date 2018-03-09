package server.controllers.rest;

import static server.controllers.rest.response.BaseResponse.Status.ERROR;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import static server.controllers.rest.response.CannedResponse.FRIEND_FOUND;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.utility.PagingUtil.getPagedResults;
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
import server.entities.dto.Notification;
import server.entities.dto.user.Friendship;
import server.entities.dto.user.User;
import server.repositories.FriendRepository;
import server.repositories.NotificationRepository;
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
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  private Logger logger = LoggerFactory.getLogger(FriendController.class);

  @Autowired
  private NotificationController notificationController;

  @ApiOperation("Get all friends with paginate option")
  @GetMapping
  @ResponseBody
  public TypedResponse<List<Friendship>> getFriends(@ApiParam(value = "The page of results to pull")
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

    List<Friendship> allFriendships = friendRepository.getFriends(session.get().getUser());

    return new TypedResponse<>(response, BaseResponse.Status.OK, null, getPagedResults(allFriendships, page, pageSize));
  }

  @ApiOperation("Get all friends")
  @GetMapping("/all")
  @ResponseBody
  public TypedResponse<List<Friendship>> getFriendIds(HttpServletRequest request,
                                                      HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, errors);
    }
    List<Friendship> list = friendRepository.getAllFriends(session.get().getUser());
    return new TypedResponse<>(response, BaseResponse.Status.OK, null, list);
  }

  @ApiOperation("Get all friend applicants")
  @GetMapping(path = "/applicants")
  @ResponseBody
  public TypedResponse<List<Friendship>> getFriendRequests(@ApiParam(value = "The page of results to pull")
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
    List<Friendship> friendshipRequests = friendRepository.getFriendApplicant(session.get().getUser());

    return new TypedResponse<>(response, BaseResponse.Status.OK, null, getPagedResults(friendshipRequests, page, pageSize));
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
    if (isFriend(session.get().getUser(), id)) {
      return new GeneralResponse(response, BaseResponse.Status.DENIED, "Already friends");
    }

    Friendship friendship = friendRepository.findOne(id);
    if (friendship == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!friendship.getReceiver().getId().equals(session.get().getUser().getId())) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!friendship.getStatus().equals("applied")) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friendship.setStatus("accepted");
    try {
      notificationController.sendFriendshipAcceptedNotification(friendship);
      friendRepository.save(friendship);
    } catch (Exception e) {
      errors.add(e.getMessage());
      return new GeneralResponse(response, ERROR, errors);
    }
    notificationController.updateFriendshipRequestNotificationsFor(friendship);

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
    Friendship Friendship = friendRepository.findOne(id);
    if (Friendship == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!Friendship.getReceiver().getId().equals(session.get().getUser().getId())) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    if (!Friendship.getStatus().equals("applied")) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Friendship.setStatus("declined");
    friendRepository.save(Friendship);

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
    Friendship friendship = friendRepository.findOne(id);
    if (friendship == null) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED);
    }
    if (!friendship.getStatus().equals("accepted")) {
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friendship.setStatus("deleted");
    friendRepository.save(friendship);
    return new GeneralResponse(response, OK);
  }


  @ApiOperation("Send a friend invite")
  @PostMapping(path = "/{id}")
  @ResponseBody
  public BaseResponse sendFriendInvite(@ApiParam(name = "ID of user to send a request to") @PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
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

    long count = friendRepository.getAllFriends(sender).stream()
        .filter(friendship -> friendship.getId().equals(id) && friendship.getStatus().equals("applied")).count();

    if (count != 0) {
      return new GeneralResponse(response, BaseResponse.Status.DENIED, "Already sent friend request");
    }

    User receiver = userRepository.findOne(id);
    Friendship friendship = new Friendship();
    friendship.setSender(sender);
    friendship.setStatus("applied");
    friendship.setReceiver(receiver);
    try {
      notificationController.sendFriendshipRequestNotification(friendship);
      friendRepository.save(friendship);
    } catch (Exception e) {
      errors.add(e.getMessage());
      return new GeneralResponse(response, ERROR, errors);
    }
    return new GeneralResponse(response, OK);
  }

  private boolean isFriend(User user, long id) {
    List<Friendship> Friendships = friendRepository.getFriends(user);
    for (Friendship Friendship : Friendships) {
      if (Friendship.getReceiver().getId() == id || Friendship.getSender().getId() == id) {
        if (Friendship.getStatus().equals("accepted")) {
          return true;
        }
      }
    }
    return false;
  }
}
