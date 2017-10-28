package com.dryadandnaiad.sethlans.components;

import com.dryadandnaiad.sethlans.services.network.MulticastSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created Mario Estrella on 10/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
@Profile({"NODE", "DUAL"})
public class SethlansMulticastSendComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SethlansMulticastSendComponent.class);

    @Value("${server.port}")
    private String sethlansPort;

    private MulticastSenderService multicastSenderService;

    @Autowired
    public void setMulticastSenderService(MulticastSenderService multicastSenderService) {
        this.multicastSenderService = multicastSenderService;
    }

    @PostConstruct
    public void startNodeMulticast(){
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        LOG.debug("Sethlans Host IP: " + ip);
        multicastSenderService.sendSethlansIPAndPort(ip, sethlansPort);
    }
}
