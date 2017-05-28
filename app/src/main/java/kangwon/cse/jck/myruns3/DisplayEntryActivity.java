package kangwon.cse.jck.myruns3;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
public class DisplayEntryActivity extends AppCompatActivity {
    private long rowid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entry);
        this.rowid = getIntent().getExtras().getLong(HistoryFragment.ROW_INDEX);
        ExerciseEntry entry = MainActivity.DBhelper.fetchEntryByIndex(this.rowid);
        ((EditText) findViewById(R.id.display_input)).setText(StartFragment.ID_TO_INPUT[entry.getmInputType()]);
        ((EditText) findViewById(R.id.display_activity)).setText(StartFragment.ID_TO_ACTIVITY[entry.getmActivityType()]);
        ((EditText) findViewById(R.id.display_datetime)).setText(HistoryFragment.formatDateTime(entry.getmDateTime()));
        ((EditText) findViewById(R.id.display_duration)).setText(HistoryFragment.formatDuration(entry.getmDuration()));
        ((EditText) findViewById(R.id.display_distance)).setText(HistoryFragment.formatDistance(entry.getmDistance(),
                PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.unit_preference), getString(R.string.unit_km))));
        ((EditText) findViewById(R.id.display_calories)).setText(entry.getmCalorie() + " cals");
        ((EditText) findViewById(R.id.display_heartrate)).setText(entry.getmHeartRate() + " bpm");
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "삭제").setShowAsAction(2);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        runThread();
        finish();
        return true;
    }
    private void runThread() {
        new Thread() {
            public void run() {
                MainActivity.DBhelper.removeEntry(DisplayEntryActivity.this.rowid);
            }
        }.start();
    }
}
