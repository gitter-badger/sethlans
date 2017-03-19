/*
 * Copyright (C) 2010-2017 Laurent CLOUET
 * Author Laurent CLOUET <laurent.clouet@nopnop.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.dryadandnaiad.sethlans.client.software;

import com.dryadandnaiad.sethlans.client.hardware.cpu.CPU;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created Mario Estrella on 3/19/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public abstract class AbstractOSClass {
    public abstract String name();

    public abstract CPU getCPU();

    public abstract int getMemory();

    public abstract String getRenderBinaryPath();

    public String getCUDALib() {
        return null;
    }

    public Process exec(List<String> command, Map<String, String> env) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        if (env != null) {
            builder.environment().putAll(env);
        }
        return builder.start();
    }

    public boolean kill(Process proc) {
        if (proc != null) {
            proc.destroy();
            return true;
        }
        return false;
    }

    public static AbstractOSClass getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new Windows();
        } else if (os.contains("mac")) {
            return new Mac();
        } else if (os.contains("nix") || os.contains("nux")) {
            return new Linux();
        } else {
            return null;
        }
    }
}
