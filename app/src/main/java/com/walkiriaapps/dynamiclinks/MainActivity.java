package com.walkiriaapps.dynamiclinks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.dynamic_link_generator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateDynamicLink();
            }
        });

        tryToGetDynamicLink();
    }

    public void tryToGetDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            if (deepLink.getQueryParameter("time") != null) {
                                ((TextView)findViewById(R.id.time_text_view)).setText("Link Generated at: " + deepLink.getQueryParameter("time"));
                                findViewById(R.id.time_text_view).setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d("WALKIRIA", "ERROR WITH DYNAMIC LINK OR NO LINK AT ALL");
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("WALKIRIA", "ERROR WITH DYNAMIC LINK " + e.toString());

                    }
                });
    }

    public void generateDynamicLink() {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Task<ShortDynamicLink> dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://walkiriaapps.page.link?time=" + currentTime))
                .setDomainUriPrefix("https://walkiriaapps.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.walkiriaapps.dynamiclinks")
                                .setMinimumVersion(1)
                                .build())
                .setIosParameters(
                        new DynamicLink.IosParameters.Builder("com.walkiriaapps.dynamiclinks")
                                .setAppStoreId("whatever")
                                .setMinimumVersion("1.0.1")
                                .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("WALKIRIA TIME")
                                .setDescription("CURRENT TIME")
                                .setImageUrl(Uri.parse("https://www.android.com/static/images/logos/andy-lg.png"))
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "Share current time" + ": " + shortLink.toString());
                            sendIntent.setType("text/plain");

                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                            startActivity(shareIntent);
                        } else {
                            // Error
                            // ...
                            Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_LONG).show();
                            Log.d("WALKIRIA", "ERROR " + task.getException());
                        }
                    }
                });
    }

}