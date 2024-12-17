package com.notorein.planetarySystem3D;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CollisionModelModelLoader {

    private  final String TAG = "CollisionModelModelLoader";
    private  List<Vector3D> vertices = new ArrayList<>();
    private  List<Vector3D> normals = new ArrayList<>();
    private  List<Vector2D> texCoords = new ArrayList<>();

    public  List<CollisionModelTriangle> loadTrianglesFromOBJ(Context context, String objFileName) {
        List<CollisionModelTriangle> collisionModelTriangles = new ArrayList<>();

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("models/" + objFileName + ".obj");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts[0].equals("v")) {
                    vertices.add(new Vector3D(
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3])
                    ));
                } else if (parts[0].equals("vn")) {
                    normals.add(new Vector3D(
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3])
                    ));
                } else if (parts[0].equals("vt")) {
                    texCoords.add(new Vector2D(
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2])
                    ));
                } else if (parts[0].equals("f")) {
                    Vector3D[] faceVertices = new Vector3D[3];
                    Vector3D[] faceNormals = new Vector3D[3];
                    Vector2D[] faceTexCoords = new Vector2D[3];
                    for (int i = 1; i < parts.length; i++) {
                        String[] faceParts = parts[i].split("/");
                        int vertexIndex = Integer.parseInt(faceParts[0]) - 1;
                        int texCoordIndex = Integer.parseInt(faceParts[1]) - 1;
                        int normalIndex = Integer.parseInt(faceParts[2]) - 1;
                        faceVertices[i - 1] = vertices.get(vertexIndex);
                        faceTexCoords[i - 1] = texCoords.get(texCoordIndex);
                        faceNormals[i - 1] = normals.get(normalIndex);
                    }
                    collisionModelTriangles.add(new CollisionModelTriangle(faceVertices[0], faceVertices[1], faceVertices[2], faceNormals[0], faceNormals[1], faceNormals[2], faceTexCoords[0], faceTexCoords[1], faceTexCoords[2]));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Loaded " + vertices.size() + " vertices");
        Log.i(TAG, "Loaded " + normals.size() + " normals");
        Log.i(TAG, "Loaded " + texCoords.size() + " texture coordinates");
        Log.i(TAG, "Loaded " + collisionModelTriangles.size() + " collisionModelTriangles");

        return collisionModelTriangles;
    }

    public  List<Vector3D> getVertices() {
        return vertices;
    }

    public  List<Vector3D> getNormals() {
        return normals;
    }

    public  List<Vector2D> getTexCoords() {
        return texCoords;
    }
}
