package server.controllers.rest;

import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.DENIED;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.errors.BadDataException;
import server.controllers.rest.errors.DeniedException;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.TimeInterval;
import server.entities.dto.group.project.ProjectInterviewSlots;
import server.entities.dto.user.ProjectNumMember;
import server.entities.dto.user.User;
import server.entities.dto.user.UserInterviewSlots;
import server.entities.dto.user.UserProjectCount;
import server.handlers.GroupMemberHelper;
import server.handlers.InterviewSlotsHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/statistics")
@Api(tags = "Statistics")
public class StatisticsController {

  @Autowired
  private FuseSessionController sessionController;

  @Autowired
  private InterviewSlotsHelper interviewSlotsHelper;

  @Autowired
  private GroupMemberHelper groupMemberHelper;

  @GetMapping("organizations/{id}/projects/interviews")
  @ResponseBody
  @ApiOperation("Returns all projects associated with an organization")
  public TypedResponse<List<ProjectInterviewSlots>> getInterviewSlotsForAllProjects(@ApiParam("Id of the organization")
                                                                                    @PathVariable(value = "id") Long id,
                                                                                    @RequestParam(value = "time", required = false) TimeInterval timeInterval,
                                                                                    HttpServletRequest request, HttpServletResponse response)

  {
    if (timeInterval == null) {
      timeInterval = new TimeInterval();
    }
    try {
      User user = sessionController.getUserFromSession(request);
      return new TypedResponse<>(response, interviewSlotsHelper
          .getInterviewSlotsForAllProjectsInOrganization(id, user, timeInterval.getStartDateTime(), timeInterval.getEndDateTime()));

    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }

  @GetMapping("organizations/{id}/members/interviews")
  @ResponseBody
  @ApiOperation("Returns all projects associated with an organization")
  public TypedResponse<List<UserInterviewSlots>> getInterviewSlotsForAllUsers(@ApiParam("Id of the organization")
                                                                              @PathVariable(value = "id") Long id,
                                                                              @RequestParam(value = "time", required = false) TimeInterval timeInterval,
                                                                              HttpServletRequest request, HttpServletResponse response)

  {
    if (timeInterval == null) {
      timeInterval = new TimeInterval();
    }
    try {
      User user = sessionController.getUserFromSession(request);
      return new TypedResponse<>(response, interviewSlotsHelper.getInterviewSlotsForAllMembersInOrganization(id, user, timeInterval.getStartDateTime(), timeInterval.getEndDateTime()));
    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }

  @GetMapping("organizations/{id}/projects/members")
  @ResponseBody
  @ApiOperation("Returns all projects associated with an organization")
  public TypedResponse<List<ProjectNumMember>> getNumOfEachProject(@ApiParam("Id of the organization")
                                                                              @PathVariable(value = "id") Long id,
                                                                            HttpServletRequest request, HttpServletResponse response)

  {

    try {
      User user = sessionController.getUserFromSession(request);
      return  new TypedResponse<>(response,groupMemberHelper.usersInEachProject(id, user));
    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }

  @GetMapping("organizations/{id}/members/projects")
  @ResponseBody
  @ApiOperation("Returns count of projects for all users apart of")
  public TypedResponse<List<UserProjectCount>> getNumOfProjectsThatUserArpatOf(@ApiParam("Id of the organization")
                                                                   @PathVariable(value = "id") Long id,
                                                                   HttpServletRequest request, HttpServletResponse response)

  {
    try {
      return  new TypedResponse<>(response,groupMemberHelper.numOfProjectsThatUserArpatOf(id, sessionController.getUserFromSession(request)));
    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }
}
