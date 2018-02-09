package server.controllers.rest;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/links")
@Api(tags = "links")
public class LinkController {
  // add link for team, user, and organization
  // remove link for team, user and organization
}
