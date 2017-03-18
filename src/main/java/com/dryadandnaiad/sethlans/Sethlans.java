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

package com.dryadandnaiad.sethlans;

import com.dryadandnaiad.sethlans.enums.SethlansMode;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LogLevel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created Mario Estrella on 3/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@SpringBootApplication
public class Sethlans {
    private static final Logger LOG = LoggerFactory.getLogger(Sethlans.class);
    // Command line options
    @Option(name = "-help", aliases = "-h", usage = "Displays this screen\n", required = false, help = true, handler = com.dryadandnaiad.sethlans.utils.HelpOptionHandler.class)
    private boolean help;
    @Option(name = "-mode", usage = "Specify whether to operate as a server, node, or both(default)", required = false)
    private SethlansMode mode = null;
    @Option(name = "-loglevel", usage = "Sets the debug level for log file.  info: normal information messages(default), debug: turns on debug logging", required = false)
    private LogLevel logLevel;
    @Option(name = "-persist", usage = "Options passed via commands line are saved and automatically used next startup", required = false)
    private boolean persist;
    @Option(name = "-http-port", usage = "Sets the http port for the WEB UI", metaVar = "7007", required = false)
    private String httpPort = null;
    @Option(name = "-https-port", usage = "Sets the https port for the WEB UI", metaVar = "7443", required = false)
    private String httpsPort = null;

    public static void main(String[] args) {
        new Sethlans().doMain(args);
    }

    public void doMain(String[] args) {
        CmdLineParser cmdParser = new CmdLineParser(this);
        List<String> arrayArgs = new ArrayList<String>();
        try {
            cmdParser.parseArgument(args);
        } catch (CmdLineException e) {
            LOG.error(e.getMessage());
            System.err.println(e.getMessage());
            System.err.println("Usage: ");
            cmdParser.printUsage(System.err);
            System.err.println();
            System.err.println("Example: java -jar sethlans-x.x.jar -http-port 7007 -persist" + cmdParser.printExample(OptionHandlerFilter.REQUIRED));
            return;
        }

        if (help) {
            cmdParser.printUsage(System.out);
            return;
        }

        if (httpPort != null) {
            arrayArgs.add("--server.port=" + httpPort);
        }

        if (logLevel != null) {
            arrayArgs.add("--logging.level.com.dryadandnaiad=" + logLevel);
        }

        if (persist) {
            LOG.info("Saving commands-line options to config file.");
        }

        String[] springArgs = new String[arrayArgs.size()];
        springArgs = arrayArgs.toArray(springArgs);
        startSpring(springArgs);
    }


    private void startSpring(String[] springArgs) {
        if (SystemTray.isSupported()) {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(Sethlans.class);
            builder.headless(false);
            builder.run(springArgs);
        } else {
            SpringApplication.run(Sethlans.class, springArgs);
        }
    }

}
