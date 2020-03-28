package com.ak47.donotdisturb.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ak47.donotdisturb.Model.Contact;

import java.util.ArrayList;
import java.util.List;

public class CallDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactsManager";
    private static final String TABLE_CONTACTS_CALL = "contacts";
    private static final String TABLE_CONTACTS_WHATSAPP = "whatsappcontacts";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";

    public CallDatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE1 = "CREATE TABLE " + TABLE_CONTACTS_CALL + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT" + ")";
        String CREATE_CONTACTS_TABLE2 = "CREATE TABLE " + TABLE_CONTACTS_WHATSAPP + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE1);
        db.execSQL(CREATE_CONTACTS_TABLE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_CALL);
        // Create tables again
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_WHATSAPP);
        onCreate(db);
    }

    public void addContact(Contact contact, String TableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getWord()); // Contact Name
        values.put(KEY_PH_NO, contact.getPhoneNumber()); // Contact Phone

        // Inserting Row
        db.insert(TableName, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection

    }

    Contact getContact(int id, String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(tableName, new String[]{KEY_ID,
                        KEY_NAME, KEY_PH_NO}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        // return contact
        return contact;
    }

    // code to get all contacts in a list view
    public List<Contact> getAllContacts(String tableName) {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + tableName;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setPhoneNumber(cursor.getString(2));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // code to update the single contact
    public int updateContact(Contact contact, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getWord());
        values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        return db.update(tableName, values, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
    }

    public void deleteContact(Contact contact, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
        db.close();
    }

    public void deleteContact(String number, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, KEY_PH_NO + " = ?",
                new String[]{number});
        db.close();
    }

    // Getting contacts Count
    public int getContactsCount(String tableName) {
        String countQuery = "SELECT  * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        return cursor.getCount();
    }
}
