package com.emrebaran.simplebirthdayreminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mree.simplebirthdayreminder.R;

/**
 * Created by mree on 14.11.2016.
 */

public class Intro extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_intro);

        PeoplesDB db= new PeoplesDB(this);


        db.updateAllPeoples();


        Thread intro = new Thread(){
            public void run(){
                try {

                    sleep(1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {

                    Intent nextIntent = new Intent(Intro.this,PeoplesActivity.class);
                    startActivity(nextIntent);
                    finish();
                }
            }
        };
        intro.start();

    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
            return;
    }

}