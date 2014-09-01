package org.traccar.protocol;

import org.traccar.helper.TestDataManager;
import static org.traccar.helper.DecoderVerifier.verify;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class MiniFinderProtocolDecoderTest {

    @Test
    public void testDecode() throws Exception {

        MiniFinderProtocolDecoder decoder = new MiniFinderProtocolDecoder(null);
        decoder.setDataManager(new TestDataManager());

        assertNull(decoder.decode(null, null, "!1,860719020212696"));

        verify(decoder.decode(null, null,
                "!D,22/2/14,13:40:58,56.899601,14.811541,0,0,1,176.0,98,5,16,0"));

        verify(decoder.decode(null, null,
                "!D,22/2/14,13:47:51,56.899517,14.811665,0,0,b0001,179.3,97,5,16,0"));

    }

}
