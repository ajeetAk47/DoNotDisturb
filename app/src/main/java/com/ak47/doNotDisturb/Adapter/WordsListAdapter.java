package com.ak47.doNotDisturb.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ak47.doNotDisturb.Model.Word;
import com.ak47.doNotDisturb.R;

import java.util.List;

public class WordsListAdapter extends ArrayAdapter<Word> {
    private int listItemLayout;

    public WordsListAdapter(Context context, int listview_words, List wordsList) {
        super(context, listview_words, wordsList);
        listItemLayout = listview_words;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Word word = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(listItemLayout, parent, false);
            viewHolder.word = convertView.findViewById(R.id.word);
            convertView.setTag(viewHolder); // view lookup cache stored in tag
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        assert word != null;
        viewHolder.word.setText(word.getWord());
        return convertView;

    }

    private static class ViewHolder {
        TextView word;
    }
}

