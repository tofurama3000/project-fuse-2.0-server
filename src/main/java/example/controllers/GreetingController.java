package example.controllers;

import example.Greeting;
import example.dto.GreetingDTO;
import example.repositories.GreetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class  GreetingController {

  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  @Autowired
  private GreetingRepository greetingRepository;


  @RequestMapping("/greeting")
  public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
    Greeting greeting = new Greeting(String.format(template, name));
    GreetingDTO greetingDTO = new GreetingDTO();
    greetingDTO.setFields(greeting);

    //greetingRepository.save(greetingDTO);

    return greeting;
  }

  @RequestMapping("/all")
  public Greeting getGreeting(@RequestParam(value="index", defaultValue="1") String index) {
    long indexL = Long.parseLong(index);
    //if (greetingRepository.exists(indexL)) {
    //  return new Greeting(greetingRepository.findOne(indexL));
    //}
    //else {
    //  return null;
    //}
    return null;
  }
}