package server.controllers.rest;

import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.OK;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.errors.BadDataException;
import server.controllers.rest.errors.ServerErrorException;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.PagedResults;
import server.entities.dto.SearchParams;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.handlers.UserInfoEnricher;
import server.utility.ElasticsearchClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping(value = "/search")
@Api(tags = "Search")
@SuppressWarnings("unused")
public class SearchController {

  private FuseSessionController sessionController;
  private final UserInfoEnricher userInfoEnricher;

  @Autowired
  public SearchController(FuseSessionController sessionController, UserInfoEnricher userInfoEnricher) {
    this.sessionController = sessionController;
    this.userInfoEnricher = userInfoEnricher;
  }

  @ApiOperation(value = "Searches all searchable entities")
  @PostMapping
  @ResponseBody
  public TypedResponse<PagedResults> searchAll(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") int page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") int pageSize,
      HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();
    Optional<User> userOptional = sessionController.getSession(request).map(FuseSession::getUser);

    if (params.getSearchString() == null) {
      errors.add("Missing search string");
      return new TypedResponse<>(response, BAD_DATA, errors);
    }


    List<String> indices = params.getIndices();
    List<String> types = params.getTypes();

    if (indices == null || types == null || params.getIndices().isEmpty() || params.getTypes().isEmpty()) {
      indices = SearchParams.allIndices();
      types = SearchParams.allTypes();
    }

    try {
      PagedResults pagedResults = doSearch(params,
          indices.toArray(new String[0]), types.toArray(new String[0]), page, pageSize);
      return new TypedResponse<>(response, userOptional.map(user -> userInfoEnricher.enrichWithUserInfo(user, pagedResults))
          .orElse(pagedResults));
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BaseResponse.Status.BAD_DATA, e.getMessage());
    } catch (ServerErrorException e) {
      return new TypedResponse<>(response, BaseResponse.Status.ERROR, e.getMessage());
    }
  }

  @ApiOperation(value = "Searches all users")
  @PostMapping("/users")
  @ResponseBody
  public TypedResponse<PagedResults> searchUser(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") Integer pageSize,
      HttpServletRequest request, HttpServletResponse response) {
    Optional<User> userOptional = sessionController.getSession(request).map(FuseSession::getUser);
    try {
      PagedResults pagedResults = doSearch(params,
          new String[]{User.esIndex()}, new String[]{User.esType()}, page, pageSize);

      return new TypedResponse<>(response, userOptional.map(user -> userInfoEnricher.enrichWithUserInfo(user, pagedResults))
          .orElse(pagedResults));
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BaseResponse.Status.BAD_DATA, e.getMessage());
    } catch (ServerErrorException e) {
      return new TypedResponse<>(response, BaseResponse.Status.ERROR, e.getMessage());
    }
  }

  @ApiOperation(value = "Searches all projects")
  @PostMapping("/projects")
  @ResponseBody
  public TypedResponse<PagedResults> searchProjects(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") Integer pageSize,
      HttpServletRequest request, HttpServletResponse response) {
    Optional<User> userOptional = sessionController.getSession(request).map(FuseSession::getUser);
    try {
      PagedResults pagedResults = doSearch(params,
          new String[]{Project.esIndex()}, new String[]{Project.esType()}, page, pageSize);
      return new TypedResponse<>(response, userOptional.map(user -> userInfoEnricher.enrichWithUserInfo(user, pagedResults))
          .orElse(pagedResults));
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BaseResponse.Status.BAD_DATA, e.getMessage());
    } catch (ServerErrorException e) {
      return new TypedResponse<>(response, BaseResponse.Status.ERROR, e.getMessage());
    }
  }

  @ApiOperation(value = "Searches all organizations")
  @PostMapping("/organizations")
  @ResponseBody
  public TypedResponse<PagedResults> searchOrganizations(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value = "The page of results to pull")
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @ApiParam(value = "The number of results per page")
      @RequestParam(value = "size", required = false, defaultValue = "15") Integer pageSize,
      HttpServletRequest request, HttpServletResponse response) {
    Optional<User> userOptional = sessionController.getSession(request).map(FuseSession::getUser);

    try {
      PagedResults pagedResults = doSearch(params,
          new String[]{Organization.esIndex()}, new String[]{Organization.esType()}, page, pageSize);
      return new TypedResponse<>(response, userOptional.map(user -> userInfoEnricher.enrichWithUserInfo(user, pagedResults))
          .orElse(pagedResults));
    } catch (BadDataException e) {
      return new TypedResponse<>(response, BaseResponse.Status.BAD_DATA, e.getMessage());
    } catch (ServerErrorException e) {
      return new TypedResponse<>(response, BaseResponse.Status.ERROR, e.getMessage());
    }
  }

  private static PagedResults doSearch(SearchParams params, String[] indices, String[] types, int page, int pageSize)
      throws BadDataException, ServerErrorException {

    if (params.getSearchString() == null) {
      throw new BadDataException("Missing search string");
    }

    try {
      return ElasticsearchClient.instance().searchSimpleQuery(
          indices,
          types,
          params.getSearchString(),
          page,
          pageSize);

    } catch (NullPointerException e) {
      throw new ServerErrorException("Server error");
    }
  }
}
