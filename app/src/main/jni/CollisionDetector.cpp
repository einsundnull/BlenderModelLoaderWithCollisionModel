#include <jni.h>
#include <iostream>
#include <cmath>
#include <cpu-features.h>
#include "com_notorein_threedmodeling_CollisionDetector.h"

class Vector3D {
public:
    float x, y, z;

    Vector3D(float x, float y, float z) : x(x), y(y), z(z) {}

    float magnitude() const {
        return std::sqrt(x * x + y * y + z * z);
    }

    Vector3D subtract(const Vector3D& other) const {
        return Vector3D(x - other.x, y - other.y, z - other.z);
    }
};

class CollisionDetector {
public:
    static bool detectCollision(const Vector3D& pos1, float size1, const Vector3D& pos2, float size2) {
        Vector3D distanceVector = pos1.subtract(pos2);
        float distance = distanceVector.magnitude();
        return distance < (size1 + size2);
    }

    static bool hasNeonSupport() {
        return android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM &&
               (android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON) != 0;
    }
};

JNIEXPORT jboolean JNICALL Java_com_notorein_threedmodeling_CollisionDetector_detectCollision
        (JNIEnv* env, jobject /* this */, jfloat x1, jfloat y1, jfloat z1, jfloat size1, jfloat x2, jfloat y2, jfloat z2, jfloat size2) {
    Vector3D pos1(x1, y1, z1);
    Vector3D pos2(x2, y2, z2);
    return CollisionDetector::detectCollision(pos1, size1, pos2, size2);
}

JNIEXPORT jboolean JNICALL Java_com_notorein_threedmodeling_CollisionDetector_hasNeonSupport
        (JNIEnv* env, jobject /* this */) {
    return CollisionDetector::hasNeonSupport();
}
