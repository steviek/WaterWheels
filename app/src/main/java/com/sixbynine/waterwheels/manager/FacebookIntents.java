package com.sixbynine.waterwheels.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.sixbynine.waterwheels.model.Post;
import com.sixbynine.waterwheels.model.Profile;

public final class FacebookIntents {

  private FacebookIntents() {
  }

  public static Intent viewProfile(Context context, Profile profile) {
    try {
      context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
      return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + profile.getId()));
    } catch (PackageManager.NameNotFoundException e) {
      return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + profile.getId()));
    }
  }

  public static Intent viewPost(Context context, Post post) {
    try {
      context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
      return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://post/" + post.getId()));
    } catch (PackageManager.NameNotFoundException e) {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      String[] parts = post.getId().split("_");
      if (isMobile()) {
        intent.setData(Uri.parse(String.format("https://m.facebook.com/groups/%s?view=permalink&id=%s&fs=2",
            parts[0], parts[1])));
      } else {
        intent.setData(Uri.parse("https://www.facebook.com/" + parts[0] + "/posts/" + parts[1]));
      }
      return intent;
    }
  }

  private static boolean isMobile() {
    return true;
  }
}
