package example;


import example.dto.GreetingDTO;

public class Greeting  {


  private final String content;

  public Greeting(String content) {
    this.content = content;
  }

  public Greeting(GreetingDTO greetingDTO) {
    this.content = greetingDTO.getContents();
  }

  public String getContent() {
    return content;
  }

}

