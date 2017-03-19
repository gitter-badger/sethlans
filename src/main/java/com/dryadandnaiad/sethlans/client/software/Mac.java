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

package com.dryadandnaiad.sethlans.client.software;

import com.dryadandnaiad.sethlans.client.hardware.cpu.CPU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created Mario Estrella on 3/19/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class Mac extends AbstractOSClass {
    private final String NICE_BINARY_PATH = "nice";
    private Boolean hasNiceBinary;
    private static final Logger LOG = LoggerFactory.getLogger(Mac.class);

    public Mac() {
        super();
        this.hasNiceBinary = null;
    }

    public String name() {
        return "mac";
    }

    @Override
    public String getRenderBinaryPath() {
        return "Blender" + File.separator + "blender.app" + File.separator + "Contents" + File.separator + "MacOS" + File.separator + "blender";
    }

    @Override
    public CPU getCPU() {
        CPU ret = new CPU();

        String command = "sysctl machdep.cpu.family machdep.cpu.brand_string";

        Process p = null;
        BufferedReader input = null;
        try {
            String line;
            p = Runtime.getRuntime().exec(command);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = input.readLine()) != null) {
                String option_cpu_family = "machdep.cpu.family:";
                String option_model_name = "machdep.cpu.brand_string:";
                if (line.startsWith(option_model_name)) {
                    ret.setName(line.substring(option_model_name.length()).trim());
                }
                if (line.startsWith(option_cpu_family)) {
                    ret.setFamily(line.substring(option_cpu_family.length()).trim());
                }
            }
            input.close();
            input = null;
        } catch (Exception err) {
            LOG.error("exception" + err.getMessage());
            ret.setName("Unknown Mac name");
            ret.setFamily("Unknown Mac family");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.error("IOException" + e.getMessage());
                }
            }

            if (p != null) {
                p.destroy();
            }
        }

        ret.setModel("Unknown");

        return ret;
    }

    @Override
    public int getMemory() {
        String command = "sysctl hw.memsize";

        Process p = null;
        BufferedReader input = null;
        try {
            String line;
            p = Runtime.getRuntime().exec(command);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = input.readLine()) != null) {
                String option = "hw.memsize:";
                if (line.startsWith(option)) {
                    String memory = line.substring(option.length()).trim(); // memory in bytes

                    return (int) (Long.parseLong(memory) / 1024);
                }
            }
            input.close();
            input = null;
        } catch (Exception err) {
            LOG.error("Exception" + err.getMessage());
            err.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }

            if (p != null) {
                p.destroy();
            }
        }

        return -1;
    }

    @Override
    public Process exec(List<String> command, Map<String, String> env) throws IOException {
        List<String> actual_command = command;
        if (this.hasNiceBinary == null) {
            this.checkNiceAvailability();
        }
        if (this.hasNiceBinary.booleanValue()) {
            // launch the process in lowest priority
            if (env != null) {
                actual_command.add(0, env.get("PRIORITY"));
            } else {
                actual_command.add(0, "19");
            }
            actual_command.add(0, "-n");
            actual_command.add(0, NICE_BINARY_PATH);
        } else {
            LOG.error("No low priority binary, will not launch renderer in normal priority");
        }
        ProcessBuilder builder = new ProcessBuilder(actual_command);
        builder.redirectErrorStream(true);
        if (env != null) {
            builder.environment().putAll(env);
        }
        return builder.start();
    }

    @Override
    public String getCUDALib() {
        return "/usr/local/cuda/lib/libcuda.dylib";
    }

    protected void checkNiceAvailability() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(NICE_BINARY_PATH);
        builder.redirectErrorStream(true);
        Process process = null;
        try {
            process = builder.start();
            this.hasNiceBinary = true;
        } catch (IOException e) {
            this.hasNiceBinary = false;
            LOG.error("Failed to find low priority binary, will not launch renderer in normal priority" + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}