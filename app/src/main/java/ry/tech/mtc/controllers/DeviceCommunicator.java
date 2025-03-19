package ry.tech.mtc.controllers;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ry.tech.mtc.models.Device;

public class DeviceCommunicator {
    private static final String BASE_URL = "http://your-iot-server.com/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;

    public DeviceCommunicator() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void sendCommand(Device device, String command, Object value) {
        try {
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("deviceId", device.getId());
            jsonBody.addProperty("command", command);
            jsonBody.addProperty("value", value.toString());

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/devices/" + device.getId() + "/command")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Обработка ошибок
        }
    }

    public JsonObject receiveData(Device device) {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/devices/" + device.getId() + "/status")
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseData = response.body().string();
                com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
                return parser.parse(responseData).getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // В случае ошибки возвращаем пустой объект
            return new JsonObject();
        }
    }
}
