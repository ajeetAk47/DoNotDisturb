package com.ak47.doNotDisturb.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ak47.doNotDisturb.Model.Contact;
import com.ak47.doNotDisturb.Model.Word;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactsManager";
    private static final String TABLE_CONTACTS_CALL = "contacts";
    private static final String TABLE_CONTACTS_WHATSAPP = "whatsappContacts";
    private static final String TABLE_WORDS = "word";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_WORD = "word";


    public DatabaseHandler(@Nullable Context context) {
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
        String CREATE_CONTACTS_TABLE3 = "CREATE TABLE " + TABLE_WORDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_WORD + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE1);
        db.execSQL(CREATE_CONTACTS_TABLE2);
        db.execSQL(CREATE_CONTACTS_TABLE3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        // Create tables again
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_CALL);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_WHATSAPP);
        onCreate(db);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        onCreate(db);
    }

    public void addContact(Contact contact, String TableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName()); // Contact Name
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
        values.put(KEY_NAME, contact.getName());
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

    /**
     * Methods For Word table
     */


    public void addWord(Word word) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word.getWord()); // Word
        // Inserting Row
        db.insert(TABLE_WORDS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection

    }

    Word getWord(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_WORDS, new String[]{KEY_ID,
                        KEY_WORD}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Word word = new Word(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        // return Word
        return word;
    }

    // code to get all contacts in a list view
    public List<Word> getAllWords() {
        List<Word> wordList = new ArrayList<Word>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_WORDS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Word word = new Word();
                word.setId(Integer.parseInt(cursor.getString(0)));
                word.setWord(cursor.getString(1));
                // Adding word to list
                wordList.add(word);
            } while (cursor.moveToNext());
        }

        // return contact list
        return wordList;
    }

    // code to update the single contact
    public int updateWord(Word word) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word.getWord());

        // updating row
        return db.update(TABLE_WORDS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(word.getId())});
    }

    public void deleteWord(Word word) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORDS, KEY_ID + " = ?",
                new String[]{String.valueOf(word.getId())});
        db.close();
    }

    public void deleteWord(String word) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORDS, KEY_WORD + " = ?",
                new String[]{word});
        db.close();
    }


    // Getting Words Count
    public int getWordsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_WORDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        return cursor.getCount();
    }
}
