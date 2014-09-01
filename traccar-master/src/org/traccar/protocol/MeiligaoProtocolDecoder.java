/*
 * Copyright 2012 - 2014 Anton Tananaev (anton.tananaev@gmail.com)
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

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.traccar.BaseProtocolDecoder;
import org.traccar.ServerManager;
import org.traccar.helper.Crc;
import org.traccar.helper.Log;
import org.traccar.model.ExtendedInfoFormatter;
import org.traccar.model.Position;

public class MeiligaoProtocolDecoder extends BaseProtocolDecoder {

    public MeiligaoProtocolDecoder(ServerManager serverManager) {
        super(serverManager);
    }

    private static final Pattern pattern = Pattern.compile(
            "(\\d{2})(\\d{2})(\\d{2})\\.?(\\d+)?," + // Time (HHMMSS.SSS)
            "([AV])," +                         // Validity
            "(\\d+)(\\d{2}\\.\\d+)," +          // Latitude (DDMM.MMMM)
            "([NS])," +
            "(\\d+)(\\d{2}\\.\\d+)," +          // Longitude (DDDMM.MMMM)
            "([EW])," +
            "(\\d+\\.?\\d*)?," +                // Speed
            "(\\d+\\.?\\d*)?," +                // Course
            "(\\d{2})(\\d{2})(\\d{2})" +        // Date (DDMMYY)
            "(?:[^\\|]*\\|(\\d+\\.\\d+)\\|" +   // Dilution of precision
            "(\\d+\\.?\\d*)\\|)?" +             // Altitude
            "([0-9a-fA-F]+)?" +                 // State
            "(?:\\|([0-9a-fA-F]+),([0-9a-fA-F]+))?" + // ADC
            "(?:,([0-9a-fA-F]+),([0-9a-fA-F]+)" +
            ",([0-9a-fA-F]+),([0-9a-fA-F]+)" +
            ",([0-9a-fA-F]+),([0-9a-fA-F]+))?" +
            "(?:\\|([0-9a-fA-F]+))?" +          // Cell
            "(?:\\|([0-9a-fA-F]+))?" +          // Signal
            "(?:\\|([0-9a-fA-F]+))?" +          // Milage
            ".*");
    
    private static final int MSG_HEARTBEAT = 0x0001;
    private static final int MSG_SERVER = 0x0002;
    private static final int MSG_LOGIN = 0x5000;
    private static final int MSG_LOGIN_RESPONSE = 0x5000;
    
    private static final int MSG_POSITION = 0x9955;
    private static final int MSG_POSITION_LOGGED = 0x9016;
    private static final int MSG_ALARM = 0x9999;
    
    private String getImei(ChannelBuffer buf) {
        String id = "";

        for (int i = 0; i < 7; i++) {
            int b = buf.readUnsignedByte();

            // First digit
            int d1 = (b & 0xf0) >> 4;
            if (d1 == 0xf) break;
            id += d1;

            // Second digit
            int d2 = (b & 0x0f);
            if (d2 == 0xf) break;
            id += d2;
        }

        if (id.length() == 14) {
            id += Crc.luhnChecksum(id); // IMEI checksum
        }
        return id;
    }
    
    private static void sendResponse(
            Channel channel, ChannelBuffer id, int type, ChannelBuffer msg) {
        
        if (channel != null) {
            ChannelBuffer buf = ChannelBuffers.buffer(
                    2 + 2 + id.readableBytes() + 2 + msg.readableBytes() + 2 + 2);
            
            buf.writeByte('@');
            buf.writeByte('@');
            buf.writeShort(buf.capacity());
            buf.writeBytes(id);
            buf.writeShort(type);
            buf.writeBytes(msg);
            buf.writeShort(Crc.crc16X25Ccitt(buf.toByteBuffer()));
            buf.writeByte('\r');
            buf.writeByte('\n');

            channel.write(buf);
        }
    }
    
    private String getMeiligaoServer(Channel channel) {
        
        if (getServerManager() != null &&
            getServerManager().getProperties().containsKey("meiligao.server")) {
            return getServerManager().getProperties().getProperty("meiligao.server");
        } else {
            InetSocketAddress address = (InetSocketAddress) channel.getLocalAddress();
            return address.getAddress().getHostAddress() + ":" + address.getPort();
        }
    }

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {
        
        ChannelBuffer buf = (ChannelBuffer) msg;
        buf.skipBytes(2); // header
        buf.readShort(); // length
        ChannelBuffer id = buf.readBytes(7);
        int command = buf.readUnsignedShort();
        ChannelBuffer response;
        
        switch (command) {
            case MSG_LOGIN:
                response = ChannelBuffers.wrappedBuffer(new byte[] {0x01});
                sendResponse(channel, id, MSG_LOGIN_RESPONSE, response);
                return null;
            case MSG_HEARTBEAT:
                response = ChannelBuffers.wrappedBuffer(new byte[] {0x01});
                sendResponse(channel, id, MSG_HEARTBEAT, response);
                return null;
            case MSG_SERVER:
                response = ChannelBuffers.copiedBuffer(
                        getMeiligaoServer(channel), Charset.defaultCharset());
                sendResponse(channel, id, MSG_SERVER, response);
                return null;
            case MSG_POSITION:
            case MSG_POSITION_LOGGED:
            case MSG_ALARM:
                break;
            default:
                return null;
        }

        // Create new position
        Position position = new Position();
        ExtendedInfoFormatter extendedInfo = new ExtendedInfoFormatter("meiligao");

        // Custom data
        if (command == MSG_ALARM) {
            extendedInfo.set("alarm", buf.readUnsignedByte());
        } else if (command == MSG_POSITION_LOGGED) {
            buf.skipBytes(6);
        }

        // Get device by id
        String imei = getImei(id);
        try {
            position.setDeviceId(getDataManager().getDeviceByImei(imei).getId());
        } catch(Exception error) {
            Log.warning("Unknown device - " + imei);
            return null;
        }

        // Parse message
        String sentence = buf.toString(
                buf.readerIndex(), buf.readableBytes() - 4, Charset.defaultCharset());
        Matcher parser = pattern.matcher(sentence);
        if (!parser.matches()) {
            return null;
        }

        Integer index = 1;

        // Time
        Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        time.clear();
        time.set(Calendar.HOUR_OF_DAY, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.MINUTE, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.SECOND, Integer.valueOf(parser.group(index++)));
        String mseconds = parser.group(index++);
        if (mseconds != null) {
            time.set(Calendar.MILLISECOND, Integer.valueOf(mseconds));
        }

        // Validity
        position.setValid(parser.group(index++).compareTo("A") == 0);

        // Latitude
        Double latitude = Double.valueOf(parser.group(index++));
        latitude += Double.valueOf(parser.group(index++)) / 60;
        if (parser.group(index++).compareTo("S") == 0) latitude = -latitude;
        position.setLatitude(latitude);

        // Longitude
        Double longitude = Double.valueOf(parser.group(index++));
        longitude += Double.valueOf(parser.group(index++)) / 60;
        if (parser.group(index++).compareTo("W") == 0) longitude = -longitude;
        position.setLongitude(longitude);

        // Speed
        String speed = parser.group(index++);
        if (speed != null) {
            position.setSpeed(Double.valueOf(speed));
        } else {
            position.setSpeed(0.0);
        }

        // Course
        String course = parser.group(index++);
        if (course != null) {
            position.setCourse(Double.valueOf(course));
        } else {
            position.setCourse(0.0);
        }

        // Date
        time.set(Calendar.DAY_OF_MONTH, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.MONTH, Integer.valueOf(parser.group(index++)) - 1);
        time.set(Calendar.YEAR, 2000 + Integer.valueOf(parser.group(index++)));
        position.setTime(time.getTime());

        // Dilution of precision
        extendedInfo.set("hdop", parser.group(index++));

        // Altitude
        String altitude = parser.group(index++);
        if (altitude != null) {
            position.setAltitude(Double.valueOf(altitude));
        } else {
            position.setAltitude(0.0);
        }

        // State
        String state = parser.group(index++);
        if (state != null) {
            extendedInfo.set("state", state);
        }

        // ADC
        for (int i = 1; i <= 8; i++) {
            String adc = parser.group(index++);
            if (adc != null) {
                extendedInfo.set("adc" + i, Integer.parseInt(adc, 16));
            }
        }

        // Cell identifier
        String cell = parser.group(index++);
        if (cell != null) {
            extendedInfo.set("cell", cell);
        }

        // GSM signal
        String gsm = parser.group(index++);
        if (gsm != null) {
            extendedInfo.set("gsm", Integer.parseInt(gsm, 16));
        }

        // Milage
        String milage = parser.group(index++);
        if (milage != null) {
            extendedInfo.set("milage", Integer.parseInt(milage, 16));
        }

        // Extended info
        position.setExtendedInfo(extendedInfo.toString());

        return position;
    }

}
