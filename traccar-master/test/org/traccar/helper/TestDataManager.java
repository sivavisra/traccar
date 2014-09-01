package org.traccar.helper;

import java.util.List;
import java.util.regex.Matcher;

import org.traccar.model.DataManager;
import org.traccar.model.Device;
import org.traccar.model.Position;
import org.traccar.model.Track;

public class TestDataManager implements DataManager {
    
    public List getDevices() {
        return null;
    }
    
    public Device getDeviceByImei(String imei) {
        Device device = new Device();
        device.setId(new Long(1));
        device.setImei("123456789012345");
        return device;
    }
    
    public Long addPosition(Position position) {
        return null;
    }
    
    public void updateLatestPosition(Long deviceId, Long positionId) throws Exception {
    }
    
	public void addTrack(Track track) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
