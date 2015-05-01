package org.republica.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * A special Activity which is displayed like a dialog and shows a room image. Specify the room name and the room image id as Intent extras.
 *
 * @author Christophe Beyls
 */
public class RoomImageDialogActivity extends Activity {

    public static final String EXTRA_ROOM_NAME = "roomName";
    public static final String EXTRA_ROOM_IMAGE_RESOURCE_ID = "imageResId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        setTitle(intent.getStringExtra(EXTRA_ROOM_NAME));

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(intent.getIntExtra(EXTRA_ROOM_IMAGE_RESOURCE_ID, 0));
        setContentView(imageView);
    }
}
