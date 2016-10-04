package com.zhuwei.yang.arfreader;

public class RFReaderConstant {
    public static final byte START_BYTE = (byte) 0xaa;
    public static final byte END_BYTE = (byte) 0xbb;
    public static final byte MF_GET_SNR = (byte) 0x25;
    public static final byte MF_GET_SNR_MODE_IDLE = (byte) 0x26;

    public static final byte MF_FAIL = (byte) 0x01;
    public static final byte MF_SUCCESS = (byte) 0x00;

    public static final int ERROR_NODEV = 201;
    public static final int ERROR_SEND_REPORT = 202;
    public static final int ERROR_GET_REPORT = 203;
    public static final int ERROR_INVALID_DATA = 204;

    public static final int READER_ERROR_NO_CARD = 0x83;
    public static final int READER_ERROR_TIMEOUT = 0x82;


    public static String error_info(int errorno) {
        String info;
        errorno &= 0xFF; // to unsigned int
        switch (errorno) {
            case MF_SUCCESS:
                info = "no error";
                break;
            case ERROR_NODEV:
                info = "No device";
                break;
            case ERROR_SEND_REPORT:
                info = "Failed to send report";
                break;
            case ERROR_GET_REPORT:
                info = "Failed to get report";
                break;
            case ERROR_INVALID_DATA:
                info = "invalid data from reader";
                break;
            case READER_ERROR_NO_CARD:
                info = "card not found";
                break;
            case READER_ERROR_TIMEOUT:
                info = "reader timeout";
                break;
            default:
                info = "Unknown error code: " + errorno;
        }
        return info;
    }
}
