package com.ak47.donotdisturb.Fragment;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.ak47.donotdisturb.Adapter.ContactListAdapter;
import com.ak47.donotdisturb.Database.ContactsDatabaseHandler;
import com.ak47.donotdisturb.Model.Contact;
import com.ak47.donotdisturb.R;

import java.util.ArrayList;
import java.util.List;


public class WhatsAppDialogFragment extends androidx.fragment.app.DialogFragment {
    public static final String TAG = "Save WhatsApp Contact";
    private static final String TABLE_CONTACTS_WHATSAPP = "whatsappcontacts";
    private ListView listView;
    private List<Contact> contacts;
    private List<Contact> nameList = new ArrayList<>();
    private ContactListAdapter contactListAdapter;
    private Toolbar toolbar;
    private TextView noContact;

    public static WhatsAppDialogFragment display(FragmentManager fragmentManager) {
        WhatsAppDialogFragment addDialogFragment = new WhatsAppDialogFragment();
        addDialogFragment.show(fragmentManager, TAG);
        return addDialogFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(CallDialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.add_fragment_dialog, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        listView = view.findViewById(R.id.contact_list);
        noContact = view.findViewById(R.id.no_contact);

        contactListAdapter = new ContactListAdapter(getContext(), R.layout.listview_contacts, contactsArrayList());
        listView.setAdapter(contactListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                Contact contact = contacts.get(position);
                new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                        .setTitle("Confirm")
                        .setMessage("Do you want to Delete this Contact?")
                        .setCancelable(false)
                        .setNeutralButton(" No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss
                            }
                        })
                        .setPositiveButton(" Yes ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(getContext(), contact.getPhoneNumber(), Toast.LENGTH_SHORT).show();
                                deleteByNumberAndUpdateView(position);
                            }
                        }).show();
            }
        });
        return view;
    }

    private void deleteByNumberAndUpdateView(int position) {

        ContactsDatabaseHandler db = new ContactsDatabaseHandler(getContext());
        String phoneNumber = nameList.get(position).getPhoneNumber();
        db.deleteContact(phoneNumber, TABLE_CONTACTS_WHATSAPP);
        nameList.remove(position);
        Log.e(TAG, "Delete Contact " + phoneNumber);
        if (db.getContactsCount(TABLE_CONTACTS_WHATSAPP) == 0) {
            listView.setVisibility(View.GONE);
            noContact.setVisibility(View.VISIBLE);
        }
        contactListAdapter.notifyDataSetChanged();


    }


    private ArrayList<Contact> contactsArrayList() {
        ContactsDatabaseHandler db = new ContactsDatabaseHandler(getContext());
        contacts = db.getAllContacts(TABLE_CONTACTS_WHATSAPP);
        if (db.getContactsCount(TABLE_CONTACTS_WHATSAPP) == 0) {
            listView.setVisibility(View.GONE);
            noContact.setVisibility(View.VISIBLE);
        }
        for (Contact cn : contacts) {
            nameList.add(new Contact(cn.getWord(), cn.getPhoneNumber()));
        }
        return (ArrayList<Contact>) nameList;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WhatsAppDialogFragment.this.dismiss();
            }
        });
        toolbar.setTitle("WhatsApp Contacts");
        toolbar.inflateMenu(R.menu.save_data_dialog);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
//                                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 1);
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Cursor cursor = null;
        try {
            String phoneNo = null;
            String name = null;
            Uri uri = data.getData();
            cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            name = cursor.getString(nameIndex);
            phoneNo = cursor.getString(phoneIndex);
            Log.e(TAG, "Got a result: " + name + " " + phoneNo);
            insertContactInfo(name, phoneNo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertContactInfo(String name, String number) {
        Log.d(TAG, "Inserting .." + number);
        ContactsDatabaseHandler db = new ContactsDatabaseHandler(getContext());
        if (checkExistenceInDataBase(number)) {
            Toast.makeText(getActivity(), "Number Already Exist ", Toast.LENGTH_SHORT).show();
        } else if (number.charAt(0) == '+') {
            number = number.replace(" ", "");
            db.addContact(new Contact(name, number), TABLE_CONTACTS_WHATSAPP);
            if (db.getContactsCount(TABLE_CONTACTS_WHATSAPP) > 0) {
                listView.setVisibility(View.VISIBLE);
                noContact.setVisibility(View.GONE);
            }
            nameList.add(new Contact(name, number));
            contactListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "Invalid Format");
            new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                    .setTitle("Alert")
                    .setMessage("Contact number must have proper Country Code Otherwise You Will Not able to Add")
                    .setCancelable(false)
                    .setPositiveButton(" Got it", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }

    private boolean checkExistenceInDataBase(String number) {
        ContactsDatabaseHandler db = new ContactsDatabaseHandler(getContext());
        number = number.replaceAll(" ", "");
        List<Contact> contacts = db.getAllContacts(TABLE_CONTACTS_WHATSAPP);
        for (Contact contactList : contacts) {
            if (contactList.getPhoneNumber().equals(number)) {
                return true;
            }
        }
        return false;
    }

    public String getContactDisplayNameByNumber(String number, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, null, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            } else {
                name = "Unknown number";
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return name;
    }

}
