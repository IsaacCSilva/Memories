package edu.csulb.memoriesapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Setting extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final TextView textSwitch = (TextView) findViewById(R.id.textSwitch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        Button b = (Button) findViewById(R.id.button2);

        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent helpIntent = new Intent(Setting.this,HelpActivity.class);
                startActivity(helpIntent);
            } });
        final SeekBar s = (SeekBar) findViewById(R.id.seekBar);
        textSwitch.setText("Covered: " + s.getProgress() + "/" + s.getMax());
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                Toast.makeText(getApplicationContext(), "Changing Radius", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textSwitch.setText("Covered: "  + progress + "/" + s.getMax());
                Toast.makeText(getApplicationContext(), "Radius", Toast.LENGTH_SHORT).show();;
            }
        });
    }

    private void setSupportActionBar(Toolbar toolbar) {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
