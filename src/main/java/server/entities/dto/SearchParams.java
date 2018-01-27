package server.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.group.team.Team;
import server.utility.ElasticsearchClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by tofurama on 12/23/17.
 */
public class SearchParams {

  final static String entityIdentifierRegex = "(^(\\s*in:[\\w,]+))|(\\s+in:[\\w,]+)";
  final static Pattern entityIdentifierPattern = Pattern.compile(entityIdentifierRegex);

  @JsonIgnore
  @Getter
  private String searchString;

  @JsonIgnore
  @Getter
  private List<String> indices = new ArrayList<>();

  @JsonIgnore
  @Getter
  private List<String> types = new ArrayList<>();

  public void setQuery(String query) {
    List<String> entityMatches = new LinkedList<>();
    Matcher matcher = entityIdentifierPattern.matcher(query);

    while (matcher.find()) {
      entityMatches.add(matcher.group());
    }

    searchString = query.replaceAll(entityIdentifierRegex, "").trim();
    if (searchString.equals("")) {
      searchString = "*";
    }


    Pattern p = Pattern.compile("(^\\w+|[\\s\\+]+[\\w\\s]+(\\s|$))");
    Matcher m = p.matcher(searchString);
    StringBuffer s = new StringBuffer();
    while (m.find()) {
      String group = m.group(1);
      searchString = searchString.replaceAll(group, " " + m.group(1).trim() + "* ");
    }

    entityMatches.forEach(this::mapEntityToIndexAndType);
  }

  private void mapEntityToIndexAndType(String entity) {
    if (entity.length() > 3 && entity.substring(0, 3).equals("in:")) {
      entity = entity.substring(3);
    }
    if (entity.contains(",")) {
      String[] entities = entity.trim().substring(3).split(",");
      indices.addAll(Arrays.stream(entities)
          .map(SearchParams::mapEntityToIndex)
          .filter(Objects::nonNull)
          .collect(Collectors.toList()));
      types.addAll(Arrays.stream(entities)
          .map(SearchParams::mapEntityToType)
          .filter(Objects::nonNull)
          .collect(Collectors.toList()));
    } else {
      String indexName = mapEntityToIndex(entity);
      String typeName = mapEntityToType(entity);
      if (indexName != null) {
        indices.add(indexName);
        types.add(typeName);
      }
    }
  }

  private static String mapEntityToIndex(String entity) {
    switch (entity.toLowerCase()) {
      case "t":
      case "team":
      case "teams":
        return Team.esIndex();
      case "o":
      case "org":
      case "orgs":
      case "organization":
      case "organizations":
        return Organization.esIndex();
      case "p":
      case "proj":
      case "project":
      case "projects":
        return Project.esIndex();
      case "u":
      case "user":
      case "users":
        return User.esIndex();
      default:
        return null;
    }
  }

  private static String mapEntityToType(String entity) {
    switch (entity.toLowerCase()) {
      case "o":
      case "org":
      case "orgs":
      case "organization":
      case "organizations":
        return Organization.esType();
      case "p":
      case "proj":
      case "project":
      case "projects":
        return Project.esType();
      case "u":
      case "user":
      case "users":
        return User.esType();
      default:
        return null;
    }
  }

  public static List<String> allTypes() {
    return Arrays.asList(
        Organization.esType(),
        Project.esType(),
        User.esType());
  }

  public static List<String> allIndices() {
    return Arrays.asList(
        Organization.esIndex(),
        Project.esIndex(),
        User.esIndex());
  }
}
