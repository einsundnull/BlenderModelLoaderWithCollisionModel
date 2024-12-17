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
//    private static final String FILE_NAME = "bodies.json";
//    private static MainActivity mainActivity;
//
//    public FileClassBodyValues(MainActivity mainActivity) {
//        this.mainActivity = mainActivity;
//    }
//
//
//    public static void saveSpheres(Context context, List<Object> bodies) {
//        File file = new File(context.getFilesDir(), FILE_NAME);
//        try (FileWriter writer = new FileWriter(file)) {
//            JsonWriter jsonWriter = new JsonWriter(writer);
//            jsonWriter.beginArray();
//            for (Object body : bodies) {
//                jsonWriter.beginObject();
//                jsonWriter.name("x").value(body.getX());
//                jsonWriter.name("y").value(body.getY());
//                jsonWriter.name("z").value(body.getZ());
//                jsonWriter.name("vx").value(body.getVx());
//                jsonWriter.name("vy").value(body.getVy());
//                jsonWriter.name("vz").value(body.getVz());
//                jsonWriter.name("mass").value(body.getMass());
//                jsonWriter.name("size").value(body.getSize());
//                jsonWriter.name("followsGravity").value(body.isFollowGravity());
//                jsonWriter.name("isTiltEnabled").value(body.isTiltEnabled());
//                jsonWriter.name("attractsOther").value(body.isAttractsOther());
//                jsonWriter.name("isAttractedByOther").value(body.isAttractedByOther());
//                jsonWriter.name("bouncesOff").value(body.isAttractedByOther());
//                jsonWriter.name("name").value(body.getName());
//                jsonWriter.endObject();
//            }
//            jsonWriter.endArray();
//            jsonWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//
//    public static ArrayList<ObjectBlenderModel> loadSpheres(Context context) {
//        File file = new File(context.getFilesDir(), FILE_NAME);
//        ArrayList<ObjectBlenderModel> ObjectBlenderModel = new ArrayList<>();
//        if (file.exists()) {
//            try (FileReader reader = new FileReader(file)) {
//                JsonReader jsonReader = new JsonReader(reader);
//                jsonReader.beginArray();
//                while (jsonReader.hasNext()) {
//                    jsonReader.beginObject();
//                    double x = 0, y = 0, z = 0, vx = 0, vy = 0, vz = 0;
//                    int color = 0,  mass = 0, size = 0;
//
//                    String name = "", objectsFileName = "";
//                    boolean attractsOther = true, isAttractedByOther = true , isTiltEnabled = true,followsGravity = true , bouncesOff = true;
//                    while (jsonReader.hasNext()) {
//                        String key = jsonReader.nextName();
//                        switch (key) {
//                            case "x":
//                                x = jsonReader.nextDouble();
//                                break;
//                            case "y":
//                                y = jsonReader.nextDouble();
//                                break;
//                            case "z":
//                                z = jsonReader.nextDouble();
//                                break;
//                            case "vx":
//                                vx = jsonReader.nextDouble();
//                                break;
//                            case "vy":
//                                vy = jsonReader.nextDouble();
//                                break;
//                            case "vz":
//                                vz = jsonReader.nextDouble();
//                                break;
//                            case "mass":
//                                mass = jsonReader.nextInt();
//                                break;
//                            case "color":
//                                color = jsonReader.nextInt();
//                                break;
//                            case "size":
//                                size = jsonReader.nextInt();
//                                break;
//                            case "followsGravity":
//                                isTiltEnabled = jsonReader.nextBoolean();
//                                break;
//                            case "isTiltEnabled":
//                                isAttractedByOther = jsonReader.nextBoolean();
//                                break;
//                            case "attractsOther":
//                                attractsOther = jsonReader.nextBoolean();
//                                break;
//                            case "isAttractedByOther":
//                                isAttractedByOther = jsonReader.nextBoolean();
//                                break;
//                            case "bouncesOff":
//                                bouncesOff = jsonReader.nextBoolean();
//                                break;
//                            case "name":
//                                name = jsonReader.nextString();
//                                break;
//                            case "objectsFileName":
//                                objectsFileName = jsonReader.nextString();
//                                break;
//                        }
//                    }
//                    ObjectBlenderModel.add(new ObjectBlenderModel(context,ObjectBlenderModel.size() -1,x, y, z, vx, vy, vz, mass, color,  size,followsGravity,isTiltEnabled,attractsOther ,isAttractedByOther,bouncesOff,name, objectsFileName));
//                    jsonReader.endObject();
//                }
//                jsonReader.endArray();
//                jsonReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            // Load default spheres if the file does not exist
//            Objects = mainActivity.initObjects();
//        }
//        return Objects;
//    }

}
