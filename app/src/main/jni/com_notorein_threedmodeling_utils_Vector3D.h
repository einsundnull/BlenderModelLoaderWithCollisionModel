/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_notorein_threedmodeling_utils_Vector3D */

#ifndef _Included_com_notorein_threedmodeling_utils_Vector3D
#define _Included_com_notorein_threedmodeling_utils_Vector3D
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    magnitude
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_magnitude
        (JNIEnv *, jobject);

/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    distanceTo
 * Signature: (Lcom/notorein/threedmodeling/utils/Vector3D;)D
 */
JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_distanceTo
        (JNIEnv *, jobject, jobject);

/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    add
 * Signature: (DDD)Lcom/notorein/threedmodeling/utils/Vector3D;
 */
JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_add
        (JNIEnv *, jobject, jdouble, jdouble, jdouble);

/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    subtract
 * Signature: (DDD)Lcom/notorein/threedmodeling/utils/Vector3D;
 */
JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_subtract
        (JNIEnv *, jobject, jdouble, jdouble, jdouble);

/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    scale
 * Signature: (D)Lcom/notorein/threedmodeling/utils/Vector3D;
 */
JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_scale
        (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    dot
 * Signature: (Lcom/notorein/threedmodeling/utils/Vector3D;)D
 */
JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_dot
        (JNIEnv *, jobject, jobject);

/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    cross
 * Signature: (Lcom/notorein/threedmodeling/utils/Vector3D;)Lcom/notorein/threedmodeling/utils/Vector3D;
 */
JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_cross
        (JNIEnv *, jobject, jobject);

/*
 * Class:     com_notorein_threedmodeling_utils_Vector3D
 * Method:    normalize
 * Signature: ()Lcom/notorein/threedmodeling/utils/Vector3D;
 */
JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector3D_normalize
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
