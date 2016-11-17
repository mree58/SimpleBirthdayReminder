package com.example.mree.simplebirthdayreminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by mree on 14.11.2016.
 */

public class Intro extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_intro);




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

}