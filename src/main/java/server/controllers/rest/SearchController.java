package server.controllers.rest;

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
@SuppressWarnings("unused")
public class SearchController {

  @PostMapping
  @ResponseBody
  public GeneralResponse searchAll(@RequestBody SearchParams params, HttpServletResponse response) {
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

  @PostMapping("/users")
  @ResponseBody
  public GeneralResponse searchUser(@RequestBody SearchParams params, HttpServletResponse response) {
    return doSearch(params, response, new String[]{User.esIndex()}, new String[]{User.esType()});
  }

  @PostMapping("/projects")
  @ResponseBody
  public GeneralResponse searchProjects(@RequestBody SearchParams params, HttpServletResponse response) {
    return doSearch(params, response, new String[]{Project.esIndex()}, new String[]{Project.esType()});
  }

  @PostMapping("/teams")
  @ResponseBody
  public GeneralResponse searchTeams(@RequestBody SearchParams params, HttpServletResponse response) {
    return doSearch(params, response, new String[]{Team.esIndex()}, new String[]{Team.esType()});
  }

  @PostMapping("/organizations")
  @ResponseBody
  public GeneralResponse searchOrganizations(@RequestBody SearchParams params, HttpServletResponse response) {
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
