package preference.internal;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import me.denley.wearpreferenceactivity.R;

public abstract class TitledWearActivity extends Activity {

    TextView heading;

    @Override protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        heading = (TextView) findViewById(R.id.heading);
        heading.setText(getTitle());
        heading.setTextColor(-1);
    }

    @Override public void setTitle(final CharSequence title) {
        super.setTitle(title);

        if(heading != null) {
            heading.setText(title);
            heading.setTextColor(-1);
        }
    }

}
