#include <jni.h>
#include <cmath>
#include "Vector3D.cpp"
#include "Vector2D.cpp"

class ObjectModelTriangle {
public:
    Vector3D v0;
    Vector3D v1;
    Vector3D v2;
    Vector3D n0;
    Vector3D n1;
    Vector3D n2;
    Vector2D t0;
    Vector2D t1;
    Vector2D t2;

    ObjectModelTriangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Vector2D t0, Vector2D t1, Vector2D t2)
            : v0(v0), v1(v1), v2(v2), n0(n0), n1(n1), n2(n2), t0(t0), t1(t1), t2(t2) {}

    Vector3D* getVertices() {
        return new Vector3D[3]{v0, v1, v2};
    }

    void setVertices(Vector3D* vertices) {
        if (vertices.length == 3) {
            this.v0 = vertices[0];
            this.v1 = vertices[1];
            this.v2 = vertices[2];
        }
    }

    Vector3D* getNormals() {
        return new Vector3D[3]{n0, n1, n2};
    }

    Vector2D* getTexCoords() {
        return new Vector2D[3]{t0, t1, t2};
    }

    Vector3D getCentroid() {
        return Vector3D(
                (v0.x + v1.x + v2.x) / 3.0,
                (v0.y + v1.y + v2.y) / 3.0,
                (v0.z + v1.z + v2.z) / 3.0
        );
    }

    bool intersects(ObjectModelTriangle* other) {
        return triangleTriangleIntersection(this, other);
    }

private:
    bool triangleTriangleIntersection(ObjectModelTriangle* t1, ObjectModelTriangle* t2) {
        Vector3D v0 = t1->v0;
        Vector3D v1 = t1->v1;
        Vector3D v2 = t1->v2;
        Vector3D u0 = t2->v0;
        Vector3D u1 = t2->v1;
        Vector3D u2 = t2->v2;

        Vector3D e1 = v1.subtract(v0);
        Vector3D e2 = v2.subtract(v0);
        Vector3D n1 = e1.cross(e2);
        double d = n1.dot(v0);

        Vector3D e3 = u1.subtract(u0);
        Vector3D e4 = u2.subtract(u0);
        Vector3D n2 = e3.cross(e4);
        double denom = n2.dot(u0);

        if (denom == 0.0) {
            return false; // Triangles are parallel
        }

        Vector3D diff = v0.subtract(u0);
        double t = n1.dot(diff) / denom;
        Vector3D p1 = v0.add(n1.scale(t));

        double u = n2.dot(diff) / denom;
        double v = n2.dot(e3) / denom;

        if (u >= 0.0 && v >= 0.0 && (u + v) <= 1.0) {
            return true;
        }

        return false;
    }
};

