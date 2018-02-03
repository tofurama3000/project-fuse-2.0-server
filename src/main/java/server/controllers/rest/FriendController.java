package server.controllers.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
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
import java.util.Objects;
import java.util.Optional;

import static server.controllers.rest.response.CannedResponse.Friend_FOUND;
import static server.controllers.rest.response.CannedResponse.INVALID_FIELDS;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;
import static server.controllers.rest.response.BaseResponse.Status.OK;

@Controller
@RequestMapping(value = "/friends")
@Api(value = "Friend Endpoints")
@SuppressWarnings("unused")
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
  public TypedResponse<List<Friend>> getFriends(@ApiParam(value="The page of results to pull")
                                       @RequestParam(value = "page", required=false, defaultValue="0") int page,
                                                @ApiParam(value="The number of results per page")
                                       @RequestParam(value = "size", required=false, defaultValue="15") int pageSize,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse(response, BaseResponse.Status.DENIED, errors);
    }

    List<Friend> list =  friendRepository.getFriends(session.get().getUser());
    List<Friend> returnList = new ArrayList<Friend>();
    for(int i = page*pageSize; i<(page*pageSize)+pageSize;i++){
      if(i>=list.size()){
        break;
      }
      returnList.add(list.get(i));
    }

    return new TypedResponse<>(response, BaseResponse.Status.OK, null,returnList);
  }

  @GetMapping(path = "/applicants")
  @ResponseBody
  public TypedResponse<List<Friend>>  getFriendRequests(@ApiParam(value="The page of results to pull")
                                                          @RequestParam(value = "page", required=false, defaultValue="0") int page,
                                                        @ApiParam(value="The number of results per page")
                                                          @RequestParam(value = "size", required=false, defaultValue="15") int pageSize,
                                                        HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new TypedResponse(response, BaseResponse.Status.DENIED, errors);
    }
    List<Friend> list =  friendRepository.getFriendApplicant(session.get().getUser());
    List<Friend> returnList = new ArrayList<Friend>();
    for(int i = page*pageSize; i<(page*pageSize)+pageSize;i++){
      if(i>=list.size()){
        break;
      }
      returnList.add(list.get(i));
    }
    return new TypedResponse<>(response, BaseResponse.Status.OK, null, returnList);
  }

  @CrossOrigin
  @PutMapping(path = "/accept/{id}")
  @ResponseBody
  public BaseResponse acceptFriend(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if(friend==null){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,BaseResponse.Status.DENIED , errors);
    }
    if(friend.getReceiver().getId()!=session.get().getUser().getId()){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,BaseResponse.Status.DENIED , errors);
    }
    if (!friend.getStatus().equals("applied")){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friend.setStatus("accepted");
    notificationController.sendNotification(friend.getSender(),friend.getReceiver().getName() + " has accepted your friend request","Friend: accepted", friend.getId());
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @PutMapping(path = "/declined/{id}")
  @ResponseBody
  public BaseResponse declineFriend(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if(friend==null){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,BaseResponse.Status.DENIED , errors);
    }
    if(friend.getReceiver().getId()!=session.get().getUser().getId()){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,BaseResponse.Status.DENIED , errors);
    }
    if (!friend.getStatus().equals("applied")){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friend.setStatus("declined");
    notificationController.sendNotification(friend.getSender(),friend.getReceiver().getName() + " has declined your friend request","Friend: declined", friend.getId());
    return new GeneralResponse(response, OK);
  }

  @CrossOrigin
  @PutMapping(path = "/delete/{id}")
  @ResponseBody
  public BaseResponse deleteFriend(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    Friend friend = friendRepository.findOne(id);
    if(friend==null){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response,BaseResponse.Status.DENIED);
    }
    if (!friend.getStatus().equals("accepted")){
      errors.add(INVALID_FIELDS);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
    friend.setStatus("deleted");
    friendRepository.save(friend);
    return new GeneralResponse(response, OK);
  }


  @PostMapping(path = "/{id}")
  @ResponseBody
  public BaseResponse applyFriend(@PathVariable("id") Long id,  HttpServletRequest request, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();
    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      errors.add(INVALID_SESSION);
      return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
    }
  User sender =session.get().getUser();
  if(Objects.equals(sender.getId(), id)){
    errors.add(INVALID_FIELDS);
    return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
  }
  if (isFriend(sender,id)){
    errors.add(Friend_FOUND);
    return new GeneralResponse(response, BaseResponse.Status.DENIED, errors);
  }
    User receiver = userRepository.findOne(id);
    Friend friend = new Friend();
    friend.setSender(sender);
    friend.setStatus("applied");
    friend.setReceiver(receiver);
    friend = friendRepository.save(friend);
    notificationController.sendNotification(receiver,sender.getName() +" wants to be your friend!","Friend",friend.getId());
    return new GeneralResponse(response, OK);
  }

  private boolean isFriend(User user, long id){
    List<Friend> list = friendRepository.getFriends(user);
    for(Friend f : list){
      if(f.getReceiver().getId()==id||f.getSender().getId()==id){
        if(f.getStatus().equals("accepted")) {
          return true;
        }
      }
    }
    return false;
  }
}
