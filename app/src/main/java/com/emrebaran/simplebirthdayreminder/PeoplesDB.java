package com.emrebaran.simplebirthdayreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mree on 12.11.2016.
 */

public class PeoplesDB extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dbPeoples";
    private static final String TABLE_PEOPLES = "tbPeoples";

    //Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SURNAME = "sur_number";
    private static final String KEY_BIRTH_DATE = "birth_date";
    private static final String KEY_CURRENT_AGE = "current_age";
    private static final String KEY_DAYS_LEFT = "days_left";

    public PeoplesDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PEOPLES +
                "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_SURNAME + " TEXT,"
                + KEY_BIRTH_DATE + " TEXT,"
                + KEY_CURRENT_AGE + " TEXT,"
                + KEY_DAYS_LEFT + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLES);
        onCreate(db);
    }


    // Adding new contact
    long addPeople(Peoples people) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, people.getName());
        values.put(KEY_SURNAME, people.getSurname());
        values.put(KEY_BIRTH_DATE, people.getBirthDate());
        values.put(KEY_CURRENT_AGE, people.getCurrentAge());
        values.put(KEY_DAYS_LEFT, people.getDaysLeft());

        long inserted_id;

        // Inserting Row
        inserted_id = db.insert(TABLE_PEOPLES, null, values);

        db.close(); // Closing database connection

        return inserted_id;

    }

    // Getting single people
    Peoples getPeople(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PEOPLES, new String[] { KEY_ID, KEY_NAME, KEY_SURNAME }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Peoples contact = new Peoples(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2));

        return contact;
    }


    // Getting All Peoples
    public List<Peoples> getAllPeoples() {
        List<Peoples> peoplesList = new ArrayList<Peoples>();
        // Select All Query Order By Days Left
        String selectQuery = "SELECT  * FROM " + TABLE_PEOPLES + " ORDER BY cast("+KEY_DAYS_LEFT +" AS INT)";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Peoples people = new Peoples();
                people.setID(Integer.parseInt(cursor.getString(0)));
                people.setName(cursor.getString(1));
                people.setSurname(cursor.getString(2));
                people.setBirthDate(cursor.getString(3));
                people.setCurrentAge(cursor.getString(4));
                people.setDaysLeft(cursor.getString(5));

                // Adding people to list
                peoplesList.add(people);
            } while (cursor.moveToNext());
        }


        db.close();

        return peoplesList;
    }


    //get row count
    public int getRowCount() {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_PEOPLES;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = cursor.getCount();

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        db.close();

        return count;
    }


    // Updating single people
    public int updatePeople(Peoples people,int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, people.getName());
        values.put(KEY_SURNAME, people.getSurname());
        values.put(KEY_BIRTH_DATE, people.getBirthDate());
        values.put(KEY_CURRENT_AGE, people.getCurrentAge());
        values.put(KEY_DAYS_LEFT, people.getDaysLeft());

        // updating row
        return db.update(TABLE_PEOPLES, values, KEY_ID + " = ?", new String[] { String.valueOf(id) });
    }


    public void updateAllPeoples() {

        String selectQuery = "SELECT  * FROM " + TABLE_PEOPLES + " ORDER BY cast("+KEY_DAYS_LEFT +" AS INT)";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Peoples people = new Peoples();

                ContentValues values = new ContentValues();
                values.put(KEY_CURRENT_AGE, people.calculateAge(cursor.getString(3)));
                values.put(KEY_DAYS_LEFT, people.calculateDays(cursor.getString(3)));


                db.update(TABLE_PEOPLES, values, KEY_ID + " = ?",
                        new String[] { String.valueOf(cursor.getString(0)) });

            } while (cursor.moveToNext());
        }


        db.close();

    }


    // Deleting single people
  /*  public void deletePeople(Peoples people) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PEOPLES, KEY_ID + " = ?",
                new String[] { String.valueOf(people.getID()) });
        db.close();
    } */

    // Deleting single people
    public void deletePeople(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PEOPLES, KEY_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }


}