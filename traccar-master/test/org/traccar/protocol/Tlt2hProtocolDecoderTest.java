package org.traccar.protocol;

import org.traccar.helper.TestDataManager;
import static org.traccar.helper.DecoderVerifier.verify;
import org.junit.Test;

public class Tlt2hProtocolDecoderTest {

    @Test
    public void testDecode() throws Exception {

        Tlt2hProtocolDecoder decoder = new Tlt2hProtocolDecoder(null);
        decoder.setDataManager(new TestDataManager());

        verify(decoder.decode(null, null,
                "#357671030946351#V500#0000#AUTO#1\r\n" +
                "#$GPRMC,223835.000,A,0615.3545,S,10708.5779,E,14.62,97.41,070313,,,D*70\r\n"));

        verify(decoder.decode(null, null,
                "\r\n#357671030946351#V500#0000#AUTO#1\r\n" +
                "#$GPRMC,223835.000,A,0615.3545,S,10708.5779,E,14.62,97.41,070313,,,D*70\r\n"));
        
        verify(decoder.decode(null, null,
                "#357671030938911#V500#0000#AUTOSTOP#1\r\n" +
                "#00b34d3c$GPRMC,140026.000,A,2623.6452,S,02828.8990,E,0.00,65.44,130213,,,A*4B\r\n"));

        verify(decoder.decode(null, null,
                "#123456789000001#V3338#0000#SMS#3\r\n" +
                "#25ee0dff$GPRMC,083945.180,A,2233.4249,N,11406.0046,E,0.00,315.00,251207,,,A*6E\r\n" +
                "#25ee0dff$GPRMC,083950.180,A,2233.4249,N,11406.0046,E,0.00,315.00,251207,,,A*6E\r\n" +
                "#25ee0dff$GPRMC,083955.180,A,2233.4249,N,11406.0046,E,0.00,315.00,251207,,,A*6E"));
        
        verify(decoder.decode(null, null,
                "#353686009063310#353686009063310#0000#AUTO#2\r\n" +
                "#239757a9$GPRMC,150252.001,A,2326.6856,S,4631.8154,W,,,260513,,,A*52\r\n" +
                "#239757a9$GPRMC,150322.001,A,2326.6854,S,4631.8157,W,,,260513,,,A*55"));
        
        verify(decoder.decode(null, null,
                "#357671031289215#V600#0000#AUTOLOW#1\r\n" +
                "#00735e1c$GPRMC,115647.000,A,5553.6524,N,02632.3128,E,0.00,0.0,130614,0.0,W,A*28"));

    }

}
