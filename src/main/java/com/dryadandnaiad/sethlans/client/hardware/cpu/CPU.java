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

package com.dryadandnaiad.sethlans.client.hardware.cpu;

/**
 * Created Mario Estrella on 3/19/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class CPU {
    private String name;
    private String model;
    private String family;
    private String arch; // 32 or 64 bits

    public CPU() {
        this.name = null;
        this.model = null;
        this.family = null;
        this.generateArch();
    }

    public String name() {
        return this.name;
    }

    public String model() {
        return this.model;
    }

    public String family() {
        return this.family;
    }

    public String arch() {
        return this.arch;
    }

    public int cores() {
        return Runtime.getRuntime().availableProcessors();
    }

    public void setName(String name_) {
        this.name = name_;
    }

    public void setModel(String model_) {
        this.model = model_;
    }

    public void setFamily(String family_) {
        this.family = family_;
    }

    public void setArch(String arch_) {
        this.arch = arch_;
    }

    public void generateArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        switch (arch) {
            case "i386":
            case "i686":
            case "x86":
                this.arch = "32bit";
                break;
            case "amd64":
            case "x86_64":
                this.arch = "64bit";
                break;
            default:
                this.arch = null;
                break;
        }
    }

    public boolean haveData() {
        return this.name != null && this.model != null && this.family != null && this.arch != null;
    }

}