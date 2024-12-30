#include <jni.h>
#include <cmath>

class Vector2D {
public:
    double u, v;

    Vector2D(double u, double v) : u(u), v(v) {}

    Vector2D add(const Vector2D& other) const {
        return Vector2D(this->u + other.u, this->v + other.v);
    }

    Vector2D subtract(const Vector2D& other) const {
        return Vector2D(this->u - other.u, this->v - other.v);
    }

    Vector2D scale(double scalar) const {
        return Vector2D(this->u * scalar, this->v * scalar);
    }

    double dot(const Vector2D& other) const {
        return this->u * other.u + this->v * other.v;
    }

    double magnitude() const {
        return std::sqrt(this->u * this->u + this->v * this->v);
    }

    Vector2D normalize() const {
        double mag = magnitude();
        return Vector2D(this->u / mag, this->v / mag);
    }

    double distanceTo(const Vector2D& other) const {
        double du = this->u - other.u;
        double dv = this->v - other.v;
        return std::sqrt(du * du + dv * dv);
    }
};

extern "C" {
JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector2D_magnitude
        (JNIEnv* env, jobject obj) {
    Vector2D* vector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(obj));
    return vector->magnitude();
}

JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector2D_distanceTo
        (JNIEnv* env, jobject obj, jobject other) {
    Vector2D* vector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(obj));
    Vector2D* otherVector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(other));
    return vector->distanceTo(*otherVector);
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector2D_add
        (JNIEnv* env, jobject obj, jdouble u, jdouble v) {
    Vector2D* vector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(obj));
    return new Vector2D(vector->add(Vector2D(u, v)));
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector2D_subtract
        (JNIEnv* env, jobject obj, jdouble u, jdouble v) {
    Vector2D* vector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(obj));
    return new Vector2D(vector->subtract(Vector2D(u, v)));
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector2D_scale
        (JNIEnv* env, jobject obj, jdouble scalar) {
    Vector2D* vector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(obj));
    return new Vector2D(vector->scale(scalar));
}

JNIEXPORT jdouble JNICALL Java_com_notorein_threedmodeling_utils_Vector2D_dot
        (JNIEnv* env, jobject obj, jobject other) {
    Vector2D* vector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(obj));
    Vector2D* otherVector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(other));
    return vector->dot(*otherVector);
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_Vector2D_normalize
        (JNIEnv* env, jobject obj) {
    Vector2D* vector = reinterpret_cast<Vector2D*>(env->GetDirectBufferAddress(obj));
    return new Vector2D(vector->normalize());
}
}
