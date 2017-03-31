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

import com.dryadandnaiad.sethlans.domains.blender.BlenderZip;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataServiceImpl;
import com.google.common.base.Throwables;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created Mario Estrella on 3/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class BlenderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderUtils.class);
    private static List<BlenderZip> blenderZipList = null;

    private static void getList() {

        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getResult("https://gist.githubusercontent.com/marioestrella/def9d852b3298008ae16040bbbabc524/raw/");
        if (data == null || data.isEmpty()) {
            LOG.debug("Trying mirror");
            data = getJSONData.getResult("https://gitlab.com/snippets/1656456/raw");
            if (data == null || data.isEmpty()) {
                LOG.debug("Unable to retrieve blenderdownload.json from internet, using local version instead.");
                data = getJSONData.getLocalResult("blenderdownload.json");
            }
        }
        LOG.debug("Retrieved JSON: \n" + data.substring(0, 100) + "...");
        if (data != null || !data.isEmpty()) {
            blenderZipList = new LinkedList<>();


            try {

                JSONObject jsonData = new JSONObject(data);
                JSONArray downloadArray = jsonData.getJSONArray("blenderdownload");

                for (int i = 0; i < downloadArray.length(); i++) {
                    List<String> macOSMirrors = new ArrayList<>();
                    List<String> windows64Mirrors = new ArrayList<>();
                    List<String> windows32Mirrors = new ArrayList<>();
                    List<String> linux64Mirrors = new ArrayList<>();
                    List<String> linux32Mirrors = new ArrayList<>();
                    JSONObject blenderBinary = downloadArray.getJSONObject(i);
                    String version = blenderBinary.getString("version");

                    JSONArray macOSArray = blenderBinary.getJSONArray("macos");
                    for (int j = 0; j < macOSArray.length(); j++) {
                        macOSMirrors.add(macOSArray.getString(j));
                    }

                    JSONArray windows64Array = blenderBinary.getJSONArray("windows64");
                    for (int j = 0; j < windows64Array.length(); j++) {
                        windows64Mirrors.add(windows64Array.getString(j));
                    }

                    JSONArray windows32Array = blenderBinary.getJSONArray("windows32");
                    for (int j = 0; j < windows32Array.length(); j++) {
                        windows32Mirrors.add(windows32Array.getString(j));
                    }

                    JSONArray linux64Array = blenderBinary.getJSONArray("linux64");
                    for (int j = 0; j < linux64Array.length(); j++) {
                        linux64Mirrors.add(linux64Array.getString(j));
                    }

                    JSONArray linux32Array = blenderBinary.getJSONArray("linux32");
                    for (int j = 0; j < linux64Array.length(); j++) {
                        linux32Mirrors.add(linux32Array.getString(j));
                    }

                    String md5MacOs = blenderBinary.getString("macos_md5");
                    String md5Windows64 = blenderBinary.getString("windows64_md5");
                    String md5Windows32 = blenderBinary.getString("windows32_md5");
                    String md5Linux64 = blenderBinary.getString("linux64_md5");
                    String md5Linux32 = blenderBinary.getString("linux32_md5");
                    BlenderZip blenderZip = new BlenderZip(version, windows32Mirrors, windows64Mirrors, macOSMirrors, linux32Mirrors, linux64Mirrors, md5MacOs, md5Windows64, md5Windows32, md5Linux32, md5Linux64);
                    blenderZipList.add(blenderZip);
                }
            } catch (JSONException jsonEx) {
                LOG.error("Error processing JSON data" + jsonEx.getMessage());
                LOG.error(Throwables.getStackTraceAsString(jsonEx));
            }
        }
    }

    public static List<BlenderZip> listBinaries() {
        if (blenderZipList == null) {
            getList();
        }
        return blenderZipList;
    }

    public static List<String> listVersions() {
        if (blenderZipList == null) {
            getList();
        }
        List<String> versions = new LinkedList<>();
        for (BlenderZip blenderZip : blenderZipList) {
            versions.add(blenderZip.getBlenderVersion());
        }
        return versions;
    }

    public static void refresh() {
        blenderZipList = null;
    }
}
