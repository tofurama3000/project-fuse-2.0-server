package server.controllers.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.SearchParams;
import server.entities.dto.User;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.team.Team;
import server.utility.ElasticsearchClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static server.controllers.rest.response.GeneralResponse.Status.BAD_DATA;
import static server.controllers.rest.response.GeneralResponse.Status.OK;

/**
 * Created by tofurama on 12/23/17.
 */
@Controller
@RequestMapping(value = "/search")
@Api(tags="Search")
@SuppressWarnings("unused")
public class SearchController {

  @ApiOperation(value = "Searches all searchable entities")
  @PostMapping
  @ResponseBody
  public GeneralResponse searchAll(
          @ApiParam(value = "The search query to use", required = true)
          @RequestBody SearchParams params, HttpServletResponse response) {
    List<String> errors = new ArrayList<>();

    if(params.getSearchString() == null){
      errors.add("Missing search string");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    List<String> indices = params.getIndices();
    List<String> types = params.getTypes();

    if (indices == null || types == null || params.getIndices().isEmpty() || params.getTypes().isEmpty()){
      indices = SearchParams.allIndices();
      types = SearchParams.allTypes();
    }

    return new GeneralResponse(
            response,
            OK,
            errors,
            doSearch(params,
                    response,
                    indices.stream().toArray(String[]::new),
                    types.stream().toArray(String[]::new)
            )
    );
  }

  @ApiOperation(value = "Searches all users")
  @PostMapping("/users")
  @ResponseBody
  public GeneralResponse searchUser(
          @ApiParam(value = "The search query to use", required = true)
          @RequestBody SearchParams params, HttpServletResponse response) {
    return doSearch(params, response, new String[]{User.esIndex()}, new String[]{User.esType()});
  }

  @ApiOperation(value = "Searches all projects")
  @PostMapping("/projects")
  @ResponseBody
  public GeneralResponse searchProjects(
          @ApiParam(value = "The search query to use", required = true)
          @RequestBody SearchParams params, HttpServletResponse response) {
    return doSearch(params, response, new String[]{Project.esIndex()}, new String[]{Project.esType()});
  }

  @ApiOperation(value = "Searches all organizations")
  @PostMapping("/organizations")
  @ResponseBody
  public GeneralResponse searchOrganizations(
          @ApiParam(value = "The search query to use", required = true)
          @RequestBody SearchParams params, HttpServletResponse response) {
    return doSearch(params, response, new String[]{Organization.esIndex()}, new String[]{Organization.esType()});
  }

  private static <T> GeneralResponse doSearch(SearchParams params, HttpServletResponse response,
                                              String[] indices, String[] types) {
    List<String> errors = new ArrayList<>();

    if(params.getSearchString() == null) {
      errors.add("Missing search string");
      return new GeneralResponse(response, BAD_DATA, errors);
    }

    return new GeneralResponse(response, OK, errors,
            ElasticsearchClient.instance().searchSimpleQuery(
                    indices,
                    types,
                    params.getSearchString())
    );
  }
}
