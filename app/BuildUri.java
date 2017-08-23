package edu.buffalo.cse.cse486586.groupmessenger2;

import android.net.Uri;

/**
 * Created by Molu on 20/2/17.
 */

public class BuildUri {

    public static Uri build(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
}
