/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.blender.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.blender.BlenderQueueService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * Created Mario Estrella on 12/25/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
public class ServerBackgroundController {
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService;
    private BlenderBenchmarkService blenderBenchmarkService;
    private NodeDiscoveryService nodeDiscoveryService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private BlenderQueueService blenderQueueService;
    private static final Logger LOG = LoggerFactory.getLogger(ServerBackgroundController.class);

    private boolean isFirstProjectRecent(BlenderProject blenderProject) {
        long projectTime = blenderProject.getTotalProjectTime();
        long minutes = projectTime / (60 * 1000);
        return projectTime == 0L || minutes < 30;
    }

    @PostMapping(value = "/api/update/node_idle_notification")
    public void nodeIdleNotification(@RequestParam String connection_uuid, ComputeType compute_type) {
        try {
            if (blenderProjectDatabaseService.listAll().size() != 0 && blenderRenderQueueDatabaseService.listAll().size() != 0) {
                BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueDatabaseService.listAll().get(0).getProject_uuid());
                SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
                if (blenderProject == null) {
                    sethlansNode.setCpuSlotInUse(false);
                    sethlansNode.setGpuSlotInUse(false);
                    sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                }
                if (blenderRenderQueueDatabaseService.listAll().size() > 0 && !isFirstProjectRecent(blenderProject)) {
                    blenderQueueService.nodeIdle(connection_uuid, compute_type);
                }
            } else {
                blenderQueueService.nodeIdle(connection_uuid, compute_type);
            }
        } catch (NullPointerException e) {
            LOG.error(Throwables.getStackTraceAsString(e));

        }
    }


    @RequestMapping(value = "/api/update/node_status_update", method = RequestMethod.GET)
    public void nodeStatusToServerUpdate(@RequestParam String connection_uuid) {
        // This is a pull request.  UUID is provided and the server will update it's information from the node.
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            SethlansNode sethlansNodetoUpdate = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
            LOG.debug("Sync request received for " + sethlansNodetoUpdate.getHostname());
            SethlansNode tempNode = nodeDiscoveryService.discoverUnicastNode(sethlansNodetoUpdate.getIpAddress(), sethlansNodetoUpdate.getNetworkPort());
            if (sethlansNodetoUpdate.isActive()) {
                sethlansNodetoUpdate.setBenchmarkComplete(false);
                if (tempNode.getComputeType().equals(ComputeType.CPU_GPU)) {
                    sethlansNodetoUpdate.setTotalRenderingSlots(2);
                    sethlansNodetoUpdate.setAvailableRenderingSlots(2);
                } else {
                    sethlansNodetoUpdate.setTotalRenderingSlots(1);
                    sethlansNodetoUpdate.setAvailableRenderingSlots(1);
                }
                updateNode(sethlansNodetoUpdate, tempNode);
                try {
                    Thread.sleep(10000);
                    blenderBenchmarkService.sendBenchmarktoNode(sethlansNodetoUpdate);
                } catch (InterruptedException e) {
                    LOG.debug(Throwables.getStackTraceAsString(e));
                }
            } else {
                updateNode(sethlansNodetoUpdate, tempNode);
            }
            LOG.debug(sethlansNodetoUpdate.getHostname() + " has been synced.");
        }
    }

    private void updateNode(SethlansNode sethlansNodetoUpdate, SethlansNode tempNode) {
        sethlansNodetoUpdate.setComputeType(tempNode.getComputeType());
        sethlansNodetoUpdate.setCpuinfo(tempNode.getCpuinfo());
        sethlansNodetoUpdate.setSelectedCores(tempNode.getSelectedCores());
        sethlansNodetoUpdate.setSelectedDeviceID(tempNode.getSelectedDeviceID());
        sethlansNodetoUpdate.setSelectedGPUs(tempNode.getSelectedGPUs());
        LOG.debug("Saving changes to database.");
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNodetoUpdate);
    }


    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setNodeDiscoveryService(NodeDiscoveryService nodeDiscoveryService) {
        this.nodeDiscoveryService = nodeDiscoveryService;
    }

    @Autowired
    public void setBlenderBenchmarkService(BlenderBenchmarkService blenderBenchmarkService) {
        this.blenderBenchmarkService = blenderBenchmarkService;
    }

    @Autowired
    public void setBlenderRenderQueueDatabaseService(BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService) {
        this.blenderRenderQueueDatabaseService = blenderRenderQueueDatabaseService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setBlenderQueueService(BlenderQueueService blenderQueueService) {
        this.blenderQueueService = blenderQueueService;
    }
}
