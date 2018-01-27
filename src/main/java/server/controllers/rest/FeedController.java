package server.controllers.rest;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/friends")
@Api(value = "Friend Endpoints")
public class FeedController {
}
