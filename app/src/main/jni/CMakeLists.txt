cmake_minimum_required(VERSION 3.4.1)
add_library(native-usb
            SHARED
            native-usb.c )

target_link_libraries(native-usb log android)
