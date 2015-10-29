package com.zanvork.guildhubv3.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.services.UserService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/users")
public class UserController extends RESTController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public UserResponse getUser(
            final Principal p) {

        User user = getActiveUser(p);
        UserResponse response = new UserResponse(user);

        return response;
    }

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public UserSummaryResponse getUserSummary(
            final Principal p) {

        User user = getActiveUser(p);
        UserSummaryResponse response = new UserSummaryResponse(user);

        return response;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public UserResponse addUser(
            final @RequestBody SignupRequest r) {

        String username = r.getUsername();
        String email = r.getEmail();
        String password = r.getPassword();

        User user = userService.createUser(username, email, password);
        UserResponse response = new UserResponse(user);

        return response;
    }

    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(
            final Principal p,
            final @RequestBody ChangePasswordRequest r) {

        String oldPassword = r.getOldPassword();
        String newPassword = r.getOldPassword();

        User user = getActiveUser(p);

        userService.updatePassword(user, oldPassword, newPassword);
    }

    @RequestMapping(value = "/admin/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeUser(
            final @PathVariable long userId) {

        userService.deleteUser(userId);
    }

    @RequestMapping(value = "/admin/password", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void adminChangePassword(
            final @RequestBody AdminChangePasswordRequest r) {

        long userId = r.userId;
        String newPassword = r.getNewPassword();

        userService.updatePasswordForUser(userId, newPassword);

    }

    //Request Objects
    
    @Data
    static class SignupRequest {

        private String username;
        private String email;
        private String password;
        
    }

    @Data
    static class ChangePasswordRequest {

        private String oldPassword;
        private String newPassword;
    }

    @Data
    static class AdminChangePasswordRequest {

        private long userId;
        private String newPassword;
    }

    //ResponseOjects
    @Data
    @JsonInclude(Include.NON_NULL)
    protected class UserSummaryResponse {

        private UserResponse user;

        private List<CharacterResponse> characters;
        private List<GuildResponse> guilds;
        private List<TeamResponse> teams;

        private List<OwnershipRequestResponse> ownershipRequests;
        private List<TeamInviteResponse> teamInvites;

        UserSummaryResponse(User user) {
            this.user = new UserResponse(user);

            if (user.getCharacters() != null){
                characters = user.getCharacters().stream()
                        .map((character) -> new CharacterResponse(character))
                        .collect(Collectors.toList());
            }
            if (user.getGuilds() != null){
                guilds = user.getGuilds().stream()
                        .map((guild) -> new GuildResponse(guild))
                        .collect(Collectors.toList());
            }
            if (user.getTeams() != null){
                teams = user.getTeams().stream()
                        .map((team) -> new TeamResponse(team))
                        .collect(Collectors.toList());
            }

            if (user.getOwnershipRequests() != null){
                ownershipRequests = user.getOwnershipRequests().stream()
                        .map((ownershipRequest) -> new OwnershipRequestResponse(ownershipRequest))
                        .collect(Collectors.toList());
            }
            if (user.getTeamInvites() != null){
                teamInvites = user.getTeamInvites().stream()
                        .map((teamInvite) -> new TeamInviteResponse(teamInvite))
                        .collect(Collectors.toList());
            }
        }
    }

}
