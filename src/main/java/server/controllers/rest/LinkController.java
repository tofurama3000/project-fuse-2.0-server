package server.controllers.rest;

import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;
import static server.controllers.rest.response.CannedResponse.INVALID_SESSION;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import server.controllers.FuseSessionController;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.controllers.rest.response.TypedResponse;
import server.entities.dto.FuseSession;
import server.entities.dto.Link;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.repositories.LinkRepository;
import server.service.EntityFinder;
import server.service.LinkResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/links")
@Api(tags = "links")
public class LinkController {

  @Autowired
  private FuseSessionController fuseSessionController;

  @Autowired
  private EntityFinder entityFinder;

  @Autowired
  private PermissionFactory permissionFactory;

  @Autowired
  private LinkRepository linkRepository;

  @PostMapping
  @ResponseBody
  public TypedResponse<Link> addLink(
          @ApiParam(value = "The user information to create with", required = true)
                                     @RequestBody Link link,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, INVALID_SESSION);
    }
    User sessionUser = session.get().getUser();
    if (!userCanModifyLink(sessionUser, link)) {
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, INSUFFICIENT_PRIVELAGES);
    }

    Link resolvedLink;
    try {
      resolvedLink = LinkResolver.resolveLink(link);
    } catch (Exception e) {
      return new TypedResponse<>(response, BaseResponse.Status.ERROR, "Malformed Link");
    }

    return new TypedResponse<>(response, BaseResponse.Status.OK, null, linkRepository.save(resolvedLink));
  }

  @CrossOrigin
  @DeleteMapping
  @ResponseBody
  @ApiOperation(value = "Removes link with id")
  public TypedResponse<Link> removeLink(@ApiParam(value = "The user information to create with", required = true)
                                        @RequestBody Link link,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, INVALID_SESSION);
    }
    User sessionUser = session.get().getUser();
    link = linkRepository.findOne(link.getId());
    if (!userCanModifyLink(sessionUser, link)) {
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, INSUFFICIENT_PRIVELAGES);
    }

    linkRepository.delete(link);
    return new TypedResponse<>(response, BaseResponse.Status.OK, null, link);
  }

  @GetMapping
  @ResponseBody
  @ApiOperation(value = "Gets all links associated with id and type")
  public TypedResponse<List<Link>> getLinks(@ApiParam(value = "The id of the profile")
                                            @RequestParam(value = "profileId") Long profileId,
                                            @ApiParam(value = "The type of profile (Project, Organization, User)")
                                            @RequestParam(value = "profileType") String profileType,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {

    Optional<FuseSession> session = fuseSessionController.getSession(request);
    if (!session.isPresent()) {
      return new TypedResponse<>(response, BaseResponse.Status.DENIED, INVALID_SESSION);
    }

    return new TypedResponse<>(response, BaseResponse.Status.OK, null,
        linkRepository.getLinksWithIdOfType(profileId, profileType));
  }

  private boolean userCanModifyLink(User sessionUser, Link link) {
    switch (link.getReferencedType()) {
      case "Organization":
        Optional<Organization> organizationOptional = entityFinder.findEntity(link.getReferencedId(), Organization.class);
        return organizationOptional
            .filter(organization -> permissionFactory.createUserToOrganizationPermission(sessionUser, organization)
                .canUpdate()).isPresent();
      case "Project":
        Optional<Project> projectOptional = entityFinder.findEntity(link.getReferencedId(), Project.class);
        return projectOptional
            .filter(project -> permissionFactory.createUserToProjectPermission(sessionUser, project)
                .canUpdate()).isPresent();
      case "User":
        Optional<User> userOptional = entityFinder.findEntity(link.getReferencedId(), User.class);
        return userOptional
            .filter(user -> user.getId().equals(sessionUser.getId())).isPresent();
      default:
        return false;
    }
  }

  public BaseResponse updateLinksFor(String referenceType, Long referenceId, List<Link> links, HttpServletResponse response) {

    List<Link> dbLinks = linkRepository.getLinksWithIdOfType(referenceId, referenceType);

    // Helper functions useful for this function
    BiFunction<Link, Link, Boolean> idsMatch = (link1, link2) -> Objects.equals(link1.getId(), link2.getId());
    BiFunction<Link, Link, Boolean> linkIsDifferent = (link1, link2) ->
            !!Objects.equals(link1.getLink(), link2.getLink()) ||
                    !Objects.equals(link1.getName(), link2.getName());
    Function<Link, Link> createCopyToSave = (link) -> {
        Link linkToSave = new Link();
        linkToSave.setLink(link.getLink());
        linkToSave.setName(link.getName());
        linkToSave.setReferencedType(referenceType);
        linkToSave.setReferencedId(referenceId);
        linkToSave.setId(link.getId());
        return linkToSave;
    };

    // Update links that need updating
    List<Link> linksToUpdate = dbLinks.stream()
            .flatMap(dbLink -> links.stream().filter(
                    inputLink -> idsMatch.apply(inputLink, dbLink) && linkIsDifferent.apply(inputLink, dbLink)
                ).limit(1)
            )
            .map(createCopyToSave)
            .collect(Collectors.toList());
    linkRepository.save(linksToUpdate);

    // Delete links not present
    List<Link> linksToDelete = dbLinks.stream()
            .filter(dbLink -> links.stream()
                    .filter(inputLink -> idsMatch.apply(dbLink, inputLink))
                    .count() == 0
            )
            .collect(Collectors.toList());
    linkRepository.delete(linksToDelete);

    // Create links that don't have an id
    List<Link> linksToCreate = links.stream()
            .filter(link -> link.getId() == null)
            .map(createCopyToSave)
            .collect(Collectors.toList());
    linkRepository.save(linksToCreate);

    return new GeneralResponse(response, BaseResponse.Status.OK);
  }

  public TypedResponse<List<Link>> getLinksFor(String referenceType, Long referenceId, HttpServletResponse response) {

    return new TypedResponse<>(response, BaseResponse.Status.OK, null,
            linkRepository.getLinksWithIdOfType(referenceId, referenceType));
  }
}
