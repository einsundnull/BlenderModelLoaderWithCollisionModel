#include <jni.h>
#include <cmath>
#include "Vector3D.h"
#include "com_notorein_threedmodeling_utils_Vector3D.h"

extern "C" {

JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_magnitude
        (JNIEnv* env, jobject obj) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double x = env->GetDoubleField(obj, xField);
    double y = env->GetDoubleField(obj, yField);
    double z = env->GetDoubleField(obj, zField);

    Vector3D vector(x, y, z);
    return vector.magnitude();
}

JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_distanceTo
        (JNIEnv* env, jobject obj, jobject other) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double x = env->GetDoubleField(obj, xField);
    double y = env->GetDoubleField(obj, yField);
    double z = env->GetDoubleField(obj, zField);

    double otherX = env->GetDoubleField(other, xField);
    double otherY = env->GetDoubleField(other, yField);
    double otherZ = env->GetDoubleField(other, zField);

    Vector3D vector(x, y, z);
    Vector3D otherVector(otherX, otherY, otherZ);
    return vector.distanceTo(otherVector);
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_add
        (JNIEnv* env, jobject obj, jdouble x, jdouble y, jdouble z) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double currentX = env->GetDoubleField(obj, xField);
    double currentY = env->GetDoubleField(obj, yField);
    double currentZ = env->GetDoubleField(obj, zField);

    Vector3D vector(currentX, currentY, currentZ);
    Vector3D result = vector.add(Vector3D(x, y, z));

    jobject resultObj = env->NewObject(vectorClass, env->GetMethodID(vectorClass, "<init>", "(DDD)V"), result.x, result.y, result.z);
    return resultObj;
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_subtract
        (JNIEnv* env, jobject obj, jdouble x, jdouble y, jdouble z) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double currentX = env->GetDoubleField(obj, xField);
    double currentY = env->GetDoubleField(obj, yField);
    double currentZ = env->GetDoubleField(obj, zField);

    Vector3D vector(currentX, currentY, currentZ);
    Vector3D result = vector.subtract(Vector3D(x, y, z));

    jobject resultObj = env->NewObject(vectorClass, env->GetMethodID(vectorClass, "<init>", "(DDD)V"), result.x, result.y, result.z);
    return resultObj;
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_scale
        (JNIEnv* env, jobject obj, jdouble scalar) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double currentX = env->GetDoubleField(obj, xField);
    double currentY = env->GetDoubleField(obj, yField);
    double currentZ = env->GetDoubleField(obj, zField);

    Vector3D vector(currentX, currentY, currentZ);
    Vector3D result = vector.scale(scalar);

    jobject resultObj = env->NewObject(vectorClass, env->GetMethodID(vectorClass, "<init>", "(DDD)V"), result.x, result.y, result.z);
    return resultObj;
}

JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_dot
        (JNIEnv* env, jobject obj, jobject other) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double x = env->GetDoubleField(obj, xField);
    double y = env->GetDoubleField(obj, yField);
    double z = env->GetDoubleField(obj, zField);

    double otherX = env->GetDoubleField(other, xField);
    double otherY = env->GetDoubleField(other, yField);
    double otherZ = env->GetDoubleField(other, zField);

    Vector3D vector(x, y, z);
    Vector3D otherVector(otherX, otherY, otherZ);
    return vector.dot(otherVector);
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_cross
        (JNIEnv* env, jobject obj, jobject other) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double x = env->GetDoubleField(obj, xField);
    double y = env->GetDoubleField(obj, yField);
    double z = env->GetDoubleField(obj, zField);

    double otherX = env->GetDoubleField(other, xField);
    double otherY = env->GetDoubleField(other, yField);
    double otherZ = env->GetDoubleField(other, zField);

    Vector3D vector(x, y, z);
    Vector3D otherVector(otherX, otherY, otherZ);
    Vector3D result = vector.cross(otherVector);

    jobject resultObj = env->NewObject(vectorClass, env->GetMethodID(vectorClass, "<init>", "(DDD)V"), result.x, result.y, result.z);
    return resultObj;
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_normalize
        (JNIEnv* env, jobject obj) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double x = env->GetDoubleField(obj, xField);
    double y = env->GetDoubleField(obj, yField);
    double z = env->GetDoubleField(obj, zField);

    Vector3D vector(x, y, z);
    Vector3D result = vector.normalize();

    jobject resultObj = env->NewObject(vectorClass, env->GetMethodID(vectorClass, "<init>", "(DDD)V"), result.x, result.y, result.z);
    return resultObj;
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_negate
        (JNIEnv* env, jobject obj) {
    jclass vectorClass = env->GetObjectClass(obj);
    jfieldID xField = env->GetFieldID(vectorClass, "x", "D");
    jfieldID yField = env->GetFieldID(vectorClass, "y", "D");
    jfieldID zField = env->GetFieldID(vectorClass, "z", "D");

    double x = env->GetDoubleField(obj, xField);
    double y = env->GetDoubleField(obj, yField);
    double z = env->GetDoubleField(obj, zField);

    Vector3D vector(x, y, z);
    Vector3D result = vector.negate();

    jobject resultObj = env->NewObject(vectorClass, env->GetMethodID(vectorClass, "<init>", "(DDD)V"), result.x, result.y, result.z);
    return resultObj;
}
}
