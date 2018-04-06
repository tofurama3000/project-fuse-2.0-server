package server.controllers.rest;

import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.DENIED;

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
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.TimeInterval;
import server.entities.dto.group.project.ProjectInterviewSlots;
import server.entities.dto.statistics.MemberProjectOrganizationInterviewSummaryView;
import server.entities.dto.statistics.UsersWithInvalidProfilesSummaryView;
import server.entities.dto.user.ProjectMemberCount;
import server.entities.dto.user.User;
import server.entities.dto.user.UserInterviewSlots;
import server.entities.dto.user.UserProjectCount;
import server.handlers.GroupMemberHelper;
import server.handlers.InterviewSlotsHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
  public TypedResponse<List<ProjectMemberCount>> getMemberCountForEachProject(@ApiParam("Id of the organization")
                                                                   @PathVariable(value = "id") Long id,
                                                                              HttpServletRequest request, HttpServletResponse response)

  {

    try {
      User user = sessionController.getUserFromSession(request);
      return new TypedResponse<>(response, groupMemberHelper.organizationProjectsUserCount(id, user));
    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }

  @GetMapping("organizations/{id}/members/projects")
  @ResponseBody
  @ApiOperation("Returns count of projects for all users apart of")
  public TypedResponse<List<UserProjectCount>> getNumOfProjectsThatUserApartOf(@ApiParam("Id of the organization")
                                                                               @PathVariable(value = "id") Long id,
                                                                               HttpServletRequest request, HttpServletResponse response)

  {
    try {
      return new TypedResponse<>(response, groupMemberHelper.organizationMembersProjectCount(id, sessionController.getUserFromSession(request)));
    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }

  @GetMapping("organizations/{id}/members/interviewSummary")
  @ResponseBody
  @ApiOperation("Returns interview summary of organization")
  public TypedResponse<List<MemberProjectOrganizationInterviewSummaryView>> getMemberProjectOrganizationInterviewSummaryView(@ApiParam("Id of the organization")
                                                                               @PathVariable(value = "id") Long id,
                                                                                HttpServletRequest request, HttpServletResponse response)

  {
    try {
      return new TypedResponse<>(response, groupMemberHelper.getMemberProjectOrganizationInterviewSummaryView(id,sessionController.getUserFromSession(request)));
    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }

  @GetMapping("organizations/{id}/members/invalidProfilesSummary")
  @ResponseBody
  @ApiOperation("Returns users that have invalid profile")
  public TypedResponse<List<UsersWithInvalidProfilesSummaryView>> getUsersWithInvalidProfilesSummaryView(@ApiParam("Id of the organization")
                                                                                                                             @PathVariable(value = "id") Long id,
                                                                                                         HttpServletRequest request, HttpServletResponse response)

  {
    try {
      return new TypedResponse<>(response, groupMemberHelper.getUsersWithInvalidProfilesSummaryView(id,sessionController.getUserFromSession(request)));
    } catch (DeniedException e) {
      return new TypedResponse<>(response, DENIED, e.getMessage());
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BAD_DATA, e.getMessage());
    }
  }


}
