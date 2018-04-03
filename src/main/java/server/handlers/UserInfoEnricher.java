package server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.entities.dto.PagedResults;
import server.entities.dto.SearchResult;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.Friendship;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToGroupPermission;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.entities.user_to_group.permissions.UserToProjectPermission;
import server.repositories.FriendRepository;
import server.service.EntityFinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserInfoEnricher {

  private final PermissionFactory permissionFactory;
  private final EntityFinder entityFinder;
  private final FriendRepository friendRepository;

  private static final String ACTIONS_AVAILABLE = "actions_available";
  private static final String SHOULD_HIDE = "SHOULD_HIDE";


  @Autowired
  public UserInfoEnricher(PermissionFactory permissionFactory, EntityFinder entityFinder, FriendRepository friendRepository) {
    this.permissionFactory = permissionFactory;
    this.entityFinder = entityFinder;
    this.friendRepository = friendRepository;
  }

  public PagedResults enrichWithUserInfo(User user, PagedResults results) {

    Set<Friendship> friendships = new HashSet<>(friendRepository.getAllFriends(user));

    List<SearchResult> enrichedSearchResults = results.getSearchResults().stream()
        .map(searchResult -> enrichForUser(user, friendships, searchResult))
        .filter(searchResult -> !searchResult.getData().containsKey(SHOULD_HIDE)).collect(Collectors.toList());

    PagedResults enrichedPageResults = new PagedResults();
    enrichedPageResults.setStart(enrichedPageResults.getStart());
    enrichedPageResults.setEnd(results.getEnd());
    enrichedPageResults.setTotalItems(results.getTotalItems());

    enrichedPageResults.setSearchResults(enrichedSearchResults);

    return enrichedPageResults;
  }

  private SearchResult enrichForUser(User loggedInUser, Set<Friendship> friendships, SearchResult searchResult) {
    Long id = ((Integer) searchResult.getData().get("id")).longValue();
    switch ((String) searchResult.getData().get("index")) {
      case "organizations":
        return entityFinder.findEntity(id, Organization.class)
            .map(org -> enrichForUser(loggedInUser, org, searchResult))
            .orElse(searchResult);
      case "projects":
        return entityFinder.findEntity(id, Project.class)
            .map(project -> enrichForUser(loggedInUser, project, searchResult))
            .orElse(searchResult);
      case "users":
        return entityFinder.findEntity(id, User.class)
            .map(user -> enrichForUser(loggedInUser, user, friendships, searchResult))
            .orElse(searchResult);
      default:
        return searchResult;
    }
  }

  private SearchResult enrichForUser(User loggedInUser, Organization organizationFromSearch, SearchResult searchResult) {
    UserToOrganizationPermission permission = permissionFactory.createUserToOrganizationPermission(loggedInUser, organizationFromSearch);
    enrichWithPermission(searchResult, permission);

    return searchResult;
  }


  private SearchResult enrichForUser(User loggedInUser, Project projectFromSearch, SearchResult searchResult) {
    UserToProjectPermission permission = permissionFactory.createUserToProjectPermission(loggedInUser, projectFromSearch);
    enrichWithPermission(searchResult, permission);

    return searchResult;
  }

  private SearchResult enrichForUser(User loggedInUser, User userFromSearch, Set<Friendship> friendships, SearchResult searchResult) {

    Friendship friendship = new Friendship();
    friendship.setSender(loggedInUser);
    friendship.setReceiver(userFromSearch);

    if (friendships.contains(friendship)) {
      searchResult.getData().put(ACTIONS_AVAILABLE, "none");
    } else {
      searchResult.getData().put(ACTIONS_AVAILABLE, "add");
    }

    if (userFromSearch.getId().equals(loggedInUser.getId())) {
      searchResult.getData().put(SHOULD_HIDE, true);
    }
    return searchResult;
  }

  private void enrichWithPermission(SearchResult searchResult, UserToGroupPermission permission) {
    switch (permission.canJoin()) {
      case NEED_INVITE:
        if (permission.hasApplied()) {
          searchResult.getData().put(ACTIONS_AVAILABLE, "none");
        } else {
          searchResult.getData().put(ACTIONS_AVAILABLE, "apply");
        }
        break;
      case HAS_INVITE:
      case OK:
        searchResult.getData().put(ACTIONS_AVAILABLE, "join");
        break;
      default:
        searchResult.getData().put(ACTIONS_AVAILABLE, "none");
        break;
    }
  }
}
