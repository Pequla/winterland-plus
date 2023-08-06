package com.pequla.winterland.service;

import com.google.gson.Gson;
import com.pequla.winterland.model.DataModel;
import com.pequla.winterland.model.ErrorModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebService {

    private static WebService instance;
    private final HttpClient client;
    private final Gson gson;

    private WebService() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.gson = new Gson();
    }

    public static WebService getInstance() {
        if (instance == null) {
            instance = new WebService();
        }
        return instance;
    }

    public DataModel getLinkDataForGuild(String uuid, String guild) throws IOException, InterruptedException {
        String url = "https://link.samifying.com/api/user/" + guild + "/" + uuid;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (rsp.statusCode() == 200) {
            return gson.fromJson(rsp.body(), DataModel.class);
        }

        if (rsp.statusCode() == 500) {
            ErrorModel model = gson.fromJson(rsp.body(), ErrorModel.class);
            throw new RuntimeException(model.getMessage());
        }

        throw new RuntimeException("Response code " + rsp.statusCode());
    }
}
