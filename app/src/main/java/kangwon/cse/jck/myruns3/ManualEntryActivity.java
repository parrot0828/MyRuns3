package kangwon.cse.jck.myruns3;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class ManualEntryActivity extends ListActivity {

    public static final String[] MANUAL_OPTIONS = new String[]{"날짜", "시각", "지속 시간", "거리",
            "칼로리", "맥박 수", "메모"};

    // Constants for case statement
    private static final int MANUAL_DATE = 0;
    private static final int MANUAL_TIME = 1;
    private static final int MANUAL_DURATION = 2;
    private static final int MANUAL_DISTANCE = 3;
    private static final int MANUAL_CALORIES = 4;
    private static final int MANUAL_HEARTRATE = 5;
    private static final int MANUAL_COMMENT = 6;

    // For date and time functionality
    public Calendar mDateAndTime;

    // Unit preference conversion
    public static final double MILES2KM = 1.60934;  // 1 마일이 몇 킬로미터인가?
    public static final double MILES2CAL = 100.0;   // 1 마일을 가면 몇 칼로리 소모하나?

    // Database
    public static ExerciseEntry entry;  // 운동 항목 (한 번의 운동)
    private class WriteToDB extends AsyncTask<ExerciseEntry,Void,String> {
        @Override
        protected String doInBackground(ExerciseEntry... exerciseEntries) {
            long id = MainActivity.DBhelper.insertEnrty(entry);
            return ""+id;
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),"Entry #"+result+" saved.",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);
        Bundle bundle = getIntent().getExtras();

        entry = new ExerciseEntry();

        // Set input type and activity type
        entry.setmInputType(bundle.getInt(StartFragment.INPUT_TYPE,0)); // 입력 방법
        entry.setmActivityType(bundle.getInt(StartFragment.ACTIVITY_TYPE, 0));  // 운동 종류

        // Set time and date
        mDateAndTime = Calendar.getInstance();    // 현재 일시.
        entry.setmDateTime(mDateAndTime.getTimeInMillis());
        Log.d("date",mDateAndTime.getTimeInMillis()+"");

        // 일단 기본 값 저장.
        entry.setmDuration(0);
        entry.setmDistance(0);
        entry.setmCalorie(0);
        entry.setmHeartRate(0);

        // Create a new adapter
        ArrayAdapter<String> manualAdapter = new ArrayAdapter<String>(this, R.layout.list_manual,
                MANUAL_OPTIONS);
        setListAdapter(manualAdapter);

        // Get the ListView
        final ListView manualListView = getListView();

        // Define the listener
        OnItemClickListener mListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // 선택된 리스트 항목에 맞는 대화창을 띄워준다.
                switch(position){

                    case MANUAL_DATE:
                        // DatePckerDialog를 띄운다.
                        selectManualDate(manualListView);
                        break;

                    case MANUAL_TIME:
                        // TimePckerDialog를 띄운다.
                         selectManualTime(manualListView);
                        break;

                    case MANUAL_DURATION:
                        DialogFragment durationPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.DURATION_PICKER_ID);
                        durationPickerWindow.show(getFragmentManager(), "Duration");
                        break;

                    case MANUAL_DISTANCE:
                        DialogFragment distancePickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.DISTANCE_PICKER_ID);
                        distancePickerWindow.show(getFragmentManager(), "Distance");
                        break;

                    case MANUAL_CALORIES:
                        DialogFragment caloresPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.CALORIES_PICKER_ID);
                        caloresPickerWindow.show(getFragmentManager(), "Calories");
                        break;

                    case MANUAL_HEARTRATE:
                        DialogFragment heartPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.HEARTRATE_PICKER_ID);
                        heartPickerWindow.show(getFragmentManager(), "Heart Rate");
                        break;

                    case MANUAL_COMMENT:
                        DialogFragment commentPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.COMMENT_PICKER_ID);
                        commentPickerWindow.show(getFragmentManager(), "Comments");
                        break;

                    default:
                        break;
                }

            }
        };

        //  리스트에 리스너를 등록한다.
        manualListView.setOnItemClickListener(mListener);
    }


    /////////////////////// Helper Functions ///////////////////////

    /**
     * Select the time using the Android widget
     * @param v
     */
    public void selectManualTime(View v) {

        TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDateAndTime.set(Calendar.MINUTE, minute);
                mDateAndTime.set(Calendar.SECOND,0);
                entry.setmDateTime(mDateAndTime.getTimeInMillis());
            }
        };

        new TimePickerDialog(ManualEntryActivity.this, mTimeListener,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), true).show();
    }

    /**
     * Select the date using the Android widget
     * @param v
     */
    public void selectManualDate(View v) {

        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                entry.setmDateTime(mDateAndTime.getTimeInMillis());
            }
        };

        new DatePickerDialog(ManualEntryActivity.this, mDateListener,
                mDateAndTime.get(Calendar.YEAR),
                mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH)).show();
    }


    /////////////////////// Handle Selection of buttons ///////////////////////

    /**
     * Handle the selection of the save button
     * @param v
     */
    public void selectManualSave(View v) {

        // ExerciseEntry를 데이터베이스에 저장한다. (미구현)
//        new WriteToDB().execute();

        Toast.makeText(getApplicationContext(), "" + entry,
                Toast.LENGTH_SHORT).show();

        // Close the activity
        finish();
    }

    /**
     * Handle the selection of the cancel button
     * @param v
     */
    public void selectManualCancel(View v) {

        // Inform user that the profile information is discarded
        Toast.makeText(getApplicationContext(), "저장되지 않았습니다.",
                Toast.LENGTH_SHORT).show();

        // Close the activity
        finish();
    }


}