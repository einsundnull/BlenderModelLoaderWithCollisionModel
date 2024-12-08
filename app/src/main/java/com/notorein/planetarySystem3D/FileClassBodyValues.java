package com.notorein.planetarySystem3D;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileClassBodyValues {
    private static final String FILE_NAME = "bodies.json";
    private static MainActivity mainActivity;


    public FileClassBodyValues(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public static void saveSpheres(Context context, List<Sphere> bodies) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginArray();
            for (Sphere body : bodies) {
                jsonWriter.beginObject();
                jsonWriter.name("x").value(body.getX());
                jsonWriter.name("y").value(body.getY());
                jsonWriter.name("z").value(body.getY());
                jsonWriter.name("vx").value(body.getVx());
                jsonWriter.name("vy").value(body.getVy());
                jsonWriter.name("vz").value(body.getVy());
                jsonWriter.name("mass").value(body.getMass());
                jsonWriter.name("color").value(body.getColor());
                jsonWriter.name("colorTrail").value(body.getColorTrail());
                jsonWriter.name("size").value(body.getSize());
                jsonWriter.name("trailLength").value(body.getTrailLength());
                jsonWriter.name("trailThickness").value(body.getTrailThickness());
                jsonWriter.name("name").value(body.getName());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<Sphere> loadSpheres(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        ArrayList<Sphere> spheres = new ArrayList<>();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject();
                    double x = 0, y = 0, z = 0, vx = 0, vy = 0, vz = 0, mass = 0;
                    int color = 0, colorTrail = 0;
                    float size = 0, trailLength = 0, trailThickness = 0;
                    String name = "";
                    while (jsonReader.hasNext()) {
                        String key = jsonReader.nextName();
                        switch (key) {
                            case "x":
                                x = jsonReader.nextDouble();
                                break;
                            case "y":
                                y = jsonReader.nextDouble();
                                break;
                            case "z":
                                y = jsonReader.nextDouble();
                                break;
                            case "vx":
                                vx = jsonReader.nextDouble();
                                break;
                            case "vy":
                                vy = jsonReader.nextDouble();
                                break;
                            case "vz":
                                vy = jsonReader.nextDouble();
                                break;
                            case "mass":
                                mass = jsonReader.nextDouble();
                                break;
                            case "color":
                                color = jsonReader.nextInt();
                                break;
                            case "colorTrail":
                                colorTrail = jsonReader.nextInt();
                                break;
                            case "size":
                                size = (float) jsonReader.nextDouble();
                                break;
                            case "trailLength":
                                trailLength = (float) jsonReader.nextDouble();
                                break;
                            case "trailThickness":
                                trailThickness = (float) jsonReader.nextDouble();
                                break;
                            case "name":
                                name = jsonReader.nextString();
                                break;
                        }
                    }
                    spheres.add(new Sphere(spheres.size() -1,x, y, z, vx, vy, vz, mass, color, colorTrail, size, trailLength, trailThickness, name));
                    jsonReader.endObject();
                }
                jsonReader.endArray();
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Load default spheres if the file does not exist
       
            spheres = mainActivity.initSpheres();
        }
        return spheres;
    }
}
