#include <jni.h>
#include <linux/usbdevice_fs.h>
#include <sys/ioctl.h>
#include <android/log.h>
#include <errno.h>


int usb_device_set_configuration(int fd, int configuration) {
    // should always success
    ioctl(fd, USBDEVFS_RESET, 0);

    return ioctl(fd, USBDEVFS_SETCONFIGURATION, &configuration);
}

JNIEXPORT jobject JNICALL
Java_com_zhuwei_yang_arfreader_RFReader_native_1set_1configuration(JNIEnv *env, jobject instance,
                                                                   jint fd, jint config) {

    if (0 == usb_device_set_configuration(fd, config)) {
        return (jobject) JNI_TRUE;
    }
    else {
        __android_log_write(ANDROID_LOG_ERROR, "rfreader.jni", "error code: " + errno);
        return (jobject) JNI_FALSE;
    }

}