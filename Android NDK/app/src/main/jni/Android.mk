LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := systeminfo
LOCAL_SRC_FILES := systeminfo.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
