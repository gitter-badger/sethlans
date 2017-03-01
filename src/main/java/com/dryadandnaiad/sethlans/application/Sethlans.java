/*
 * Copyright (C) 2017 Dryad and Naiad Software LLC
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
 */
package com.dryadandnaiad.sethlans.application;

import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.StringKey;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.helper.SethlansConfiguration;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.dryadandnaiad.sethlans.webui.SethlansWebUI;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public class Sethlans {

    private static final Logger logger = LogManager.getLogger(Sethlans.class);
    private SethlansConfiguration config = SethlansConfiguration.getInstance();
    private String noArgs[] = null; // For JavaFX GUI launch, no args needed, they're set on the config

    public static void main(String[] args) {
        logger.info("********************* " + SethlansUtils.getString(StringKey.APP_NAME) + " Startup" + " *********************");
        new Sethlans().doMain(args);
    }

    public void doMain(String[] args) {

        config.setLoglevel(LogLevel.DEBUG);

        CmdLineParser cmdParser = new CmdLineParser(this);

        try {
            cmdParser.parseArgument(args);

        } catch (CmdLineException e) {
            logger.error(e.getMessage());
            System.err.println(e.getMessage());
            System.err.println("Usage: ");
            cmdParser.printUsage(System.err);
            System.err.println();
            System.err.println("Example: java " + "-jar sethlans.jar -ui gui" + cmdParser.printExample(OptionHandlerFilter.REQUIRED));
            return;

        }
        if (help) {
            cmdParser.printUsage(System.out);
            return;
        }

        if (persist) {
             if (method != null) {
                config.setComputeMethod(method);
            }
            if (cores != 0) {
                config.setCores(cores);
            }
            if (mode != null) {
                config.setMode(mode);
            }
            if (logLevel != null) {
                config.setLoglevel(logLevel);
            }
            if (httpPort != null) {
                config.setHttpPort(httpPort);
            }
            if (httpsPort != null) {
                config.setHttpsPort(httpsPort);
            }
        }
        

        startTomcat();

    }

    private void startTomcat() {
        logger.info("Starting Sethlans Web UI");
        if(httpPort == null) {
            httpPort = config.getHttpPort();            
        }
        if (httpsPort == null) {
            httpsPort = config.getHttpsPort();            
        }
        logger.debug("HTTP Port is set to: " + httpPort);
        logger.debug("HTTPS Port is set to: " + httpsPort);
        SethlansWebUI.start(httpPort, httpsPort);

    }

    // Command line options
    @Option(name = "-help", aliases = "-h", usage = "Displays this screen\n", required = false, help = true, handler = com.dryadandnaiad.sethlans.utils.HelpOptionHandler.class)
    private boolean help;

    @Option(name = "-compute-method", usage = "CPU: only use cpu, "
            + "GPU: only use gpu, CPU_GPU: can use cpu and gpu "
            + "(not at the same time) if -gpu is not use it will not "
            + "use the gpu", metaVar = "CPU", required = false)
    private ComputeType method = null;

    @Option(name = "-cores", usage = "Number of cores/threads to use for the "
            + "render", metaVar = "1", required = false)
    private int cores;

    @Option(name = "-mode", usage = "Specify whether to operate as a server, node, or both(default)", required = false)
    private SethlansMode mode = null;

    @Option(name = "-loglevel", usage = "Sets the debug level for log file.  info: normal information messages(default), debug: turns on debug logging", required = false)
    private LogLevel logLevel;

    @Option(name = "-persist", usage = "Options passed via command line are saved and automatically used next startup", required = false)
    private boolean persist;
    
    @Option(name = "-http-port", usage = "Sets the http port for the WEB UI", metaVar = "8443" , required = false)
    String httpPort=null;
    
    @Option(name = "-https-port", usage = "Sets the https port for the WEB UI", metaVar = "8993" , required = false)
    String httpsPort=null;

}