extern "C" {
JNIEXPORT jboolean JNICALL Java_com_notorein_threedmodeling_utils_ObjectModelTriangle_intersects
        (JNIEnv* env, jobject obj, jobject other) {
    ObjectModelTriangle* t1 = reinterpret_cast<ObjectModelTriangle*>(env->GetDirectBufferAddress(obj));
    ObjectModelTriangle* t2 = reinterpret_cast<ObjectModelTriangle*>(env->GetDirectBufferAddress(other));
    return t1->intersects(t2);
}

JNIEXPORT jobjectArray JNICALL Java_com_notorein_threedmodeling_utils_ObjectModelTriangle_getVertices
        (JNIEnv* env, jobject obj) {
    ObjectModelTriangle* triangle = reinterpret_cast<ObjectModelTriangle*>(env->GetDirectBufferAddress(obj));
    Vector3D* vertices = triangle->getVertices();
    jsize numVertices = 3;
    jclass vector3DClass = env->FindClass("com/notorein/threedmodeling/utils/Vector3D");
    jobjectArray verticesArray = env->NewObjectArray(numVertices, vector3DClass, NULL);
    for (jsize i = 0; i < numVertices; ++i) {
        jobject vertexObj = env->NewObject(vector3DClass, vector3DClass->GetMethodID(vector3DClass, "<init>", "(DDD)V"));
        env->SetDoubleField(vertexObj, vector3DClass->GetFieldID(vector3DClass, "x", "D"), vertices[i].x);
        env->SetDoubleField(vertexObj, vector3DClass->GetFieldID(vector3DClass, "y", "D"), vertices[i].y);
        env->SetDoubleField(vertexObj, vector3DClass->GetFieldID(vector3DClass, "z", "D"), vertices[i].z);
        env->SetObjectArrayElement(verticesArray, i, vertexObj);
        env->DeleteLocalRef(vertexObj);
    }
    return verticesArray;
}

JNIEXPORT jobjectArray JNICALL Java_com_notorein_threedmodeling_utils_ObjectModelTriangle_getNormals
        (JNIEnv* env, jobject obj) {
    ObjectModelTriangle* triangle = reinterpret_cast<ObjectModelTriangle*>(env->GetDirectBufferAddress(obj));
    Vector3D* normals = triangle->getNormals();
    jsize numNormals = 3;
    jclass vector3DClass = env->FindClass("com/notorein/threedmodeling/utils/Vector3D");
    jobjectArray normalsArray = env->NewObjectArray(numNormals, vector3DClass, NULL);
    for (jsize i = 0; i < numNormals; ++i) {
        jobject normalObj = env->NewObject(vector3DClass, vector3DClass->GetMethodID(vector3DClass, "<init>", "(DDD)V"));
        env->SetDoubleField(normalObj, vector3DClass->GetFieldID(vector3DClass, "x", "D"), normals[i].x);
        env->SetDoubleField(normalObj, vector3DClass->GetFieldID(vector3DClass, "y", "D"), normals[i].y);
        env->SetDoubleField(normalObj, vector3DClass->GetFieldID(vector3DClass, "z", "D"), normals[i].z);
        env->SetObjectArrayElement(normalsArray, i, normalObj);
        env->DeleteLocalRef(normalObj);
    }
    return normalsArray;
}

JNIEXPORT jobjectArray JNICALL Java_com_notorein_threedmodeling_utils_ObjectModelTriangle_getTexCoords
        (JNIEnv* env, jobject obj) {
    ObjectModelTriangle* triangle = reinterpret_cast<ObjectModelTriangle*>(env->GetDirectBufferAddress(obj));
    Vector2D* texCoords = triangle->getTexCoords();
    jsize numTexCoords = 3;
    jclass vector2DClass = env->FindClass("com/notorein/threedmodeling/utils/Vector2D");
    jobjectArray texCoordsArray = env->NewObjectArray(numTexCoords, vector2DClass, NULL);
    for (jsize i = 0; i < numTexCoords; ++i) {
        jobject texCoordObj = env->NewObject(vector2DClass, vector2DClass->GetMethodID(vector2DClass, "<init>", "(DD)V"));
        env->SetDoubleField(texCoordObj, vector2DClass->GetFieldID(vector2DClass, "u", "D"), texCoords[i].u);
        env->SetDoubleField(texCoordObj, vector2DClass->GetFieldID(vector2DClass, "v", "D"), texCoords[i].v);
        env->SetObjectArrayElement(texCoordsArray, i, texCoordObj);
        env->DeleteLocalRef(texCoordObj);
    }
    return texCoordsArray;
}

JNIEXPORT jobject JNICALL Java_com_notorein_threedmodeling_utils_ObjectModelTriangle_getCentroid
        (JNIEnv* env, jobject obj) {
    ObjectModelTriangle* triangle = reinterpret_cast<ObjectModelTriangle*>(env->GetDirectBufferAddress(obj));
    Vector3D centroid = triangle->getCentroid();
    jclass vector3DClass = env->FindClass("com/notorein/threedmodeling/utils/Vector3D");
    jobject centroidObj = env->NewObject(vector3DClass, vector3DClass->GetMethodID(vector3DClass, "<init>", "(DDD)V"));
    env->SetDoubleField(centroidObj, vector3DClass->GetFieldID(vector3DClass, "x", "D"), centroid.x);
    env->SetDoubleField(centroidObj, vector3DClass->GetFieldID(vector3DClass, "y", "D"), centroid.y);
    env->SetDoubleField(centroidObj, vector3DClass->GetFieldID(vector3DClass, "z", "D"), centroid.z);
    return centroidObj;
}
}
