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
package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.GitPropertyKey;
import com.dryadandnaiad.sethlans.enums.StringKey;
import java.io.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public class SethlansUtils {
    
    private static final Logger logger = LogManager.getLogger(SethlansUtils.class);
    
    public static String getString(StringKey sentEnum) {
        String newKey = sentEnum.toString();
        String value = null;
        try {
            Properties properties;
            InputStream input = new Resources("properties/strings.xml").getResource();
            properties = new Properties();
            properties.loadFromXML(input);
                        
            value = properties.getProperty(newKey);
             
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            
        } catch (IOException e) {
            logger.error(e.getMessage());
            
        }
        return value;
    }
    
    public static String getGitProperties(GitPropertyKey sentEnum) {
        return null;
    }
    
    public static String updaterTimeStamp(){
        Date currentDate = GregorianCalendar.getInstance().getTime();
        return String.format("Updated: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", currentDate);
    }
    
    public static String enumToString(Enum value){
        return value.toString().toLowerCase();
    }
    
    
}