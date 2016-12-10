package com.emrebaran.simplebirthdayreminder;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mree.simplebirthdayreminder.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by mree on 10.11.2016.
 */

public class PeoplesActivity extends AppCompatActivity implements BackupAndRestore.OnBackupListener {

    ListView listPeoples, listPeoplesHeader;

    DisplayMetrics metrics;

    //initialize date and time
    int year = 2010,month = 0,day = 1,hour = 13,minute= 30;

    int edit_year = 2010 , edit_month = 0, edit_day = 1;

    private static int SIMPLE_NOTIFICATION_ID=1;

    Integer[] array_ids;
    String[] array_names;
    String[] array_surnames;
    String[] array_birthdates;
    String[] array_ages;
    String[] array_days;

    String[] array_empty = {};

    PeoplesDB db= new PeoplesDB(this);

    private BackupAndRestore backupData;

    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();

    int txtChangeControl;


    //permissions
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private SharedPreferences permissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_peoples_activity);

        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        listPeoples = (ListView) findViewById(R.id.lvPeoples);


        listPeoplesHeader = (ListView) findViewById(R.id.lvPeoplesheader);

     //for header at top
        ViewGroup headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.header_peoples_list, listPeoplesHeader, false);
        listPeoplesHeader.addHeaderView(headerView);
        ListAdapterPeoples adapterx = new ListAdapterPeoples(PeoplesActivity.this, array_empty, array_empty, array_empty, array_empty, array_empty);
        listPeoplesHeader.setAdapter(adapterx);


        //for popup depending on resolution
        Display display = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        load();


        backupData = new BackupAndRestore(this);
        backupData.setOnBackupListener(this);


        listPeoples.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final Dialog infoDialog = new Dialog(PeoplesActivity.this);
                infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                infoDialog.setContentView(R.layout.layout_dialog_buttons);


                infoDialog.getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);



                ImageButton editPerson = (ImageButton) infoDialog.findViewById(R.id.popup_btn_edit);
                editPerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showPopupEdit(PeoplesActivity.this,array_ids[position],array_names[position],array_surnames[position],array_birthdates[position]);
                        infoDialog.dismiss();


                    }
                });


                ImageButton cancelAlarm = (ImageButton) infoDialog.findViewById(R.id.popup_btn_cancel_alarm);
                cancelAlarm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelExactAlarm(array_ids[position]);
                        infoDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.alarm_cancelled), Toast.LENGTH_SHORT).show();


                    }
                });

                ImageButton setAlarm = (ImageButton) infoDialog.findViewById(R.id.popup_btn_set_alarm);
                setAlarm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
                            public void onTimeSet(TimePicker view, int _hour, int _minute) {
                                    hour = _hour;
                                    minute = _minute;
                                    setAlarm(array_birthdates[position],hour+":"+minute,array_ids[position]);
                                    infoDialog.dismiss();

                            }
                        };


                        final TimePickerDialog timePickerDialog = new TimePickerDialog(
                                PeoplesActivity.this, timePickerListener,
                                hour, minute,true);


                        timePickerDialog.show();


                    }
                });


                ImageButton deletePerson = (ImageButton) infoDialog.findViewById(R.id.popup_btn_delete);
                deletePerson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder newDialog = new AlertDialog.Builder(PeoplesActivity.this);
                        newDialog.setMessage(getString(R.string.delete_person));
                        newDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                db.deletePeople(array_ids[position]);
                                cancelExactAlarm(array_ids[position]);
                                load();
                                dialog.dismiss();
                                infoDialog.dismiss();

                            }
                        });
                        newDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                dialog.cancel();
                            }
                        });
                        newDialog.show();


                    }
                });

                infoDialog.show();



            }
        });

        listPeoples.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                showHoroscope(array_birthdates[position]);

                return true;

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(PeoplesActivity.this);

            }
        });

    }

    private void load() {


        int j = db.getRowCount();

        if(j>0) {

            List<Peoples> contacts = db.getAllPeoples();

            array_ids = new Integer[j];
            array_names = new String[j];
            array_surnames = new String[j];
            array_birthdates = new String[j];
            array_ages = new String[j];
            array_days = new String[j];

            int i = -1;
            for (Peoples p : contacts) {
                i++;

                array_ids[i] = p.getID();
                array_names[i] = p.getName();
                array_surnames[i] = p.getSurname();
                array_birthdates[i] = p.getBirthDate();
                array_ages[i] = p.getCurrentAge();
                array_days[i] = p.getDaysLeft();
            }

            ListAdapterPeoples adapter = new ListAdapterPeoples(PeoplesActivity.this, array_names, array_surnames, array_ages, array_birthdates, array_days);
            listPeoples.setAdapter(adapter);
        }
        else
        {

            ListAdapterPeoples adapter = new ListAdapterPeoples(PeoplesActivity.this, array_empty, array_empty, array_empty, array_empty, array_empty);
            listPeoples.setAdapter(adapter);
        }
    }



    private PopupWindow pw;
    private void showPopup(final Activity context) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.layout_add_new, (ViewGroup) findViewById(R.id.popup));

            float popupWidth = 350*metrics.scaledDensity;
            float popupHeight = 260*metrics.scaledDensity;

            pw = new PopupWindow(context);
            pw.setContentView(layout);
            pw.setWidth((int)popupWidth);
            pw.setHeight((int)popupHeight);
            pw.setFocusable(true);

            Point p = new Point();
            p.x = 50;
            p.y = 50;

            int OFFSET_X = -50;
            int OFFSET_Y = (int)(90*metrics.scaledDensity);


            pw.showAtLocation(layout, Gravity.TOP, p.x + OFFSET_X, p.y + OFFSET_Y);


            final EditText edtName= (EditText) layout.findViewById(R.id.popup_edt_name);
            final EditText edtSurname= (EditText) layout.findViewById(R.id.popup_edt_surname);
            final TextView txtDate= (TextView) layout.findViewById(R.id.popup_txt_date);
            final TextView txtTime= (TextView) layout.findViewById(R.id.popup_txt_time);
            final Switch swAlarm = (Switch)layout.findViewById(R.id.popup_switch_alarm);

            txtTime.setClickable(false);

            swAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked){
                        txtTime.setClickable(true);
                        txtTime.setTextColor(getResources().getColor(R.color.black));
                    }else{

                        txtTime.setClickable(false);
                        txtTime.setTextColor(getResources().getColor(R.color.gray));

                    }

                }
            });



            final Calendar c = Calendar.getInstance();


            txtDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        DatePickerDialog dpd = new DatePickerDialog(PeoplesActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int _year, int _month, int _day) {

                                        c.set(_year, _month, _day);
                                        String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                                        txtDate.setText(date);


                                        year = c.get(Calendar.YEAR);
                                        month = c.get(Calendar.MONTH);
                                        day = c.get(Calendar.DAY_OF_MONTH);

                                    }
                                }, year, month, day);

                        Calendar d = Calendar.getInstance();
                        d.add(Calendar.MONTH, 1);

                        dpd.show();

                    }
            });


            txtTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (swAlarm.isChecked()) {

                    TimePickerDialog tpd = new TimePickerDialog(PeoplesActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int _hour, int _minute) {
                            txtTime.setText( _hour + ":" + _minute);

                            hour = _hour;
                            minute = _minute;

                        }
                    }, hour, minute, true);


                    tpd.show();
                    }

                }
            });


            ImageButton close= (ImageButton) layout.findViewById(R.id.popup_btn_close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();

                }
            });

            ImageButton save= (ImageButton) layout.findViewById(R.id.popup_btn_save);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(edtName.getText().toString().length()<1)
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.warning_name),Toast.LENGTH_SHORT).show();
                    else if(edtSurname.getText().toString().length()<1)
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.warning_surname),Toast.LENGTH_SHORT).show();
                    else
                    {
                        long inserted_id;

                    inserted_id = db.addPeople(new Peoples(edtName.getText().toString(), edtSurname.getText().toString(),txtDate.getText().toString()));
                    load();

                    if(swAlarm.isChecked())
                    {
                        setAlarm(txtDate.getText().toString(),txtTime.getText().toString(),inserted_id);
                    }

                    pw.dismiss();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private PopupWindow pwe;
    private void showPopupEdit(final Activity context, final int update_id, final String name, final String sur_name, final String birth_date) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.layout_edit, (ViewGroup) findViewById(R.id.popup));

            float popupWidth = 350*metrics.scaledDensity;
            float popupHeight = 210*metrics.scaledDensity;

            pwe = new PopupWindow(context);
            pwe.setContentView(layout);
            pwe.setWidth((int)popupWidth);
            pwe.setHeight((int)popupHeight);
            pwe.setFocusable(true);

            Point p = new Point();
            p.x = 50;
            p.y = 50;

            int OFFSET_X = -50;
            int OFFSET_Y = (int)(90*metrics.scaledDensity);


            pwe.showAtLocation(layout, Gravity.TOP, p.x + OFFSET_X, p.y + OFFSET_Y);


            final EditText edtName= (EditText) layout.findViewById(R.id.popup_edt_name);
            final EditText edtSurname= (EditText) layout.findViewById(R.id.popup_edt_surname);
            final TextView txtDate= (TextView) layout.findViewById(R.id.popup_txt_date);

            final Calendar c = Calendar.getInstance();

            edtName.setText(name);
            edtSurname.setText(sur_name);
            txtDate.setText(birth_date);

            String[] dates = birth_date.split("/");
            edit_day = Integer.parseInt(dates[0]);
            edit_month = Integer.parseInt(dates[1])-1;
            edit_year = Integer.parseInt(dates[2]);

            txtChangeControl = 0;

            txtDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DatePickerDialog dpd = new DatePickerDialog(PeoplesActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int _year, int _month, int _day) {

                                    c.set(_year, _month, _day);
                                    String date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                                    txtDate.setText(date);


                                    edit_year = c.get(Calendar.YEAR);
                                    edit_month = c.get(Calendar.MONTH);
                                    edit_day = c.get(Calendar.DAY_OF_MONTH);

                                }
                            }, edit_year, edit_month, edit_day);

                    Calendar d = Calendar.getInstance();
                    d.add(Calendar.MONTH, 1);

                    dpd.show();

                }
            });


            txtDate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {


                    txtChangeControl++;

                }
            });

            ImageButton close= (ImageButton) layout.findViewById(R.id.popup_btn_close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwe.dismiss();

                }
            });

            ImageButton save= (ImageButton) layout.findViewById(R.id.popup_btn_save);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(edtName.getText().toString().length()<1)
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.warning_name),Toast.LENGTH_SHORT).show();
                    else if(edtSurname.getText().toString().length()<1)
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.warning_surname),Toast.LENGTH_SHORT).show();
                    else
                    {
                        long updated_id;

                        updated_id = db.updatePeople(new Peoples(edtName.getText().toString(), edtSurname.getText().toString(),txtDate.getText().toString()),update_id);

                        if(updated_id!=0) {

                            if(txtChangeControl>0) {
                                cancelExactAlarm((int) updated_id);
                                Toast.makeText(getApplicationContext(), getString(R.string.alarm_cancelled), Toast.LENGTH_SHORT).show();
                            }


                            load();
                            pwe.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.edit_person_warning),Toast.LENGTH_SHORT).show();

                        }

                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setAlarm(String my_date, String my_time, long alarm_id){
        String[] dates = my_date.split("/");
        int my_day = Integer.parseInt(dates[0]);
        int my_month = Integer.parseInt(dates[1])-1;
        int my_year = Integer.parseInt(dates[2]);


        String[] hours = my_time.toString().split(":");
        int my_hour = Integer.parseInt(hours[0]);
        int my_min = Integer.parseInt(hours[1]);

        Calendar current = Calendar.getInstance();

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, my_day);
        cal.set(Calendar.MONTH, my_month);
        cal.set(Calendar.HOUR_OF_DAY, my_hour);
        cal.set(Calendar.MINUTE, my_min);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 00);


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy hh:mm:ss:ss ");

        if(cal.compareTo(current) <= 0){

            int new_year = Calendar.getInstance().get(Calendar.YEAR)+1;
            cal.set(Calendar.YEAR, new_year);

            setExactAlarm(cal,alarm_id);

        }else{
            setExactAlarm(cal,alarm_id);
        }
    }



    private void setExactAlarm(Calendar targetCal, long alarm_id){

        Toast.makeText(getApplicationContext(),getString(R.string.alarm_set),Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getBaseContext(), AlarmManagerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), (int)alarm_id, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }


    public void cancelExactAlarm(int alarm_id){
        Context context = this.getApplicationContext();
        AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();

        if(alarm != null){
            alarm.CancelAlarm(context,alarm_id);
        }else{
            Toast.makeText(context, getString(R.string.alarm_error), Toast.LENGTH_SHORT).show();
        }
    }

    private PopupWindow pwa;
    private void showPopupAbout(final Activity context) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.layout_about, (ViewGroup) findViewById(R.id.popup_1));

            float popupWidth = 330*metrics.scaledDensity;
            float popupHeight = 500*metrics.scaledDensity;

            pwa = new PopupWindow(context);
            pwa.setContentView(layout);
            pwa.setWidth((int)popupWidth);
            pwa.setHeight((int)popupHeight);
            pwa.setFocusable(true);

            Point p = new Point();
            p.x = 50;
            p.y = 50;

            int OFFSET_X = -50;
            int OFFSET_Y = (int)(50*metrics.scaledDensity);


            pwa.showAtLocation(layout, Gravity.TOP, p.x + OFFSET_X, p.y + OFFSET_Y);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    void showHoroscope(String my_date){

        String[] dates = my_date.split("/");
        int my_day = Integer.parseInt(dates[0]);
        int my_month = Integer.parseInt(dates[1]);
        int my_year = Integer.parseInt(dates[2]);

        if ((my_month==1 && my_day<=20)||(my_month==12 && my_day>=21)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope1),Toast.LENGTH_LONG).show();
        }else if((my_month==1 && my_day>=21)||(my_month==2 && my_day<=19)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope2),Toast.LENGTH_LONG).show();
        }else if((my_month==2 && my_day>=20)||(my_month==3 && my_day<=20)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope3),Toast.LENGTH_LONG).show();
        }else if((my_month==3 && my_day>=21)||(my_month==4 && my_day<=20)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope4),Toast.LENGTH_LONG).show();
        }else if((my_month==4 && my_day>=21)||(my_month==5 && my_day<=20)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope5),Toast.LENGTH_LONG).show();
        }else if((my_month==5 && my_day>=21)||(my_month==6 && my_day<=21)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope6),Toast.LENGTH_LONG).show();
        }else if((my_month==6 && my_day>=22)||(my_month==7 && my_day<=22)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope7),Toast.LENGTH_LONG).show();
        }else if((my_month==7 && my_day>=23)||(my_month==8 && my_day<=23)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope8),Toast.LENGTH_LONG).show();
        }else if((my_month==8 && my_day>=24)||(my_month==9 && my_day<=23)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope9),Toast.LENGTH_LONG).show();
        }else if((my_month==9 && my_day>=24)||(my_month==10 && my_day<=23)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope10),Toast.LENGTH_LONG).show();
        }else if((my_month==10 && my_day>=24)||(my_month==11 && my_day<=22)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope11),Toast.LENGTH_LONG).show();
        }else if((my_month==11 && my_day>=23)||(my_month==12 && my_day<=21)) {
            Toast.makeText(getApplicationContext(),getString(R.string.horoscope12),Toast.LENGTH_LONG).show();
        }
    }


    public void checkPermissions(){
            if(ActivityCompat.checkSelfPermission(PeoplesActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(PeoplesActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(PeoplesActivity.this,permissionsRequired[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(PeoplesActivity.this,permissionsRequired[1])){
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(PeoplesActivity.this);
                    builder.setTitle(getString(R.string.permissions_title));
                    builder.setMessage(getString(R.string.permissions_message));
                    builder.setPositiveButton(getString(R.string.permissions_grant), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(PeoplesActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                        }
                    });
                    builder.setNegativeButton(getString(R.string.permissions_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                    //Previously Permission Request was cancelled with 'Dont Ask Again',
                    // Redirect to Settings after showing Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(PeoplesActivity.this);
                    builder.setTitle(getString(R.string.permissions_title));
                    builder.setMessage(getString(R.string.permissions_message));
                    builder.setPositiveButton(getString(R.string.permissions_grant), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton(getString(R.string.permissions_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }  else {
                    //just request the permission
                    ActivityCompat.requestPermissions(PeoplesActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                }

                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(permissionsRequired[0],true);
                editor.commit();
            } else {
                //You already have the permission, just go ahead.
                proceedAfterPermission();
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if(allgranted)
            {
                proceedAfterPermission();
            }
            else if(ActivityCompat.shouldShowRequestPermissionRationale(PeoplesActivity.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(PeoplesActivity.this,permissionsRequired[1]))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(PeoplesActivity.this);
                builder.setTitle(getString(R.string.permissions_title));
                builder.setMessage(getString(R.string.permissions_message));
                builder.setPositiveButton(getString(R.string.permissions_grant), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(PeoplesActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(getString(R.string.permissions_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
            else
            {
                Toast.makeText(getBaseContext(),getString(R.string.permissions_unable),Toast.LENGTH_LONG).show();
            }
        }
    }


    private void proceedAfterPermission() {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(PeoplesActivity.this);
        newDialog.setMessage(getString(R.string.backup_question));
        newDialog.setPositiveButton(getString(R.string.exportDb), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){

                if(db.getRowCount()>0)
                    backupData.exportToSD();
                else
                    Toast.makeText(getApplicationContext(),getString(R.string.export_warning),Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            }
        });
        newDialog.setNegativeButton(getString(R.string.importDb), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){

                backupData.importFromSD();

                dialog.cancel();
            }
        });
        newDialog.show();
    }


    @Override
    public void onFinishExport(String error) {
        String notify = error;
        if (error == null) {
            notify = getString(R.string.backup_export_success);
        }
        Toast.makeText(getApplicationContext(), notify, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFinishImport(String error) {
        String notify = error;
        if (error == null) {
            notify = getString(R.string.backup_import_success);
            load();
        }
        Toast.makeText(getApplicationContext(), notify, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_db) {

            checkPermissions();


        }
        else if (id == R.id.action_help) {

            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.action_help_message), Toast.LENGTH_LONG);
            TextView vv = (TextView) toast.getView().findViewById(android.R.id.message);
            if( vv != null) vv.setGravity(Gravity.CENTER);
            toast.show();

            return true;
        }

        else if (id == R.id.action_about) {
            showPopupAbout(PeoplesActivity.this);

            return true;
        }
        else if (id == R.id.action_rate) {

            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //double click to exit
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mHandler != null) { mHandler.removeCallbacks(mRunnable); }
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.app_exit, Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(mRunnable, 1500);
    }

}