LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := collisiondetector
LOCAL_SRC_FILES := CollisionDetector.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
