package kangwon.cse.jck.myruns3;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.settings_preferences);
    }
    /*Life cycle 알아보기위해서 onStart, onResume, onPause, onStop 추가!*/
    public void onStart() {
        super.onStart();
        Toast.makeText(getActivity(),"SettingsFragment start",Toast.LENGTH_SHORT).show();
    }
    public void onStop() {
        super.onStop();
        Toast.makeText(getActivity(),"SettingsFragment stop",Toast.LENGTH_SHORT).show();
    }

    public void onPause() {
        super.onPause();
        Toast.makeText(getActivity(),"SettingsFragment pause",Toast.LENGTH_SHORT).show();

    }
    public void onResume() {
        super.onResume();
        Toast.makeText(getActivity(),"SettingsFragment resume",Toast.LENGTH_SHORT).show();
    }
}
