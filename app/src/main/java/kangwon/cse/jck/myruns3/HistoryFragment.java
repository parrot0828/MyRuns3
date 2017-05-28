package kangwon.cse.jck.myruns3;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<ExerciseEntry>> {
    public static final String FROM_HISTORY = "From_History";
    public static final String ROW_INDEX = "Row_Index";
    private HistoryAdapter adapter;

    class HistoryAdapter extends ArrayAdapter<ExerciseEntry> {
        HistoryAdapter(Context context, List<ExerciseEntry> entries) {
            super(context, 0, entries);
        }

        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
            }
            ExerciseEntry entry = (ExerciseEntry) getItem(position);
            ((TextView) view.findViewById(R.id.history_item_first_line)).setText(getFirstLine(entry));
            ((TextView) view.findViewById(R.id.history_item_second_line)).setText(getSecondLine(entry));
            ((TextView) view.findViewById(R.id.history_item_rowid)).setText(entry.getmId() + "");
            return view;
        }

        private String getFirstLine(ExerciseEntry entry) {
            String input = StartFragment.ID_TO_INPUT[entry.getmInputType()];
            String activity = StartFragment.ID_TO_ACTIVITY[entry.getmActivityType()];
            return input + ": " + activity + ", " + HistoryFragment.formatDateTime(entry.getmDateTime());
        }

        private String getSecondLine(ExerciseEntry entry) {
            String distance = HistoryFragment.formatDistance(entry.getmDistance(),
                    PreferenceManager.getDefaultSharedPreferences(HistoryFragment.this.getActivity()).getString(HistoryFragment.this.getString(R.string.unit_preference)
                            , HistoryFragment.this.getString(R.string.unit_km)));
            return distance + ", " + HistoryFragment.formatDuration(entry.getmDuration());
        }
    }

    public static class HistoryLoader extends AsyncTaskLoader<List<ExerciseEntry>> {
        public HistoryLoader(Context context) {
            super(context);
        }

        public List<ExerciseEntry> loadInBackground() {
            Log.d("loading", "loading");
            return MainActivity.DBhelper != null ? MainActivity.DBhelper.fetchEntries() : null;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            this.adapter = new HistoryAdapter(getActivity(), new ArrayList());
            Log.d("adapter created", "adapter created");
            setListAdapter(this.adapter);
            loadData();
        }
        if (savedInstanceState != null) {
            setListAdapter(this.adapter);
            this.adapter.notifyDataSetChanged();
        }
    }

    private void loadData() {
        if (getLoaderManager().getLoader(0) == null) {
            getLoaderManager().initLoader(0, null, this).forceLoad();
        } else {
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public Loader<List<ExerciseEntry>> onCreateLoader(int id, Bundle args) {
        Log.d("HistoryFragment: ", "Loader Created");
        return new HistoryLoader(getContext());
    }

    public void onLoadFinished(Loader<List<ExerciseEntry>> loader, List<ExerciseEntry> data) {
        Log.d("HistoryFragment: ", "Loading Finished");
        this.adapter.clear();
        this.adapter.addAll(data);
    }

    public void onLoaderReset(Loader<List<ExerciseEntry>> loader) {
    }

    public static String formatDateTime(long dateTime) {
        Date date = new Date(dateTime);
        return new SimpleDateFormat("hh:mm:ss").format(date) + " " + new SimpleDateFormat("MMM dd yyyy").format(date);
    }

    public static String formatDuration(double duration) {
        int minutes = (int) (duration / 60.0d);
        int seconds = (int) (duration % 60.0d);
        if (minutes == 0 && seconds == 0) {
            return "0secs";
        }
        return String.valueOf(minutes) + "min " + String.valueOf(seconds) + "secs";
    }

    public static String formatDistance(double distance, String unitPref) {
        if (unitPref.equals("Miles")) {
            distance /= ManualEntryActivity.MILES2KM;
        }
        return String.format("%.2f", new Object[]{Double.valueOf(distance)}) + " " + unitPref;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        Intent mIntent;
        super.onListItemClick(parent, v, position, id);
        long rowid = Long.parseLong(((TextView) v.findViewById(R.id.history_item_rowid)).getText().toString());
        if (MainActivity.DBhelper.fetchEntryByIndex(rowid).getmInputType() == ((Integer) StartFragment.INPUT_TO_ID_MAP.get(StartFragment.MANUAL_ENTRY)).intValue()) {
            mIntent = new Intent(getActivity(), DisplayEntryActivity.class);
        } else {
            mIntent = new Intent(getActivity(), MapDisplayActivity.class);
        }
        mIntent.putExtra(FROM_HISTORY, true);
        mIntent.putExtra(ROW_INDEX, rowid);
        getActivity().startActivity(mIntent);
    }
    /*Life cycle 알아보기위해서 onStart, onResume, onPause, onStop 추가!*/
    public void onStart() {
        super.onStart();
        Toast.makeText(getActivity(),"HistoryFragment start",Toast.LENGTH_SHORT).show();
    }
    public void onStop() {
        super.onStop();
        Toast.makeText(getActivity(),"HistoryFragment stop",Toast.LENGTH_SHORT).show();
    }

    public void onPause() {
        super.onPause();
        Toast.makeText(getActivity(),"HistoryFragment pause",Toast.LENGTH_SHORT).show();

    }
    public void onResume() {
        super.onResume();
        Toast.makeText(getActivity(),"HistoryFragment resume",Toast.LENGTH_SHORT).show();
    }
}