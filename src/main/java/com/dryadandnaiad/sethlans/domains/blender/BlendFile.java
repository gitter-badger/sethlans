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

package com.dryadandnaiad.sethlans.domains.blender;

/**
 * Created Mario Estrella on 3/30/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class BlendFile {
    private String sceneName;
    private String engine;
    private int frameStart;
    private int frameEnd;
    private int frameSkip;
    private int resPercent;
    private int resolutionX;
    private int resolutionY;
    private String cameraName;
    private int cyclesSamples;

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public int getFrameStart() {
        return frameStart;
    }

    public void setFrameStart(int frameStart) {
        this.frameStart = frameStart;
    }

    public int getFrameEnd() {
        return frameEnd;
    }

    public void setFrameEnd(int frameEnd) {
        this.frameEnd = frameEnd;
    }

    public int getFrameSkip() {
        return frameSkip;
    }

    public void setFrameSkip(int frameSkip) {
        this.frameSkip = frameSkip;
    }

    public int getResPercent() {
        return resPercent;
    }

    public void setResPercent(int resPercent) {
        this.resPercent = resPercent;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public void setResolutionX(int resolutionX) {
        this.resolutionX = resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public void setResolutionY(int resolutionY) {
        this.resolutionY = resolutionY;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public int getCyclesSamples() {
        return cyclesSamples;
    }

    public void setCyclesSamples(int cyclesSamples) {
        this.cyclesSamples = cyclesSamples;
    }

    @Override
    public String toString() {
        return "BlendFile{" +
                "sceneName='" + sceneName + '\'' +
                ", engine='" + engine + '\'' +
                ", frameStart=" + frameStart +
                ", frameEnd=" + frameEnd +
                ", frameSkip=" + frameSkip +
                ", resPercent=" + resPercent +
                ", resolutionX=" + resolutionX +
                ", resolutionY=" + resolutionY +
                ", cameraName='" + cameraName + '\'' +
                ", cyclesSamples=" + cyclesSamples +
                '}';
    }
}