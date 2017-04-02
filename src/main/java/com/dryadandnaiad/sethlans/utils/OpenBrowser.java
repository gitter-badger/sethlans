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

package com.dryadandnaiad.sethlans.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
/**
 * Created Mario Estrella on 3/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class OpenBrowser {
    private static String port;
    private static final Logger LOG = LoggerFactory.getLogger(OpenBrowser.class);

    @Value("${server.port}")
    public void setPort(String port) {
        OpenBrowser.port = port;
    }

    public static void start() throws MalformedURLException {

        LOG.debug("Opening Browser");
        URL url = new URL("https://localhost:" + port + "/");
        SethlansUtils.openWebpage(url);

    }

    public static void dbConsole() throws MalformedURLException {
        LOG.debug("Opening DB console");
        URL url = new URL("https://localhost:" + port + "/console");
        SethlansUtils.openWebpage(url);
    }

    public static void about() throws MalformedURLException {
        LOG.debug("Showing About Page");
    }
}
