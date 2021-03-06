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

import com.dryadandnaiad.sethlans.domains.blender.PartCoordinates;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderProjectServiceImpl implements BlenderProjectService {
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private BlenderQueueService blenderQueueService;
    private static final Logger LOG = LoggerFactory.getLogger(BlenderProjectServiceImpl.class);

    @Override
    @Async
    public void startProject(BlenderProject blenderProject) {
        configureFrameList(blenderProject);
        while (!blenderQueueService.populateQueueWithProject(blenderProject)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resumeProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        sendResumeToQueueService(blenderProject);
    }


    @Override
    public void resumeProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        sendResumeToQueueService(blenderProject);

    }

    private void sendResumeToQueueService(BlenderProject blenderProject) {
        if (!blenderProject.isAllImagesProcessed()) {
            while (!blenderQueueService.resumeBlenderProjectQueue(blenderProject)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void pauseProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        sendPauseToQueueService(blenderProject);
    }

    @Override
    public void pauseProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        sendPauseToQueueService(blenderProject);
    }

    private void sendPauseToQueueService(BlenderProject blenderProject) {
        if (!blenderProject.isAllImagesProcessed()) {
            while (!blenderQueueService.pauseBlenderProjectQueue(blenderProject)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void stopProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        while (!blenderQueueService.stopBlenderProjectQueue(blenderProject)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int count = blenderProject.getFrameFileNames().size();
        deleteProjectFrames(blenderProject, count);
    }

    @Override
    public void stopProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        while (!blenderQueueService.stopBlenderProjectQueue(blenderProject)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int count = blenderProject.getFrameFileNames().size();
        deleteProjectFrames(blenderProject, count);

    }


    @Override
    public void deleteProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        blenderQueueService.stopBlenderProjectQueue(blenderProject);
        String directory = blenderProject.getProjectRootDir();
        blenderProjectDatabaseService.delete(id);
        deleteDirectory(directory);
    }

    @Override
    public void deleteProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        blenderQueueService.stopBlenderProjectQueue(blenderProject);
        String directory = blenderProject.getProjectRootDir();
        blenderProjectDatabaseService.deleteWithVerification(username, id);
        deleteDirectory(directory);
    }

    private void deleteDirectory(String directory) {
        try {
            Thread.sleep(5000);
            FileUtils.deleteDirectory(new File(directory));
        } catch (InterruptedException | IOException e) {
            LOG.error("Error occurred deleting project " + e.getMessage());
        }
    }


    private void deleteProjectFrames(BlenderProject blenderProject, int count) {
        for (int i = 0; i < count; i++) {
            int frame = i + 1;
            try {
                FileUtils.deleteDirectory(new File(blenderProject.getProjectRootDir() + File.separator + "frame_" + frame));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void configureFrameList(BlenderProject blenderProject) {
        List<BlenderFramePart> blenderFramePartList = new ArrayList<>();
        List<String> frameFileNames = new ArrayList<>();
        List<PartCoordinates> partCoordinatesList = configurePartCoordinates(blenderProject.getPartsPerFrame());
        for (int i = 0; i < blenderProject.getTotalNumOfFrames(); i++) {
            frameFileNames.add(blenderProject.getProject_uuid() + "-" + (i + 1));
            for (int j = 0; j < blenderProject.getPartsPerFrame(); j++) {
                BlenderFramePart blenderFramePart = new BlenderFramePart();
                blenderFramePart.setFrameFileName(frameFileNames.get(i));
                blenderFramePart.setFrameNumber(i + 1);
                blenderFramePart.setPartNumber(j + 1);
                blenderFramePart.setPartFilename(blenderFramePart.getFrameFileName() + "-part" + (j + 1));
                blenderFramePart.setPartPositionMaxY(partCoordinatesList.get(j).getMax_y());
                blenderFramePart.setPartPositionMinY(partCoordinatesList.get(j).getMin_y());
                blenderFramePart.setFileExtension("png");
                blenderFramePartList.add(blenderFramePart);

            }
        }
        blenderProject.setFramePartList(blenderFramePartList);
        LOG.debug("Project Frames configured " + blenderFramePartList);
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);

    }

    private List<PartCoordinates> configurePartCoordinates(int partsPerFrame) {
        List<PartCoordinates> partCoordinatesList = new ArrayList<>();
        Integer parts = partsPerFrame;
        Double upperLimit = 1.0;
        Double difference = upperLimit / parts;
        LOG.debug("Slice Interval " + difference);
        Double startingPoint = upperLimit;
        Double endingPoint;
        for (int i = 0; i < parts; i++) {
            LOG.debug("Starting Point " + startingPoint);
            endingPoint = startingPoint - difference;
            if (i == parts - 1) {
                endingPoint = 0.0;
            }
            PartCoordinates partCoordinates = new PartCoordinates();
            partCoordinates.setMax_y(startingPoint);
            partCoordinates.setMin_y(endingPoint);
            partCoordinatesList.add(partCoordinates);
            startingPoint = endingPoint;
            LOG.debug("Ending Point " + endingPoint);

        }
        LOG.debug("Part Coordinate List generated " + partCoordinatesList);

        return partCoordinatesList;
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
