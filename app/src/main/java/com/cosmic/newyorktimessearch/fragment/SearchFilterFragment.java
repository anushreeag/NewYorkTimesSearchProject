package com.cosmic.newyorktimessearch.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.cosmic.newyorktimessearch.R;
import com.cosmic.newyorktimessearch.activity.NYTSearchActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by anushree on 9/19/2017.
 */

public class SearchFilterFragment extends DialogFragment {
    Context mCtx;

    Calendar myCalendar;
    EditText begin_date;
    Spinner  sort_spin;
    CheckBox arts;
    CheckBox sports;
    CheckBox fashion;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Button save;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCtx = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.filter_layout,container,false);

        begin_date = (EditText) view.findViewById(R.id.edit_begin);
        begin_date.setFocusable(false);
        sort_spin = (Spinner) view.findViewById(R.id.spinner);
        arts = (CheckBox) view.findViewById(R.id.arts_box);
        sports = (CheckBox) view.findViewById(R.id.sport_box);
        fashion = (CheckBox) view.findViewById(R.id.fashion_box);
        save = (Button) view.findViewById(R.id.save);

        pref = mCtx.getSharedPreferences(NYTSearchActivity.PREFERENCES,Context.MODE_PRIVATE);
        editor = pref.edit();



        myCalendar = Calendar.getInstance();
        begin_date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                DatePickerDialog pickerDialog = new DatePickerDialog(mCtx, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                pickerDialog.show();

            }
        });


        ArrayList<String> sortOrderList = new ArrayList<>();
        sortOrderList.add(mCtx.getResources().getString(R.string.old));
        sortOrderList.add(mCtx.getResources().getString(R.string.newest));
        ArrayAdapter<String> sortAdp = new ArrayAdapter<String>(mCtx,android.R.layout.simple_spinner_dropdown_item,sortOrderList);
        sort_spin.setAdapter(sortAdp);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFilter();
            }
        });

        begin_date.setText(pref.getString(NYTSearchActivity.BEGIN_DATE,""));
        sort_spin.setSelection(pref.getInt(NYTSearchActivity.SORT_ORDER,0));
        arts.setChecked(pref.getBoolean(NYTSearchActivity.ARTS,false));
        sports.setChecked(pref.getBoolean(NYTSearchActivity.SPORTS,false));
        fashion.setChecked(pref.getBoolean(NYTSearchActivity.FASHION,false));

        return view;
    }


    private void updateLabel() {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        begin_date.setText(sdf.format(myCalendar.getTime()));
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void saveFilter(){
        editor.putString(NYTSearchActivity.BEGIN_DATE,begin_date.getText().toString());
        //Log.i(NYTSearchActivity.TAG,"Begin date = "+begin_date.getText().toString());
        editor.putInt(NYTSearchActivity.SORT_ORDER,sort_spin.getSelectedItemPosition());
        //Log.i(NYTSearchActivity.TAG,""+"Selected spinner = "+sort_spin.getSelectedItemPosition());
        editor.putBoolean(NYTSearchActivity.ARTS,arts.isChecked());
        editor.putBoolean(NYTSearchActivity.SPORTS,sports.isChecked());
        editor.putBoolean(NYTSearchActivity.FASHION,fashion.isChecked());
        //Log.i(NYTSearchActivity.TAG,"ARTS = "+arts.isChecked()+" sPORTS = "+sports.isChecked()+" fashion = "+fashion.isChecked());
        editor.commit();
        dismiss();
    }
}
