package com.ak47.doNotDisturb.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ak47.doNotDisturb.R;

public class AboutActivity extends AppCompatActivity {
    private ImageView linkedInLinkImageView, githubLinkImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AddFindViewByid();

        linkedInLinkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLinkedInId();
            }
        });
        githubLinkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGithubId();
            }
        });
    }


    private void AddFindViewByid() {
        linkedInLinkImageView = findViewById(R.id.linkedInLinkImageView);
        githubLinkImageView = findViewById(R.id.githubLinkImageView);
    }

    private void openGithubId() {
        Uri uri = Uri.parse("https://github.com/ajeetAk47");
        Intent githubIntent = new Intent(Intent.ACTION_VIEW, uri);
        githubIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(githubIntent);
    }

    private void openLinkedInId() {
        Uri uri = Uri.parse("linkedin://add/%@" + "ajeet-yadav-3a123a130");
        Intent linkedInIntent = new Intent(Intent.ACTION_VIEW, uri);
        linkedInIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(linkedInIntent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/ajeet-yadav-3a123a130")));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
