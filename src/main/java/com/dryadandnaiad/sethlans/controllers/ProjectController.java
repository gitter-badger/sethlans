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
import com.dryadandnaiad.sethlans.domains.info.ProjectInfo;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.forms.ProjectForm;
import com.dryadandnaiad.sethlans.services.blender.BlenderParseBlendFileService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.storage.WebUploadService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 3/27/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

@RestController
@Profile({"SERVER", "DUAL"})
public class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

    @Value("${sethlans.benchmarkDir}")
    private String benchmarkDir;

    @Value("${sethlans.blenderDir}")
    private String blenderDir;

    @Value("${sethlans.projectDir}")
    private String projectDir;

    @Value("${sethlans.tempDir}")
    private String temp;

    private WebUploadService webUploadService;
    private BlenderParseBlendFileService blenderParseBlenderFileService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;


    @GetMapping(value = "/api/project_ui/num_of_projects")
    public Integer numberOfProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return blenderProjectDatabaseService.listAllReverse().size();
        } else {
            return blenderProjectDatabaseService.getProjectsByUser(auth.getName()).size();
        }
    }

    @GetMapping(value = "/api/project_ui/nodes_ready")
    public boolean nodesReady() {
        return sethlansNodeDatabaseService.activeNodes();
    }

    @GetMapping(value = "/api/project_ui/project_list")
    public List<ProjectInfo> getProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return convertBlenderProjectsToProjectInfo(blenderProjectDatabaseService.listAllReverse());
        } else {
            return convertBlenderProjectsToProjectInfo(blenderProjectDatabaseService.getProjectsByUser(auth.getName()));
        }

    }

    @GetMapping(value = "/api/project_actions/delete_project/{id}")
    public boolean deleteProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProjectDatabaseService.delete(id);
            return true;
        } else {
            return blenderProjectDatabaseService.deleteWithVerification(auth.getName(), id);
        }
    }

    @GetMapping(value = "/api/project_ui/project_details/{id}")
    public ProjectInfo getProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.getById(id));
        } else {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.getProjectByUser(auth.getName(), id));
        }
    }

    @PostMapping(value = "/api/project_form/upload_project")
    public ProjectForm newProjectUpload(@RequestParam("projectFile") MultipartFile projectFile) {
        LOG.debug("Upload Attempted");
        String uploadTag = SethlansUtils.getShortUUID();
        webUploadService.store(projectFile, uploadTag);
        ProjectForm newProject = new ProjectForm();
        newProject.setUploadedFile(projectFile.getOriginalFilename());
        newProject.setFileLocation(temp + uploadTag + "-" + projectFile.getOriginalFilename());
        newProject.populateForm(blenderParseBlenderFileService.parseBlendFile(newProject.getFileLocation()));
        LOG.debug(newProject.toString());
        return newProject;
    }

    @PostMapping(value = "/api/project_form/submit_project")
    public boolean submitProject(@RequestBody ProjectForm projectForm) {
        if (projectForm != null) {
            LOG.debug("Project Submitted" + projectForm);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            projectForm.setUsername(auth.getName());
            projectForm.setProjectStatus(ProjectStatus.NOT_STARTED);
            blenderProjectDatabaseService.saveOrUpdateProjectForm(projectForm);
            return true;
        }
        return false;
    }

    private List<ProjectInfo> convertBlenderProjectsToProjectInfo(List<BlenderProject> projectsToConvert) {
        List<ProjectInfo> projectsToReturn = new ArrayList<>();
        for (BlenderProject blenderProject : projectsToConvert) {
            projectsToReturn.add(convertBlenderProjectToProjectInfo(blenderProject));
        }
        return projectsToReturn;
    }

    private ProjectInfo convertBlenderProjectToProjectInfo(BlenderProject blenderProject) {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setId(blenderProject.getId());
        projectInfo.setStartFrame(blenderProject.getStartFrame());
        projectInfo.setEndFrame(blenderProject.getEndFrame());
        projectInfo.setStepFrame(blenderProject.getStepFrame());
        projectInfo.setSamples(blenderProject.getSamples());
        projectInfo.setProjectStatus(blenderProject.getProjectStatus());
        projectInfo.setProjectType(blenderProject.getProjectType());
        projectInfo.setProjectName(blenderProject.getProjectName());
        projectInfo.setSelectedBlenderversion(blenderProject.getBlenderVersion());
        projectInfo.setRenderOn(blenderProject.getRenderOn());
        projectInfo.setOutputFormat(blenderProject.getRenderOutputFormat());
        projectInfo.setUsername(blenderProject.getSethlansUser().getUsername());
        projectInfo.setResolutionX(blenderProject.getResolutionX());
        projectInfo.setResolutionY(blenderProject.getResolutionY());
        projectInfo.setBlenderEngine(blenderProject.getBlenderEngine());
        projectInfo.setResPercentage(blenderProject.getResPercentage());
        projectInfo.setPartsPerFrame(blenderProject.getPartsPerFrame());
        if (projectInfo.getPartsPerFrame() > 1) {
            projectInfo.setUseParts(true);
        } else {
            projectInfo.setUseParts(false);
        }
        return projectInfo;
    }


    @Autowired
    public void setWebUploadService(WebUploadService webUploadService) {
        this.webUploadService = webUploadService;
    }


    @Autowired
    public void setBlenderParseBlenderFileService(BlenderParseBlendFileService blenderParseBlenderFileService) {
        this.blenderParseBlenderFileService = blenderParseBlenderFileService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }
}
