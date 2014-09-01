/*
 * Copyright 2014 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.traccar.BaseProtocolDecoder;
import org.traccar.ServerManager;
import org.traccar.helper.Log;
import org.traccar.model.ExtendedInfoFormatter;
import org.traccar.model.Position;

public class FreedomProtocolDecoder extends BaseProtocolDecoder {

    public FreedomProtocolDecoder(ServerManager serverManager) {
        super(serverManager);
    }

    private static final Pattern pattern = Pattern.compile(
            "IMEI," +
            "(\\d+)," +                           // IMEI
            "(\\d{4}).(\\d{2}).(\\d{2}), " +      // Date
            "(\\d{2}):(\\d{2}):(\\d{2}), " +      // Time
            "([NS]), Lat:(\\d{2})(\\d+.\\d+), " + // Latitude
            "([EW]), Lon:(\\d{3})(\\d+.\\d+), " + // Longitude
            "Spd:(\\d+.\\d+)" +                   // Speed
            ".*");

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {

        // Parse message
        Matcher parser = pattern.matcher((String) msg);
        if (!parser.matches()) {
            return null;
        }

        // Create new position
        Position position = new Position();
        ExtendedInfoFormatter extendedInfo = new ExtendedInfoFormatter("freedom");
        Integer index = 1;

        // Identification
        String imei = parser.group(index++);
        try {
            position.setDeviceId(getDataManager().getDeviceByImei(imei).getId());
        } catch(Exception error) {
            Log.warning("Unknown device - " + imei);
            return null;
        }
        
        // Validity
        position.setValid(true);

        // Time
        Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        time.clear();
        time.set(Calendar.YEAR, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.MONTH, Integer.valueOf(parser.group(index++)) - 1);
        time.set(Calendar.DAY_OF_MONTH, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.HOUR_OF_DAY, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.MINUTE, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.SECOND, Integer.valueOf(parser.group(index++)));
        position.setTime(time.getTime());

        // Latitude
        String hemisphere = parser.group(index++);
        Double latitude = Double.valueOf(parser.group(index++));
        latitude += Double.valueOf(parser.group(index++)) / 60;
        if (hemisphere.compareTo("S") == 0) latitude = -latitude;
        position.setLatitude(latitude);

        // Longitude
        hemisphere = parser.group(index++);
        Double longitude = Double.valueOf(parser.group(index++));
        longitude += Double.valueOf(parser.group(index++)) / 60;
        if (hemisphere.compareTo("W") == 0) longitude = -longitude;
        position.setLongitude(longitude);
        
        // Altitude
        position.setAltitude(0.0);

        // Speed
        position.setSpeed(Double.valueOf(parser.group(index++)));

        // Course
        position.setCourse(0.0);

        position.setExtendedInfo(extendedInfo.toString());
        return position;
    }

}
