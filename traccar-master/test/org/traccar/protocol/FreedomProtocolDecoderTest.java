package org.traccar.protocol;

import org.traccar.helper.TestDataManager;
import static org.traccar.helper.DecoderVerifier.verify;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class FreedomProtocolDecoderTest {

    @Test
    public void testDecode() throws Exception {

        FreedomProtocolDecoder decoder = new FreedomProtocolDecoder(null);
        decoder.setDataManager(new TestDataManager());

        verify(decoder.decode(null, null,
                "IMEI,353358011714362,2014/05/22, 20:49:32, N, Lat:4725.9624, E, Lon:01912.5483, Spd:5.05"));
        
    }

}
