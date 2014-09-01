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

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.traccar.BaseProtocolDecoder;
import org.traccar.ServerManager;
import org.traccar.model.Track;
import org.traccar.util.TraccarUtil;

public class Tk103ProtocolDecoder extends BaseProtocolDecoder {

    public Tk103ProtocolDecoder(ServerManager serverManager) {
        super(serverManager);
    }
    
    private static final  Pattern pattern = Pattern.compile("\\((.{11}).(.{4})(.{14}).(.{6})(.)(.{4}\\..{4}.)(.{5}\\..{4}.)(.{3}\\..)(.{6})(.{6})(.{8})(.)(.{8})\\).*");
    
    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {
    	
    	String dataStr = (String)msg + ")";
    	
    	//String dataStr = "(07867085729.BP0500007867085729.140827A1631.4287N08046.8912E035.716122856.19000000000L005A81CC)";
    	Matcher matcher = Tk103ProtocolDecoder.pattern.matcher(dataStr);
    	   
    	if ((matcher.matches()) && (matcher.groupCount() == 13)) {
			Track track = new Track();
			track.setTrackerID(matcher.group(1));
			track.setDeviceIMEI(matcher.group(3));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmsszzz");
    		java.util.Date dateObj = sdf.parse(matcher.group(4) + matcher.group(9) + "UTC");
    		
    		track.setDate(new java.sql.Date(dateObj.getTime()));
    		track.setTime(new Time(dateObj.getTime()));
    		track.setStatus(matcher.group(5));
    		track.setLatitude(TraccarUtil.convertToDegrees(matcher.group(6)));
    		track.setLongitude(TraccarUtil.convertToDegrees(matcher.group(7)));
			track.setSpeed(new Double(matcher.group(8)).doubleValue());
			track.setDirection(new Double(matcher.group(10)).doubleValue());
			
			String deviceStatus = matcher.group(11);
			
			track.setBattery(Integer.parseInt(String.valueOf(deviceStatus.charAt(0))));
			
			String statusTEMP = Integer.toBinaryString(Integer.parseInt(String.valueOf(deviceStatus.charAt(2)), 16));
			
			track.setSos(Integer.parseInt(String.valueOf((statusTEMP.length() > 2 ? statusTEMP.charAt(statusTEMP.length()-2) : "0"))));
			track.setAc(Integer.parseInt(String.valueOf(deviceStatus.charAt(1))));
			track.setFuel(0.0d);
			track.setDeviceStatus(deviceStatus);
			track.setMileage(Double.valueOf(String.valueOf(Long.parseLong(matcher.group(13), 16))).doubleValue());
			
			return track;			
		}else {			
			return null;
		}    	
    }
    
    public static void main(String [] args)
    {

         try {
			new Tk103ProtocolDecoder(null).decode(null, null, "(07867085729.BP0500007867085729.140827A1631.4287N08046.8912E035.716122856.19000000000L005A81CC)");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
    }

}
