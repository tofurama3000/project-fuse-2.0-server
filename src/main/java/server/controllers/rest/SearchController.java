package server.controllers.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.SearchParams;
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
  public GeneralResponse searchAll(@RequestBody SearchParams params, HttpServletRequest request, HttpServletResponse response) {
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

    return new GeneralResponse(response, OK, errors,
            ElasticsearchClient.instance().searchSimpleQuery(
              indices.stream().toArray(String[]::new),
              types.stream().toArray(String[]::new),
              params.getSearchString())
    );
  }
}
