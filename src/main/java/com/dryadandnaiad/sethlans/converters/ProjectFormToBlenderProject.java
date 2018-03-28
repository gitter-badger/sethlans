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

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.forms.ProjectForm;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProjectFormToBlenderProject implements Converter<ProjectForm, BlenderProject> {
    private SethlansUserDatabaseService sethlansUserDatabaseService;

    @Override
    public BlenderProject convert(ProjectForm projectForm) {
        BlenderProject blenderProject = new BlenderProject();
        blenderProject.setBlenderEngine(projectForm.getBlenderEngine());
        blenderProject.setProjectStatus(projectForm.getProjectStatus());
        blenderProject.setRenderOn(projectForm.getRenderOn());
        blenderProject.setProject_uuid(projectForm.getUuid());
        blenderProject.setProjectName(projectForm.getProjectName());
        blenderProject.setBlenderVersion(projectForm.getSelectedBlenderversion());
        blenderProject.setBlendFileLocation(projectForm.getFileLocation());
        blenderProject.setSethlansUser(sethlansUserDatabaseService.findByUserName(projectForm.getUsername()));
        blenderProject.setProjectType(projectForm.getProjectType());
        blenderProject.setStartFrame(projectForm.getStartFrame());
        blenderProject.setEndFrame(projectForm.getEndFrame());
        blenderProject.setStepFrame(projectForm.getStepFrame());
        blenderProject.setResolutionX(projectForm.getResolutionX());
        blenderProject.setResolutionY(projectForm.getResolutionY());
        blenderProject.setResPercentage(projectForm.getResPercentage());
        blenderProject.setBlendFilename(projectForm.getUploadedFile());
        blenderProject.setPartsPerFrame(projectForm.getPartsPerFrame());
        return blenderProject;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
