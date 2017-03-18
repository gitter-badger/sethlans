/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.components;

import com.dryadandnaiad.sethlans.components.systray.SethlansSystray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;

/**
 * Created Mario Estrella on 3/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class SethlansSystrayComponent {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansSystrayComponent.class);


    @PostConstruct
    public void startSystray() {
        if (SystemTray.isSupported()) {
            LOG.debug("System Tray is Supported");
            SethlansSystray sethlansSystray = new SethlansSystray();
            sethlansSystray.setup();
            sethlansSystray.setImageAutoSize(true);
        } else {
            LOG.debug("System Tray is Not Supported");
        }
    }
}
