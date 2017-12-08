package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

public class Setting extends Activity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // buttons for help and change user
        Button b2 = (Button) findViewById(R.id.button2);
        Button b1 = (Button) findViewById(R.id.button1);
        // set click listeners
        b2.setOnClickListener(this);
        b1.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //send intent to help
            case R.id.button2:
                Intent helpIntent = new Intent(Setting.this, HelpActivity.class);
                startActivity(helpIntent);
                break;
            // sending intent to edit profile
            case R.id.button1:
                // alert dialogbuilder  use for question user if he/she wants to change user page
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Setting.this);
                alertDialogBuilder
                        .setTitle("Are you sure you want to proceed with the edit? ")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent editIntent = new Intent(Setting.this, EditProfile.class);
                                startActivity(editIntent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.create();
                Dialog d = alertDialogBuilder.show();
                int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                TextView tv = (TextView) d.findViewById(textViewId);
                tv.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }
    }


    private void setSupportActionBar(Toolbar toolbar) {
    }

    public void loadSpinnerData() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Languages, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    public Resources res;
    public String locale;

    // this should help with changing languages
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Configuration config;
        config = new Configuration(res.getConfiguration());
        String label = parent.getItemAtPosition(position).toString();
        if (label == "French") {
            config.locale = new Locale("fr");
        } else if (label == "Chinese") {
            config.locale = new Locale("zh");
        } else if (label == "Spanish") {
            config.locale = new Locale("es");

        } else if (label == "Arabic") {
            config.locale = new Locale("ar");

        } else {
            label = "English(default)";
            config.locale = Locale.ENGLISH;
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
    }


    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return true;
    }
}
