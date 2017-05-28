package kangwon.cse.jck.myruns3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

public class MyRunsDialogFragment extends DialogFragment {

    private static final String DIALOG_ID_KEY = "which_dialog";

    // List of dialog IDs (여섯 종류의 대화창이 있다.)
    public static final int PHOTO_PICKER_ID = 0;
    public static final int DURATION_PICKER_ID = 1;
    public static final int DISTANCE_PICKER_ID = 2;
    public static final int CALORIES_PICKER_ID = 3;
    public static final int HEARTRATE_PICKER_ID = 4;
    public static final int COMMENT_PICKER_ID = 5;

    // List of options for photo picker dialog
    public static final int SELECT_FROM_CAMERA = 0;
    public static final int SELECT_FROM_GALLERY = 1;

    /**
     * Create a new instance of a dialog fragment
     * @param dialog_id
     * @return
     */
    public static MyRunsDialogFragment newInstance(int dialog_id) {

        MyRunsDialogFragment dialogFrag = new MyRunsDialogFragment();
        Bundle inputBundle = new Bundle();
        inputBundle.putInt(DIALOG_ID_KEY, dialog_id);
        dialogFrag.setArguments(inputBundle);

        return dialogFrag;
    }

    /**
     * Create dialog fragment
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity parent = getActivity();  // ManualEntryActivity
        int dialog_id = getArguments().getInt(DIALOG_ID_KEY);

        // Create dialog builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(parent);

        // Create OnClickListener based on the type of dialog fragment
        switch (dialog_id) {

            case PHOTO_PICKER_ID:
                buildPhotoPickerDialog(dialogBuilder, parent);
                return dialogBuilder.create();

            case DURATION_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, "지속 시간", dialog_id);
                return dialogBuilder.create();

            case DISTANCE_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, "거리",dialog_id);
                return dialogBuilder.create();

            case CALORIES_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, "칼로리",dialog_id);
                return dialogBuilder.create();

            case HEARTRATE_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, "백박 수",dialog_id);
                return dialogBuilder.create();

            case COMMENT_PICKER_ID:
                buildEditPickerDialog(dialogBuilder, parent, "메모", dialog_id);
                return dialogBuilder.create();

            default:
                return null;
        }
    }


    /////////////////////// Helper Functions for building dialogs ///////////////////////

    /**
     * build the photo picker dialog
     * @param dialogBuilder
     * @param parent
     */
    private void buildPhotoPickerDialog(AlertDialog.Builder dialogBuilder, final Activity parent){

        dialogBuilder.setTitle("사진 가져오는 방법");

        // Create listener
        DialogInterface.OnClickListener photoPickerlistener =
                new DialogInterface.OnClickListener() {

                    @Override // Override the onClick method
                    public void onClick(DialogInterface dialog, int item) {
                        ((UserProfileActivity) parent).selectPhotoPickerItem(item);
                    }
                };

        // Create the dialog
        dialogBuilder.setItems(R.array.ui_photo_picker_list, photoPickerlistener);
    }

    /**
     * 문자 입력아나 숫자 입력을 위한 DialogBuiler 설정.
     * @param dialogBuilder
     * @param parent
     */
    private void buildEditPickerDialog(final AlertDialog.Builder dialogBuilder, final Activity parent,
                                       final String title, int id){
        dialogBuilder.setTitle(title);

        // Use an EditText view to get user input
        final EditText input = new EditText(parent);

        if(id == DISTANCE_PICKER_ID || id == DURATION_PICKER_ID) {
            // 숫자 입력, 소수점 허용, 양수 음수 부호 허용
            input.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }else if (id == COMMENT_PICKER_ID) {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("어땠어요?");
            input.setLines(4);
        }else{
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        dialogBuilder.setView(input);

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int btnid) {
                String value = input.getText().toString();

                switch (title) {
                    case "지속 시간":
                        double valueInSeconds = Double.parseDouble(value)*60;
                        ((ManualEntryActivity) parent).entry.
                                setmDuration(!value.equals("") ? valueInSeconds : 0);
                        break;
                    case "거리":
                        double val;
                        if(!value.equals(""))
                            val = Double.parseDouble(value);
                        else
                            val = 0;

                        // Find unit
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String unitPref = pref.getString("unit_preference",
                                "Kilometers");

                        if (unitPref.equals("Miles")) {
                            val *= ManualEntryActivity.MILES2KM;
                        }
                        ((ManualEntryActivity) parent).entry.
                                setmDistance(!value.equals("") ? val : 0);
                        break;
                    case "칼로리":
                        ((ManualEntryActivity) parent).entry.
                                setmCalorie(!value.equals("") ? Integer.parseInt(value) : 0);
                        break;
                    case "맥박 수":
                        ((ManualEntryActivity) parent).entry.
                                setmHeartRate(!value.equals("") ? Integer.parseInt(value) : 0);
                        break;
                    case "메모":
                        ((ManualEntryActivity) parent).entry.setmComment(value);
                        break;
                }
                return;
            }
        });

        dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int btnid) {
                return;
            }
        });
    }
    /*Life cycle 알아보기위해서 onStart, onResume, onPause, onStop 추가!*/
    public void onStart() {
        super.onStart();
        Toast.makeText(getActivity(),"MyRunsDialogFragment start",Toast.LENGTH_SHORT).show();
    }
    public void onStop() {
        super.onStop();
        Toast.makeText(getActivity(),"MyRunsDialogFragment stop",Toast.LENGTH_SHORT).show();
    }

    public void onPause() {
        super.onPause();
        Toast.makeText(getActivity(),"MyRunsDialogFragment pause",Toast.LENGTH_SHORT).show();

    }
    public void onResume() {
        super.onResume();
        Toast.makeText(getActivity(),"MyRunsDialogFragment resume",Toast.LENGTH_SHORT).show();
    }
}