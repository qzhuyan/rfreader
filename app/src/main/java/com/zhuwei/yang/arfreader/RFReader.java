package com.zhuwei.yang.arfreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static android.content.ContentValues.TAG;

public class RFReader {
    private static byte[] CMD_GET_SNRNUM = {RFReaderConstant.MF_GET_SNR, RFReaderConstant.MF_GET_SNR_MODE_IDLE, 0x00};
    private static int DataFixedLength = 256;
    private static int DataOffset = 8;
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";


    public static final BroadcastReceiver UsbPermitReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {

                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };


    public static class RFReaderResp {
        public int errno = 0;
        public byte[] data = null;

        RFReaderResp(byte[] d) {
            errno = d[0];
            data = Arrays.copyOfRange(d, 1, d.length - 1);
        }

        RFReaderResp(int err) {
            errno = err;
            data = null;
        }

        public String card_sernum() {
            // cut the first byte which is the station id.
            return bytesToHex(Arrays.copyOfRange(data, 1, data.length));
        }

        public byte error_code() {
            if (errno == RFReaderConstant.MF_FAIL)
                return data[0];
            else
                return 0;
        }
    }

    public static final byte[] encode(byte[] cmd) {
        //format is
        // [ start, addr, len, cmd, cmd_args...,csum,end]
        byte[] packet = new byte[8];
        int len = cmd.length;

        packet[0] = RFReaderConstant.START_BYTE;
        packet[1] = (byte) 0x00;
        packet[2] = (byte) len;
        System.arraycopy(cmd, 0, packet, 3, cmd.length);

        byte csum = checksum(Arrays.copyOfRange(packet, 1, cmd.length + 2));
        packet[cmd.length + 3] = csum;
        packet[cmd.length + 4] = (RFReaderConstant.END_BYTE);

        return packet;
    }

    public static byte checksum(byte[] data) {
        byte Seed = data[0];
        for (int d : Arrays.copyOfRange(data, 1, data.length)) {
            Seed ^= d;
        }
        return (byte) Seed;
    }

    public static byte[] commandBuffer(byte[] buff) {
        byte[] data = new byte[DataFixedLength];
        Arrays.fill(data, (byte) 0x00);
        data[6] = (byte) buff.length;
        System.arraycopy(buff, 0, data, 8, buff.length);
        return data;
    }

    public static UsbDevice get_device_with_vid_pid(UsbManager manager, int vid, int pid) {
        UsbDevice device = null;
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            Log.i(TAG, "vender id " + device.getVendorId());

            if (device.getVendorId() == vid && device.getProductId() == pid) {
                Log.i(TAG, "find target dev");
                break;
            }
        }

        return device;

    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static RFReaderResp read_card_snr(UsbManager manager) {
        UsbDevice device = get_device_with_vid_pid(manager, 65535, 53);
        int len;

        if (null == device)
            return new RFReaderResp(RFReaderConstant.ERROR_NODEV);

        UsbInterface intf = device.getInterface(0);

        UsbDeviceConnection m_connection = manager.openDevice(device);

//      for android 5.0 +
//        if (! m_connection.setConfiguration(device.getConfiguration(0))) {
//            Log.i(TAG, "failed to set usb config");
//        }

        if (!native_set_configuration(m_connection.getFileDescriptor(), 1)) {
            Log.i(TAG, "failed to set usb config with jni");
        }


        if (!m_connection.claimInterface(intf, false))
            Log.i(TAG, "failed to claim usb interface");

        byte[] data = commandBuffer(encode(new byte[]{RFReaderConstant.MF_GET_SNR, RFReaderConstant.MF_GET_SNR_MODE_IDLE, 0x00}));
        Log.i(TAG, "send data" + bytesToHex(data));

        len = m_connection.controlTransfer(0x21, //RequestType
                0x09, //Request
                0x0301, //value
                0, //index
                data, // data
                DataFixedLength, // length
                500 //timeout
        );

        if (!m_connection.releaseInterface(intf))
            Log.i(TAG, "failed to release usb interface");

        if (len < 0) {
            Log.i(TAG, "set report failed error:" + len);
            m_connection.close();
            return new RFReaderResp(RFReaderConstant.ERROR_SEND_REPORT);
        } else {
            Log.i(TAG, "control transfered len:" + len);
        }

        if (!m_connection.claimInterface(intf, true))
            Log.i(TAG, "failed to claim usb interface");

        m_connection.controlTransfer(0xa1, //0x01 |(0x01 << 5) | 0x80,
                0x01,
                0x0302,
                0,
                data,
                DataFixedLength, 500);

        if (!m_connection.releaseInterface(intf))
            Log.i(TAG, "failed to release usb interface");

        if (len < 0) {
            Log.i(TAG, "get report failed");
            m_connection.close();
            return new RFReaderResp(RFReaderConstant.ERROR_GET_REPORT);
        } else {
            Log.i(TAG, "control transfer len:" + len);
        }
        Log.i(TAG, "get report data:" + bytesToHex(data));

        m_connection.close();

        return parseData(data);
    }

    public static RFReaderResp parseData(byte[] buff) {

        if (validate_data(buff)) {
            return new RFReaderResp(Arrays.copyOfRange(buff, DataOffset + 3, DataOffset + 3 + buff[DataOffset + 2] + 1));
        } else {
            return new RFReaderResp(RFReaderConstant.ERROR_INVALID_DATA);
        }

    }

    public static boolean validate_data(byte[] buffer) {
        int len = buffer[DataOffset + 2];
        byte bcc = buffer[DataOffset + 3 + len];
        return buffer[DataOffset] == 0x02  //data start
                && buffer[DataOffset + 1] == 0x00 //Station ID 0
                && buffer[DataOffset + 2 + len + 2] == 0x03 // data end
                && (bcc == checksum(Arrays.copyOfRange(buffer, DataOffset + 1, DataOffset + 1 + 2 + len)));

    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static native boolean native_set_configuration(int fd, int config);

    static {
        System.loadLibrary("native-usb");
    }

}
