package com.sixbynine.waterwheels.manager;

import com.google.common.collect.ImmutableList;
import com.sixbynine.waterwheels.model.Post;
import com.sixbynine.waterwheels.model.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

final class GraphResponseUtil {

  private GraphResponseUtil() {}

  private static Post getPost(JSONObject object) {
    try {
      String id = object.getString("id");
      String message = object.optString("message", null);

      if (message == null) {
        return null;
      }

      long createdTime = object.getLong("created_time");
      long updatedTime = object.getLong("updated_time");

      JSONObject from = object.getJSONObject("from");
      String fromId = from.getString("id");
      String fromName = from.getString("name");
      Profile fromProfile = new Profile(fromName, fromId);

      return new Post(
          id,
          message,
          createdTime,
          updatedTime,
          fromProfile);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  static List<Post> getPostsFromGraphResponse(JSONObject object) {
    try {
      JSONArray arr = object.getJSONArray("data");
      ImmutableList.Builder<Post> list = ImmutableList.builder();
      for (int i = 0; i < arr.length(); i++) {
        Post post = getPost(arr.getJSONObject(i));
        if (post != null) {
          list.add(post);
        }
      }
      return list.build();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
}
