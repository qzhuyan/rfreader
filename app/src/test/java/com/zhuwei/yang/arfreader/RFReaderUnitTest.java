package com.zhuwei.yang.arfreader;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class RFReaderUnitTest {

    static byte[] testdata_resp = {
            0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x06, 0x00, 0x00, (byte) 0xbd, 0x71, 0x4e,
            0x67, (byte) 0xe3, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    static byte[] testdata_resp_nocard = {
            0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x02, 0x01, (byte) 0x83, (byte) 0x80, 0x03, (byte) 0xBB, //0xBB is dirty
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    // data with bad checksum
    static byte[] testdata_resp_inval = {
            0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x06, 0x00, 0x00, (byte) 0xbd, 0x71, 0x4e,
            0x67, (byte) 0xff, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    @Test
    public void encode_isCorrect() throws Exception {
        byte[] expected = new byte[]{(byte) 0xaa, 0x00, 0x03, 0x25, 0x26, 0x00, 0x00, (byte) 0xbb};
        byte[] res = RFReader.encode(new byte[]{RFReaderConstant.MF_GET_SNR, RFReaderConstant.MF_GET_SNR_MODE_IDLE, 0x00});
        assertArrayEquals(expected, res);
    }

    @Test
    public void checksum_isCorrect() throws Exception {
        byte[] testdata = {0x00, 0x03, 0x25, 0x26, 0x00};
        assertEquals(0x00, RFReader.checksum(testdata));
    }

    @Test
    public void commandBuffer_isCorrect() throws Exception {
        byte[] testdata = {(byte) 0xaa, 0x00, 0x03, 0x25, 0x26, 0x00, 0x00, (byte) 0xbb};
        byte[] expected = {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00,
                (byte) 0xaa, 0x00, 0x03, 0x25, 0x26, 0x00, 0x00, (byte) 0xbb,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00

        };
        assertArrayEquals(expected, RFReader.commandBuffer(testdata));
    }

    @Test
    public void validate_data_isCorrect() throws Exception {

        assertTrue(RFReader.validate_data(testdata_resp));
    }

    @Test
    public void validate_data_isCorrect2() throws Exception {
        assertTrue(RFReader.validate_data(testdata_resp_nocard));
    }

    @Test
    public void parseData_isCorrect() throws Exception {
        RFReader.RFReaderResp res = RFReader.parseData(testdata_resp);
        assertEquals(0, res.errno);
        assertArrayEquals(new byte[]{0x00, (byte) 0xBD, 0x71, (byte) 0x4E, (byte) 0x67}, res.data);
    }

    @Test
    public void parseData_can_handle_error_resp() throws Exception {
        RFReader.RFReaderResp res = RFReader.parseData(testdata_resp_nocard);
        assertEquals(1, res.errno);
        assertArrayEquals(new byte[]{(byte) 0x83}, res.data);
    }

    @Test
    public void parseData_can_handle_inval_data() throws Exception {
        RFReader.RFReaderResp res = RFReader.parseData(testdata_resp_inval);
        assertEquals(RFReaderConstant.ERROR_INVALID_DATA, res.errno);
        assertArrayEquals(null, res.data);
    }

    @Test
    public void card_sernum() throws Exception {
        RFReader.RFReaderResp res = RFReader.parseData(testdata_resp);
        assertEquals("BD714E67", res.card_sernum());
    }


}