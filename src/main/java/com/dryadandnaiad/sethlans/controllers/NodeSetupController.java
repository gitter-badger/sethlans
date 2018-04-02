package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.services.config.UpdateComputeService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.services.system.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * Created Mario Estrella on 4/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@RestController
@Profile({"NODE", "DUAL"})
@RequestMapping("/api/setup")
public class NodeSetupController {
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private NodeActivationService nodeActivationService;
    private UpdateComputeService updateComputeService;
    private SethlansManagerService sethlansManagerService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeSetupController.class);


    @GetMapping("/server_acknowledge/{id}")
    public boolean acknowledgeNode(@PathVariable Long id) {
        SethlansServer sethlansServer = sethlansServerDatabaseService.getById(id);
        sethlansServer.setPendingAcknowledgementResponse(true);
        LOG.debug(sethlansServer.toString());
        sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
        nodeActivationService.sendActivationResponse(sethlansServer, SethlansUtils.getCurrentNodeInfo(), false);
        return true;
    }

    @GetMapping("/server_delete/{id}")
    public boolean deleteServer(@PathVariable Long id) {
        sethlansServerDatabaseService.delete(id);
        return true;
    }

    @PostMapping("/update_compute")
    public boolean submit(@RequestBody SetupNode setupNode) {
        LOG.debug("Processing Compute Setting Update");
        if (setupNode != null) {
            LOG.debug(setupNode.toString());
            boolean updateComplete = updateComputeService.saveComputeSettings(setupNode);
            sethlansManagerService.restart();
            return updateComplete;
        } else {
            return false;
        }
    }


    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }


    @Autowired
    public void setNodeActivationService(NodeActivationService nodeActivationService) {
        this.nodeActivationService = nodeActivationService;
    }

    @Autowired
    public void setUpdateComputeService(UpdateComputeService updateComputeService) {
        this.updateComputeService = updateComputeService;
    }

    @Autowired
    public void setSethlansManagerService(SethlansManagerService sethlansManagerService) {
        this.sethlansManagerService = sethlansManagerService;
    }

}
