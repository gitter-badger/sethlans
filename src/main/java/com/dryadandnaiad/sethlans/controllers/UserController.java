package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.domains.info.UserInfo;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Created Mario Estrella on 2/26/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @GetMapping(value = {"/username"})
    public String getUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        } else {
            return auth.getName();
        }
    }

    @GetMapping(value = {"/get_user/{username}"})
    public UserInfo getUserInfo(@PathVariable String username) {
        if (requestMatchesAuthUser(username)) {
            SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(username);
            UserInfo userToSend = new UserInfo();
            userToSend.setUsername(sethlansUser.getUsername());
            userToSend.setActive(sethlansUser.isActive());
            userToSend.setRoles(sethlansUser.getRoles());
            userToSend.setEmail(sethlansUser.getEmail());
            userToSend.setId(sethlansUser.getId());
            userToSend.setLastUpdated(sethlansUser.getLastUpdated());
            userToSend.setDateCreated(sethlansUser.getDateCreated());
            return userToSend;
        } else {
            return null;
        }

    }

    @PostMapping(value = {"/email_pass_change"})
    public boolean changeEmailandPassword(@RequestParam String username, @RequestParam String passToCheck, @RequestParam String newPassword, @RequestParam String newEmail) {
        return changeEmail(username, newEmail) && changePassword(username, passToCheck, newPassword);
    }

    @PostMapping(value = {"/pass_change"})
    public boolean changePassword(@RequestParam String username, @RequestParam String passToCheck, @RequestParam String newPassword) {
        if (requestMatchesAuthUser(username)) {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
            if (encoder.matches(passToCheck, user.getPassword())) {
                LOG.debug("Updating password for " + username);
                user.setPasswordUpdated(true);
                user.setPassword(newPassword);
                sethlansUserDatabaseService.saveOrUpdate(user);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @PostMapping(value = {"/email_change"})
    public boolean changeEmail(@RequestParam String username, @RequestParam String newEmail) {
        if (requestMatchesAuthUser(username)) {
            SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
            LOG.debug("Changing email for " + username);
            user.setEmail(newEmail);
            user.setPasswordUpdated(false);
            sethlansUserDatabaseService.saveOrUpdate(user);
            return true;
        }
        return false;

    }

    private boolean requestMatchesAuthUser(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName().equals(username);
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
