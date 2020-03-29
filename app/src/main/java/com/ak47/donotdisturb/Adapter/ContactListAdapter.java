package com.ak47.donotdisturb.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ak47.donotdisturb.Model.Contact;
import com.ak47.donotdisturb.R;

import java.util.List;

public class ContactListAdapter extends ArrayAdapter<Contact> {
    private int listItemLayout;

    public ContactListAdapter(Context context, int listview_contacts, List contactsList) {
        super(context, listview_contacts, contactsList);
        listItemLayout = listview_contacts;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Contact contact = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(listItemLayout, parent, false);
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.number = convertView.findViewById(R.id.number);
            convertView.setTag(viewHolder); // view lookup cache stored in tag
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(contact.getWord());
        viewHolder.number.setText(contact.getPhoneNumber());

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView number;
    }
}
