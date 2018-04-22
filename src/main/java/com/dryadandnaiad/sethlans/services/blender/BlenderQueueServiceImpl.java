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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProcessQueueItem;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderQueueItem;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.services.database.BlenderProcessQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created Mario Estrella on 4/21/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderQueueServiceImpl implements BlenderQueueService {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderQueueServiceImpl.class);
    private BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService;
    private boolean modifyingQueue = false;
    private List<SethlansNode> sortedSethlansNodeList = new ArrayList<>();
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderProcessQueueDatabaseService blenderProcessQueueDatabaseService;
    private BlenderProjectService blenderProjectService;


    @Async
    @Override
    public void startQueue() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            LOG.debug("Stopping Blender Queue Service");
        }
        while (true) {
            try {
                Thread.sleep(1000);
                assignNodeToQueueItem();
                sendQueueItemsToAssignedNode();
                processReceivedQueueItems();
            } catch (InterruptedException e) {
                LOG.debug("Stopping Blender Queue Service");
            }
        }
    }

    @Override
    public void populateQueueWithProject(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
            for (BlenderFramePart blenderFramePart : blenderFramePartList) {
                BlenderRenderQueueItem blenderRenderQueueItem = new BlenderRenderQueueItem();
                blenderRenderQueueItem.setProject_uuid(blenderProject.getProject_uuid());
                blenderRenderQueueItem.setProjectName(blenderProject.getProjectName());
                blenderRenderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
                blenderRenderQueueItem.setQueueItem_uuid(UUID.randomUUID().toString());
                blenderRenderQueueItem.setComplete(false);
                blenderRenderQueueItem.setPaused(false);
                blenderRenderQueueItem.setConnection_uuid(null);
                blenderRenderQueueItem.setBlenderFramePart(blenderFramePart);
                blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
            }
            modifyingQueue = false;
            LOG.debug("Render Queue configured, " + blenderRenderQueueDatabaseService.listPendingRender().size() + " items in queue");
        } else {
            pauseBlenderProjectQueue(blenderProject);
        }
    }

    @Override
    public void pauseBlenderProjectQueue(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            LOG.debug("Pausing queue for " + blenderProject.getProjectName());
            List<BlenderRenderQueueItem> blenderRenderQueueItemList =
                    blenderRenderQueueDatabaseService.listQueueItemsByProjectUUID(blenderProject.getProject_uuid());
            for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                if (!blenderRenderQueueItem.isComplete()) {
                    blenderRenderQueueItem.setPaused(true);
                    blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
                }
            }
            blenderProject.setProjectStatus(ProjectStatus.Paused);
            blenderProject.setQueueItemEndTime(System.currentTimeMillis());
            long totalTimeAtPause = blenderProject.getQueueItemEndTime() - blenderProject.getQueueItemStartTime();
            blenderProject.setTotalProjectTime(blenderProject.getTotalRenderTime() + totalTimeAtPause);
            blenderProject.setQueueItemStartTime(0L);
            blenderProject.setQueueItemEndTime(0L);
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            modifyingQueue = false;
        } else {
            pauseBlenderProjectQueue(blenderProject);
        }
    }

    @Override
    public void resumeBlenderProjectQueue(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            LOG.debug("Resuming queue for " + blenderProject.getProjectName());
            List<BlenderRenderQueueItem> blenderRenderQueueItemList =
                    blenderRenderQueueDatabaseService.listQueueItemsByProjectUUID(blenderProject.getProject_uuid());
            for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                if (!blenderRenderQueueItem.isComplete()) {
                    blenderRenderQueueItem.setPaused(false);
                    blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
                }
            }
            blenderProject.setProjectStatus(ProjectStatus.Pending);
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            modifyingQueue = false;
        } else {
            resumeBlenderProjectQueue(blenderProject);
        }

    }

    @Override
    public void stopBlenderProjectQueue(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            LOG.debug("Stopping queue for " + blenderProject.getProjectName());
            blenderRenderQueueDatabaseService.deleteAllByProject(blenderProject.getProject_uuid());
            blenderProject.setProjectStatus(ProjectStatus.Added);
            blenderProject.setQueueItemStartTime(0L);
            blenderProject.setQueueItemEndTime(0L);
            blenderProject.setTotalProjectTime(0L);
            blenderProject.setFrameFileNames(new ArrayList<>());
            blenderProject.setCurrentFrameThumbnail(null);
            blenderProject.setCurrentPercentage(0);
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            modifyingQueue = false;
        } else {
            stopBlenderProjectQueue(blenderProject);
        }
    }

    @Override
    public void nodeRejectQueueItem(String queue_uuid) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            BlenderRenderQueueItem blenderRenderQueueItem = blenderRenderQueueDatabaseService.getByQueueUUID(queue_uuid);
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
            LOG.debug("Received rejection for queue item " + queue_uuid + " adding back to pending queue.");
            blenderRenderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
            blenderRenderQueueItem.setConnection_uuid(null);
            blenderRenderQueueItem.setRendering(false);
            blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
            modifyingQueue = false;
        } else {
            nodeRejectQueueItem(queue_uuid);
        }
    }

    @Override
    public void nodeAcknowledgeQueueItem(String queue_uuid) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            BlenderRenderQueueItem blenderRenderQueueItem = blenderRenderQueueDatabaseService.getByQueueUUID(queue_uuid);
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
            ComputeType computeType = blenderRenderQueueItem.getRenderComputeType();
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(blenderRenderQueueItem.getConnection_uuid());
            LOG.debug("Received node acknowledgement from " + sethlansNode.getHostname() + " for queue item " + queue_uuid);
            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() - 1);
            if (sethlansNode.getAvailableRenderingSlots() < 0) {
                sethlansNode.setAvailableRenderingSlots(0);
            }
            switch (computeType) {
                case CPU:
                    sethlansNode.setCpuSlotInUse(true);
                    break;
                case GPU:
                    sethlansNode.setGpuSlotInUse(true);
                    break;
                default:
                    LOG.error("Invalid compute type, this message should not occur.");
            }
            if (blenderProject.getProjectStatus().equals(ProjectStatus.Pending)) {
                blenderProject.setProjectStatus(ProjectStatus.Started);
            }
            blenderProject.setQueueItemStartTime(System.currentTimeMillis());
            blenderProject.setQueueItemEndTime(0L);
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            modifyingQueue = false;
        } else {
            nodeAcknowledgeQueueItem(queue_uuid);
        }
    }

    @Override
    public void addItemToProcess(BlenderProcessQueueItem blenderProcessQueueItem) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            blenderProcessQueueDatabaseService.saveOrUpdate(blenderProcessQueueItem);
            BlenderRenderQueueItem blenderRenderQueueItem = blenderRenderQueueDatabaseService.getByQueueUUID(blenderProcessQueueItem.getQueueUUID());
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(blenderProcessQueueItem.getConnection_uuid());
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
            ComputeType computeType = blenderRenderQueueItem.getRenderComputeType();
            blenderRenderQueueItem.setRendering(false);
            blenderRenderQueueItem.setComplete(true);
            blenderRenderQueueItem.setPaused(false);
            blenderRenderQueueItem.getBlenderFramePart().setStoredDir(blenderProject.getProjectRootDir() +
                    File.separator + "frame_" + blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() + File.separator);
            blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
            LOG.debug("Completed Render Task received from " + sethlansNode.getHostname() + ". Adding to processing queue.");
            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
            if (sethlansNode.getAvailableRenderingSlots() > sethlansNode.getTotalRenderingSlots()) {
                sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
            }
            switch (computeType) {
                case GPU:
                    sethlansNode.setGpuSlotInUse(false);
                    break;
                case CPU:
                    sethlansNode.setCpuSlotInUse(false);
                    break;
                default:
                    LOG.error("Invalid compute type, this message should not occur.");
            }
            blenderProject.setQueueItemEndTime(System.currentTimeMillis());
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            modifyingQueue = false;
        } else {
            addItemToProcess(blenderProcessQueueItem);
        }
    }

    private void processReceivedQueueItems() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            LOG.debug("Running processing queue. ");
            List<BlenderProcessQueueItem> blenderProcessQueueItemList = blenderProcessQueueDatabaseService.listAll();
            if (!blenderProcessQueueItemList.isEmpty()) {
                for (BlenderProcessQueueItem blenderProcessQueueItem : blenderProcessQueueItemList) {
                    BlenderRenderQueueItem blenderRenderQueueItem = blenderRenderQueueDatabaseService.getByQueueUUID(blenderProcessQueueItem.getQueueUUID());
                    int partNumber = blenderRenderQueueItem.getBlenderFramePart().getPartNumber();
                    int frameNumber = blenderRenderQueueItem.getBlenderFramePart().getFrameNumber();
                    BlenderProject blenderProject =
                            blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
                    int projectTotalQueue =
                            blenderRenderQueueDatabaseService.listQueueItemsByProjectUUID(blenderRenderQueueItem.getProject_uuid()).size();
                    int remainingTotalQueue =
                            blenderRenderQueueDatabaseService.listRemainingQueueItemsByProjectUUID(blenderRenderQueueItem.getProject_uuid()).size();
                    int remainingPartsForFrame =
                            blenderRenderQueueDatabaseService.listRemainingPartsInProjectQueueByFrameNumber(
                                    blenderRenderQueueItem.getProject_uuid(), frameNumber).size();

                    File storedDir = new File(blenderRenderQueueItem.getBlenderFramePart().getStoredDir());
                    storedDir.mkdirs();
                    try {
                        // Save sent file
                        byte[] bytes = blenderProcessQueueItem.getPart().getBytes(1, (int) blenderProcessQueueItem.getPart().length());
                        Path path = Paths.get(storedDir.toString() + File.separator +
                                blenderRenderQueueItem.getBlenderFramePart().getPartFilename() + "." +
                                blenderRenderQueueItem.getBlenderFramePart().getFileExtension());
                        Files.write(path, bytes);


                        LOG.debug("Processing completed render from " +
                                sethlansNodeDatabaseService.getByConnectionUUID(blenderProcessQueueItem.getConnection_uuid()).getHostname() +
                                ". Part: " + partNumber
                                + " Frame: " + frameNumber);


                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }
                    LOG.debug("Remaining parts per frame for Frame " + frameNumber + ": " + remainingPartsForFrame);
                    LOG.debug("Remaining items in project Queue " + remainingTotalQueue);
                    LOG.debug("Project total Queue " + projectTotalQueue);

                    double currentPercentage = ((projectTotalQueue - remainingTotalQueue) * 100.0) / projectTotalQueue;
                    LOG.debug("Current Percentage " + currentPercentage);
                    blenderProject.setCurrentPercentage((int) currentPercentage);

                    blenderProject.setTotalRenderTime(blenderProject.getTotalRenderTime() + blenderProcessQueueItem.getRenderTime());
                    if (remainingTotalQueue > 0) {
                        blenderProject.setProjectStatus(ProjectStatus.Rendering);
                    }
                    if (remainingPartsForFrame == 0) {
                        if (blenderProjectService.combineParts(blenderProject, frameNumber)) {
                            if (remainingTotalQueue == 0) {
                                if (blenderProject.getProjectType() == ProjectType.ANIMATION && blenderProject.getRenderOutputFormat() == RenderOutputFormat.AVI) {
                                    blenderProject.setQueueItemStartTime(System.currentTimeMillis());
                                    blenderProject.setProjectStatus(ProjectStatus.Processing);
                                    blenderProjectService.createAVI(blenderProject);
                                    blenderProject.setQueueItemEndTime(System.currentTimeMillis());

                                }
                                if (blenderProject.getProjectType() == ProjectType.ANIMATION && blenderProject.getRenderOutputFormat() == RenderOutputFormat.MP4) {
                                    blenderProject.setQueueItemStartTime(System.currentTimeMillis());
                                    blenderProject.setProjectStatus(ProjectStatus.Processing);
                                    blenderProjectService.createMP4(blenderProject);
                                    blenderProject.setQueueItemEndTime(System.currentTimeMillis());
                                } else {
                                    blenderProject.setProjectStatus(ProjectStatus.Finished);
                                    blenderRenderQueueDatabaseService.deleteAllByProject(blenderProject.getProject_uuid());
                                }
                                blenderProject.setAllImagesProcessed(true);
                            }
                        }

                    }
                    long time = blenderProject.getQueueItemEndTime() - blenderProject.getQueueItemStartTime();
                    blenderProject.setTotalProjectTime(blenderProject.getTotalProjectTime() + time);
                    blenderProjectDatabaseService.saveOrUpdate(blenderProject);


                }
            }
            modifyingQueue = false;
        }

    }

    private void assignNodeToQueueItem() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.listPendingRender();
            int totalAvailableSlots = sethlansNodeDatabaseService.activeNodeswithFreeSlots().size();
            if (blenderRenderQueueItemList.size() > 0 && totalAvailableSlots > 0) {
                for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                    LOG.debug(blenderRenderQueueItem.getProjectName() + " uuid: " +
                            blenderRenderQueueItem.getProject_uuid() + " Frame: "
                            + blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() + " Part: "
                            + blenderRenderQueueItem.getBlenderFramePart().getPartNumber() + " is waiting to be rendered.");
                    SethlansNode sethlansNode;
                    switch (blenderRenderQueueItem.getRenderComputeType()) {
                        case CPU_GPU:
                            getSortedNodeList(ComputeType.CPU_GPU);
                            sethlansNode = sortedSethlansNodeList.get(0);
                            blenderRenderQueueItem.setConnection_uuid(sethlansNode.getConnection_uuid());
                            blenderRenderQueueItem = setQueueItemComputeType(sethlansNode, blenderRenderQueueItem);
                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() - 1);
                            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                            break;
                        case CPU:
                            getSortedNodeList(ComputeType.CPU);
                            sethlansNode = sortedSethlansNodeList.get(0);
                            blenderRenderQueueItem.setConnection_uuid(sethlansNode.getConnection_uuid());
                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() - 1);
                            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                            break;
                        case GPU:
                            getSortedNodeList(ComputeType.GPU);
                            sethlansNode = sortedSethlansNodeList.get(0);
                            blenderRenderQueueItem.setConnection_uuid(sethlansNode.getConnection_uuid());
                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() - 1);
                            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                            break;
                    }
                    blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
                }
            }
            modifyingQueue = false;
        }
    }

    private void sendQueueItemsToAssignedNode() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.listPendingRenderWithNodeAssigned();
            for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
                SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(blenderRenderQueueItem.getConnection_uuid());
                String connectionURL = "https://" + sethlansNode.getIpAddress() + ":" +
                        sethlansNode.getNetworkPort() + "/api/render/request";
                String params = "project_name=" + blenderProject.getProjectName() +
                        "&connection_uuid=" + sethlansNode.getConnection_uuid() +
                        "&project_uuid=" + blenderProject.getProject_uuid() +
                        "&queue_item_uuid=" + blenderRenderQueueItem.getQueueItem_uuid() +
                        "&render_output_format=" + blenderProject.getRenderOutputFormat() +
                        "&samples=" + blenderProject.getSamples() +
                        "&blender_engine=" + blenderProject.getBlenderEngine() +
                        "&compute_type=" + blenderRenderQueueItem.getRenderComputeType() +
                        "&blend_file=" + blenderProject.getBlendFilename() +
                        "&blender_version=" + blenderProject.getBlenderVersion() +
                        "&frame_filename=" + blenderRenderQueueItem.getBlenderFramePart().getFrameFileName() +
                        "&part_filename=" + blenderRenderQueueItem.getBlenderFramePart().getPartFilename() +
                        "&frame_number=" + blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() +
                        "&part_number=" + blenderRenderQueueItem.getBlenderFramePart().getPartNumber() +
                        "&part_resolution_x=" + blenderProject.getResolutionX() +
                        "&part_resolution_y=" + blenderProject.getResolutionY() +
                        "&part_position_min_y=" + blenderRenderQueueItem.getBlenderFramePart().getPartPositionMinY() +
                        "&part_position_max_y=" + blenderRenderQueueItem.getBlenderFramePart().getPartPositionMaxY() +
                        "&part_res_percentage=" + blenderProject.getResPercentage() +
                        "&file_extension=" + blenderRenderQueueItem.getBlenderFramePart().getFileExtension();
                LOG.debug("Sending " + blenderRenderQueueItem + " to " + sethlansNode.getHostname());
                sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
                blenderRenderQueueItem.setRendering(true);
                blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
            }
            modifyingQueue = false;
        }
    }

    @Override
    public void nodeIdle(String connection_uuid, ComputeType computeType) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
            switch (computeType) {
                case GPU:
                    sethlansNode.setGpuSlotInUse(false);
                    break;
                case CPU:
                    sethlansNode.setCpuSlotInUse(false);
                    break;
                case CPU_GPU:
                    sethlansNode.setCpuSlotInUse(false);
                    sethlansNode.setGpuSlotInUse(false);
                    break;
            }

            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
            if (sethlansNode.getAvailableRenderingSlots() > sethlansNode.getTotalRenderingSlots()) {
                sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
            }

            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            List<BlenderRenderQueueItem> listOfItemsWIthNode = blenderRenderQueueDatabaseService.listQueueItemsByConnectionUUID(connection_uuid);
            for (BlenderRenderQueueItem blenderRenderQueueItem : listOfItemsWIthNode) {
                if (!blenderRenderQueueItem.isComplete()) {
                    blenderRenderQueueItem.setConnection_uuid(null);
                    blenderRenderQueueItem.setRendering(false);
                    BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
                    blenderRenderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
                }
            }
            modifyingQueue = false;
        }

    }


    private BlenderRenderQueueItem setQueueItemComputeType(SethlansNode sethlansNode, BlenderRenderQueueItem blenderRenderQueueItem) {
        // Before sending to a node the compute type must be either GPU or CPU,  CPU&GPU is only used for sorting at the server level.
        if (sethlansNode.getCombinedGPURating() < sethlansNode.getCpuRating() && !sethlansNode.isGpuSlotInUse()) {
            blenderRenderQueueItem.setRenderComputeType(ComputeType.GPU);
            return blenderRenderQueueItem;
        } else if (sethlansNode.getCombinedGPURating() > sethlansNode.getCpuRating() && !sethlansNode.isCpuSlotInUse()) {
            blenderRenderQueueItem.setRenderComputeType(ComputeType.CPU);
            return blenderRenderQueueItem;
        } else if (sethlansNode.getCombinedGPURating() == sethlansNode.getCpuRating() && !sethlansNode.isCpuSlotInUse()) {
            blenderRenderQueueItem.setRenderComputeType(ComputeType.CPU);
            return blenderRenderQueueItem;

        } else if (sethlansNode.isCpuSlotInUse()) {
            blenderRenderQueueItem.setRenderComputeType(ComputeType.GPU);
            return blenderRenderQueueItem;

        } else if (sethlansNode.isGpuSlotInUse()) {
            blenderRenderQueueItem.setRenderComputeType(ComputeType.CPU);
            return blenderRenderQueueItem;

        } else {
            blenderRenderQueueItem.setRenderComputeType(ComputeType.CPU);
            return blenderRenderQueueItem;
        }
    }

    private void getSortedNodeList(ComputeType computeType) {
        for (SethlansNode sethlansNode : sethlansNodeDatabaseService.listAll()) {
            if (sethlansNode.getAvailableRenderingSlots() > 0 && sethlansNode.isBenchmarkComplete() && sethlansNode.isActive()) {
                SethlansUtils.listofNodes(computeType, sortedSethlansNodeList, sethlansNode);
            }
        }
        if (SethlansUtils.sortedNodeList(computeType, sortedSethlansNodeList)) {
            LOG.debug("Returned List:");
            for (SethlansNode sethlansNode : sortedSethlansNodeList) {
                LOG.debug(sethlansNode.getHostname() +
                        ": Available Slots(" + sethlansNode.getAvailableRenderingSlots() +
                        "). Compute Type(" + sethlansNode.getComputeType().getName() +
                        "). CPU in use(" + sethlansNode.isCpuSlotInUse() +
                        "). GPU in use (" + sethlansNode.isGpuSlotInUse() + ")");
            }
        }
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
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderProcessQueueDatabaseService(BlenderProcessQueueDatabaseService blenderProcessQueueDatabaseService) {
        this.blenderProcessQueueDatabaseService = blenderProcessQueueDatabaseService;
    }

    @Autowired
    public void setBlenderProjectService(BlenderProjectService blenderProjectService) {
        this.blenderProjectService = blenderProjectService;
    }
}


