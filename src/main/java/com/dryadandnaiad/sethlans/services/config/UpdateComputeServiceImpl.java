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
package com.dryadandnaiad.sethlans.services.config;

import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansUtils.getGPUDeviceString;
import static com.dryadandnaiad.sethlans.utils.SethlansUtils.writeProperty;

/**
 * Created Mario Estrella on 3/5/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class UpdateComputeServiceImpl implements UpdateComputeService {
    private SethlansServerDatabaseService sethlansServerDatabaseService;

    @Override
    public boolean saveComputeSettings(SetupNode setupNode) {

        writeProperty(SethlansConfigKeys.COMPUTE_METHOD, setupNode.getComputeMethod().toString());
        switch (setupNode.getComputeMethod()) {
            case CPU:
                writeProperty(SethlansConfigKeys.CPU_CORES, setupNode.getCores().toString());
                writeProperty(SethlansConfigKeys.TILE_SIZE_CPU, setupNode.getTileSizeCPU().toString());
                writeProperty(SethlansConfigKeys.GPU_DEVICE, "");
                return setNodeUpdated();
            case GPU:
                writeProperty(SethlansConfigKeys.TILE_SIZE_GPU, setupNode.getTileSizeGPU().toString());
                writeProperty(SethlansConfigKeys.GPU_DEVICE, getGPUDeviceString(setupNode));
                writeProperty(SethlansConfigKeys.CPU_CORES, "");
                return setNodeUpdated();

            case CPU_GPU:
                writeProperty(SethlansConfigKeys.CPU_CORES, setupNode.getCores().toString());
                writeProperty(SethlansConfigKeys.TILE_SIZE_CPU, setupNode.getTileSizeCPU().toString());
                writeProperty(SethlansConfigKeys.TILE_SIZE_GPU, setupNode.getTileSizeGPU().toString());
                writeProperty(SethlansConfigKeys.GPU_DEVICE, getGPUDeviceString(setupNode));
                return setNodeUpdated();

        }
        return false;
    }

    private boolean setNodeUpdated() {
        List<SethlansServer> sethlansServerList = sethlansServerDatabaseService.listAll();
        for (SethlansServer sethlansServer : sethlansServerList) {
            sethlansServer.setNodeUpdated(true);
            sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
        }
        return true;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }
}
