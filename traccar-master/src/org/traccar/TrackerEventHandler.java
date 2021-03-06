/*
 * Copyright 2012 Anton Tananaev (anton.tananaev@gmail.com)
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
package org.traccar;

import java.util.List;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.traccar.helper.Log;
import org.traccar.model.DataManager;
import org.traccar.model.Position;
import org.traccar.model.Track;

/**
 * Tracker message handler
 */
@ChannelHandler.Sharable
public class TrackerEventHandler extends IdleStateAwareChannelHandler {

    /**
     * Data manager
     */
    private DataManager dataManager;

    TrackerEventHandler(DataManager newDataManager) {
        super();
        dataManager = newDataManager;
    }

    private void processSinglePosition(Position position) {
        if (position == null) {
            Log.info("processSinglePosition null message");
        } else {
            StringBuilder s = new StringBuilder();
            s.append("device: ").append(position.getDeviceId()).append(", ");
            s.append("time: ").append(position.getTime()).append(", ");
            s.append("lat: ").append(position.getLatitude()).append(", ");
            s.append("lon: ").append(position.getLongitude());
            Log.info(s.toString());
        }

        // Write position to database
        try {
            Long id = dataManager.addPosition(position);
            if (id != null) {
                dataManager.updateLatestPosition(position.getDeviceId(), id);
            }
        } catch (Exception error) {
            Log.warning(error);
        }
    }
    
    private void processSingleTrack(Track track) {
        if (track == null) {
            Log.info("processSingleTrack null message");
        } else {
            StringBuilder s = new StringBuilder();
            s.append("Tracker ID: ").append(track.getTrackerID()).append(", ");
            s.append("time: ").append(track.getTime()).append(", ");
            s.append("lat: ").append(track.getLatitude()).append(", ");
            s.append("lon: ").append(track.getLongitude());
            Log.info(s.toString());
        }

        // Write track to database
        try {
            
        	dataManager.addTrack(track);
            
        } catch (Exception error) {
            Log.warning(error);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    	if (e.getMessage() instanceof Track) {
            processSingleTrack((Track) e.getMessage());
        }
    	else if (e.getMessage() instanceof Position) {
            processSinglePosition((Position) e.getMessage());
        } else if (e.getMessage() instanceof List) {
            List<Position> positions = (List<Position>) e.getMessage();
            for (Position position : positions) {
                processSinglePosition(position);
            }
        }
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Log.info("Closing connection by disconnect");
        e.getChannel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Log.info("Closing connection by exception");
        e.getChannel().close();
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
        Log.info("Closing connection by timeout");
        e.getChannel().close();
    }

}
