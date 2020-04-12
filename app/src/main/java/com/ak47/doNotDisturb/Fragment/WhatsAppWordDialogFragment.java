package com.ak47.doNotDisturb.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.ak47.doNotDisturb.Adapter.WordsListAdapter;
import com.ak47.doNotDisturb.Database.DatabaseHandler;
import com.ak47.doNotDisturb.Model.Word;
import com.ak47.doNotDisturb.R;

import java.util.ArrayList;
import java.util.List;

public class WhatsAppWordDialogFragment extends androidx.fragment.app.DialogFragment {
    public static final String TAG = "Save Words";
    private ListView listView;
    private List<Word> word;
    private List<Word> wordList = new ArrayList<>();
    private WordsListAdapter wordsListAdapter;
    private Toolbar toolbar;
    private TextView noWords;

    public static WhatsAppWordDialogFragment display(FragmentManager fragmentManager) {
        WhatsAppWordDialogFragment whatsAppWordDialogFragment = new WhatsAppWordDialogFragment();
        whatsAppWordDialogFragment.show(fragmentManager, TAG);
        return whatsAppWordDialogFragment;
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
        View view = inflater.inflate(R.layout.fragment_whats_app_word_dialog, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        listView = view.findViewById(R.id.word_list);
        noWords = view.findViewById(R.id.no_word);

        wordsListAdapter = new WordsListAdapter(getContext(), R.layout.listview_words, wordsArrayList());
        listView.setAdapter(wordsListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                Contact contact = contacts.get(position);
                new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                        .setTitle("Confirm")
                        .setMessage("Do you want to Delete this Word?")
                        .setCancelable(false)
                        .setNeutralButton(" No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss
                            }
                        })
                        .setPositiveButton(" Yes ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteByWordAndUpdateView(position);
                            }
                        }).show();
            }
        });


        return view;
    }

    private void deleteByWordAndUpdateView(int position) {

        DatabaseHandler db = new DatabaseHandler(getContext());
        String word = wordList.get(position).getWord();
        db.deleteWord(word);
        wordList.remove(position);
        Log.e(TAG, "Delete Word " + word);
        if (db.getWordsCount() == 0) {
            listView.setVisibility(View.GONE);
            noWords.setVisibility(View.VISIBLE);
        }
        wordsListAdapter.notifyDataSetChanged();
    }

    private ArrayList<Word> wordsArrayList() {
        DatabaseHandler db = new DatabaseHandler(getContext());
        word = db.getAllWords();
        if (db.getWordsCount() == 0) {
            listView.setVisibility(View.GONE);
            noWords.setVisibility(View.VISIBLE);
        }
        for (Word wd : word) {
            wordList.add(new Word(wd.getWord()));
        }
        return (ArrayList<Word>) wordList;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WhatsAppWordDialogFragment.this.dismiss();
            }
        });
        toolbar.setTitle("Words");
        toolbar.inflateMenu(R.menu.save_word);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                final EditText wordEditText = new EditText(getContext());

                new androidx.appcompat.app.AlertDialog.Builder(getContext(), R.style.AlertDialogStyle)
                        .setTitle("Add Words")
                        .setView(wordEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(wordEditText.getText());
                                insertWord(task);
                            }
                        })
                        .show();
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


    private void insertWord(String word) {
        Log.d(TAG, "Inserting .." + word);
        DatabaseHandler db = new DatabaseHandler(getContext());
        if (checkExistenceInDataBase(word)) {
            Toast.makeText(getActivity(), "Word Already Exist ", Toast.LENGTH_SHORT).show();
        } else {
            word = word.trim();
            db.addWord(new Word(word));
            if (db.getWordsCount() > 0) {
                listView.setVisibility(View.VISIBLE);
                noWords.setVisibility(View.GONE);
            }
            wordList.add(new Word(word));
            wordsListAdapter.notifyDataSetChanged();
        }
    }

    private boolean checkExistenceInDataBase(String word) {
        DatabaseHandler db = new DatabaseHandler(getContext());
        word = word.trim();
        List<Word> words = db.getAllWords();
        for (Word wd : words) {
            if (wd.getWord().equals(word)) {
                return true;
            }
        }
        return false;
    }
}
