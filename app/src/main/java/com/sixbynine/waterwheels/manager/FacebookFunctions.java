package com.sixbynine.waterwheels.manager;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import com.sixbynine.waterwheels.model.Post;
import com.sixbynine.waterwheels.model.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public final class FacebookFunctions {

    private FacebookFunctions() {}

    public static final Function<JSONObject, Profile> JSON_OBJECT_PROFILE = new Function<JSONObject, Profile>() {
        @Override
        public Profile apply(JSONObject object) {
            try {
                String id = object.getString("id");
                String name = object.getString("name");
                return new Profile(name, id);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public static final Function<JSONObject, Post> JSON_OBJECT_POST = new Function<JSONObject, Post>() {
        @Override
        public Post apply(JSONObject object) {
            try {
                String id = object.getString("id");
                String message = object.optString("message", null);

                if (message == null) {
                    return null;
                }

                long createdTime = object.getLong("created_time");
                long updatedTime = object.getLong("updated_time");
                Profile from = JSON_OBJECT_PROFILE.apply(object.getJSONObject("from"));
                return new Post(
                        id,
                        message,
                        createdTime,
                        updatedTime,
                        from);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public static final Function<JSONObject, List<Post>> GRAPH_RESPONSE_POST_LIST = new Function<JSONObject, List<Post>>() {
        @Override
        public List<Post> apply(JSONObject object) {
            try {
                JSONArray arr = object.getJSONArray("data");
                ImmutableList.Builder<Post> list = ImmutableList.builder();
                for (int i = 0; i < arr.length(); i++) {
                    Post post = JSON_OBJECT_POST.apply(arr.getJSONObject(i));
                    if (post != null) {
                        list.add(post);
                    }
                }
                return list.build();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };
}
