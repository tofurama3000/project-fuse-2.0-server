package server.controllers.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.PagedResults;
import server.entities.dto.SearchParams;
import server.entities.dto.user.User;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.utility.ElasticsearchClient;

import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static server.controllers.rest.response.BaseResponse.Status.BAD_DATA;
import static server.controllers.rest.response.BaseResponse.Status.OK;

/**
 * Created by tofurama on 12/23/17.
 */
@Controller
@RequestMapping(value = "/search")
@Api(tags = "Search")
@SuppressWarnings("unused")
public class SearchController {

  @ApiOperation(value = "Searches all searchable entities")
  @PostMapping
  @ResponseBody
  public TypedResponse<PagedResults> searchAll(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value="The page of results to pull")
      @RequestParam(value = "page", required=false, defaultValue="0") int page,
      @ApiParam(value="The number of results per page")
      @RequestParam(value = "size", required=false, defaultValue="15") int pageSize,
      HttpServletResponse response) {

    List<String> errors = new ArrayList<>();

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

    return doSearch(params,
          response,
          indices.stream().toArray(String[]::new),
          types.stream().toArray(String[]::new),
          page,
          pageSize
      );
  }

  @ApiOperation(value = "Searches all users")
  @PostMapping("/users")
  @ResponseBody
  public TypedResponse<PagedResults> searchUser(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value="The page of results to pull")
      @RequestParam(value = "page", required=false, defaultValue="0") Integer page,
      @ApiParam(value="The number of results per page")
      @RequestParam(value = "size", required=false, defaultValue="15") Integer pageSize,
      HttpServletResponse response) {
    return doSearch(
            params,
            response,
            new String[]{User.esIndex()},
            new String[]{User.esType()},
            page,
            pageSize
    );
  }

  @ApiOperation(value = "Searches all projects")
  @PostMapping("/projects")
  @ResponseBody
  public TypedResponse<PagedResults> searchProjects(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value="The page of results to pull")
      @RequestParam(value = "page", required=false, defaultValue="0") Integer page,
      @ApiParam(value="The number of results per page")
      @RequestParam(value = "size", required=false, defaultValue="15") Integer pageSize,HttpServletResponse response) {
    return doSearch(
            params,
            response,
            new String[]{Project.esIndex()},
            new String[]{Project.esType()},
            page,
            pageSize
    );
  }

  @ApiOperation(value = "Searches all organizations")
  @PostMapping("/organizations")
  @ResponseBody
  public TypedResponse<PagedResults> searchOrganizations(
      @ApiParam(value = "The search query to use", required = true)
      @RequestBody SearchParams params,
      @ApiParam(value="The page of results to pull")
      @RequestParam(value = "page", required=false, defaultValue="0") Integer page,
      @ApiParam(value="The number of results per page")
      @RequestParam(value = "size", required=false, defaultValue="15") Integer pageSize,HttpServletResponse response) {
    return doSearch(
            params,
            response,
            new String[]{Organization.esIndex()},
            new String[]{Organization.esType()},
            page,
            pageSize
    );
  }

  private static TypedResponse<PagedResults> doSearch(SearchParams params, HttpServletResponse response,
                                              String[] indices, String[] types, int page, int pageSize) {
    List<String> errors = new ArrayList<>();

    if (params.getSearchString() == null) {
      errors.add("Missing search string");
      return new TypedResponse<>(response, BAD_DATA, errors);
    }

    try {
      return new TypedResponse<>(response, OK, errors,
              ElasticsearchClient.instance().searchSimpleQuery(
                      indices,
                      types,
                      params.getSearchString(),
                      page,
                      pageSize)
      );
    } catch(NullPointerException e) {
      errors.add("Server error");
      return new TypedResponse<>(response, BaseResponse.Status.ERROR,errors);
    }
  }
}
