package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.events.SethlansEvent;
import com.dryadandnaiad.sethlans.services.database.UserService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 10/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class AbstractSethlansController implements ApplicationListener<SethlansEvent> {
    private UserService userService;
    private boolean notification;
    private List<String> notificationMessage = new ArrayList<>();
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSethlansController.class);

    @Value("${sethlans.mode}")
    private SethlansMode mode;

    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @ModelAttribute("version")
    public String getVersion() {
        return SethlansUtils.getVersion();
    }

    @ModelAttribute("sethlansmode")
    public String getMode() {
        return mode.toString();
    }

    @ModelAttribute("username")
    public String getUserName() {
        if(firstTime){
            return "username";
        }
        return userService.getById(1).getUsername();

    }

    @ModelAttribute("isNewNotification")
    public boolean isNotification(){
        LOG.debug("Current notification list size: " + notificationMessage.size());
        return notification;
    }

    @ModelAttribute("notificationMessages")
    public List<String> getNotificationMessage(){
        return notificationMessage;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void onApplicationEvent(SethlansEvent event) {
        notification = event.isNewNotification();
        if(notification) {
            notificationMessage.add(event.getMessage());
        }
        else {
            notificationMessage = new ArrayList<>();
        }

    }
}
