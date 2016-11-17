package com.mree.simplebirthdayreminder;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by mree on 12.11.2016.
 */

public class Peoples {

    //private variables
    int _id;
    String _name;
    String _sur_name;
    String _birth_date;
    String _current_age;
    String _days_left;

    // Empty constructor
    public Peoples(){

    }
    // constructor
    public Peoples(int id, String name, String sur_name, String birth_date){
        this._id = id;
        this._name = name;
        this._sur_name = sur_name;
        this._birth_date = birth_date;
    }

    // constructor
    public Peoples(int id, String name, String sur_name){
        this._id = id;
        this._name = name;
        this._sur_name = sur_name;
    }

    public Peoples(String name, String sur_name){
        this._name = name;
        this._sur_name = sur_name;
    }

    public Peoples(String name, String sur_name, String birth_date){
        this._name = name;
        this._sur_name = sur_name;
        this._birth_date = birth_date;
        this._current_age = String.valueOf(calculateAge(birth_date));
        this._days_left = calculateDays(birth_date);

    }




    public int calculateAge(String my_date)
    {
        String[] parts = my_date.split("/");
        int my_day = Integer.parseInt(parts[0]);
        int my_month = Integer.parseInt(parts[1])-1;
        int my_year = Integer.parseInt(parts[2]);

        Date now = new Date();

        Date date1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, my_day);
        calendar.set(Calendar.MONTH, my_month);
        calendar.set(Calendar.YEAR, my_year);
        date1 = calendar.getTime();

        Calendar c1 = Calendar.getInstance();
        c1.setTime(now);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(date1);

        int yearDiff = c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);

        return yearDiff;

    }


    public String calculateDays(String my_date)
    {
        String[] parts = my_date.split("/");
        int my_day = Integer.parseInt(parts[0]);
        int my_month = Integer.parseInt(parts[1])-1;
        int my_year = Integer.parseInt(parts[2]);

        Date now = new Date();

        Date date1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, my_day);
        calendar.set(Calendar.MONTH, my_month);
        calendar.set(Calendar.YEAR, my_year);
        date1 = calendar.getTime();

        Calendar c1 = Calendar.getInstance();
        c1.setTime(now);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(date1);

        int monthDiff = c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);

        int dayDiff = c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);

        String gun = "0";


        if (monthDiff==0) {
            if (dayDiff < 0) {

                dayDiff *=-1;
                gun = String.valueOf(dayDiff);

            } else if (dayDiff > 0) {

                calendar.set(Calendar.YEAR, c1.get(Calendar.YEAR)+1);
                date1 = calendar.getTime();

                gun = String.valueOf((date1.getTime() - now.getTime())/(24*60*60*1000));
            }
            else if (dayDiff == 0) {
                gun = "0";
            }
        }
        if (monthDiff<0) {
            calendar.set(Calendar.YEAR, c1.get(Calendar.YEAR));
            date1 = calendar.getTime();

            gun = String.valueOf((date1.getTime() - now.getTime())/(24*60*60*1000));
        }
        if (monthDiff>0) {
            calendar.set(Calendar.YEAR, c1.get(Calendar.YEAR)+1);
            date1 = calendar.getTime();

            gun = String.valueOf((date1.getTime() - now.getTime())/(24*60*60*1000));
        }

        return gun;

    }



    public int getID(){
        return this._id;
    }
    public void setID(int id){
        this._id = id;
    }

    public String getName(){
        return this._name;
    }
    public void setName(String name){
        this._name = name;
    }

    public String getSurname(){
        return this._sur_name;
    }
    public void setSurname(String sur_name){
        this._sur_name = sur_name;
    }

    public String getBirthDate(){
        return this._birth_date;
    }
    public void setBirthDate(String birth_date){
        this._birth_date = birth_date;
    }

    public String getCurrentAge(){
        return this._current_age;
    }
    public void setCurrentAge(String current_age){
        this._current_age = current_age;
    }

    public String getDaysLeft(){
        return this._days_left;
    }
    public void setDaysLeft(String days_left){ this._days_left = days_left; }


}