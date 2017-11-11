package server.controllers.rest;

import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.User;
import server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value="/user")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @PostMapping(path="/add") // Map ONLY GET Requests
  public @ResponseBody
  GeneralResponse addNewUser (@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {

    List<String> errors = new ArrayList<>();

    // @ResponseBody means the returned String is the response, not a view name
    // @RequestParam means it is a parameter from the GET or POST request
    if(user != null){
      if(user.getName() == null)
        errors.add("Missing Name");
      if(user.getEncoded_password() == null)
        errors.add("Missing Password");
      if(user.getEmail() == null)
        errors.add("Missing Email");
      if(errors.size() == 0 && userRepository.findByEmail(user.getEmail()) != null)
        errors.add("Username already exists!");
    }
    else
      errors.add("No request body found");

    if(errors.size() == 0)
      userRepository.save(user);

    return new GeneralResponse(response, errors);
  }

  @PostMapping(path="/login")
  public @ResponseBody
  GeneralResponse login(@RequestBody User loginRequest, HttpServletRequest request, HttpServletResponse response){
    //UserPermission permissions = new UserPermission(request);
    //if(permissoins.isLogedIn()){
    //  // logged in!
    //  user = permissions.getUser();
    //}
    List<String> errors = new ArrayList<>();
    if(loginRequest == null){
      errors.add("Invalid Credentials");
    }
    else {
      User dbUser = userRepository.findByEmail(loginRequest.getEmail());

      if (dbUser == null) {
        errors.add("Invalid Credentials");
      } else {
        loginRequest.setEncoded_password(dbUser.getEncoded_password());

        if (loginRequest.checkPassword()) {
          return new GeneralResponse(response, GeneralResponse.Status.OK, null, SessionController.GetSession(dbUser));
        }
        errors.add("Invalid Credentials");
      }
    }
    return new GeneralResponse(response, GeneralResponse.Status.DENIED, errors);
  }

  @GetMapping(path="/all")
  public @ResponseBody Iterable<User> getAllUsers() {
    return userRepository.findAll();
  }
}
