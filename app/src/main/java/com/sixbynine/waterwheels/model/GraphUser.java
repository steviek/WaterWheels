package com.sixbynine.waterwheels.model;

import org.json.JSONException;
import org.json.JSONObject;

public final class GraphUser {

    private final String id;
    private final String link;
    private final String name;

    public GraphUser(String id, String link, String name) {
        this.id = id;
        this.link = link;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public static GraphUser fromJsonObject(JSONObject object) {
        try {
            String id = object.getString("id");
            String link = object.getString("link");
            String name = object.getString("name");
            return new GraphUser(id, link, name);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
