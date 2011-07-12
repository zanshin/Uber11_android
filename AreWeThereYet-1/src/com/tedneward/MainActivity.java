package com.tedneward;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import java.util.List;
import java.util.ArrayList;


public class MainActivity extends Activity
{
  private EditText smsNumber;
  private EditText message;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    message = (EditText)findViewById(R.id.message);
    smsNumber = (EditText)findViewById(R.id.number);
  }

  /** Registered in the layout XML */
  public void onSendClicked(View view)
  {
    Toast.makeText(this, "Sending " + message.getText() + " ...", 
      Toast.LENGTH_SHORT).show();
  }
  /** Registered in the layout XML */
  public void onStartClicked(View view)
  {
    Toast.makeText(this, "Starting sending " + message.getText() + " ...", 
      Toast.LENGTH_SHORT).show();
  }
  /** Registered in the layout XML */
  public void onStopClicked(View view)
  {
    Toast.makeText(this, "Stopping", Toast.LENGTH_SHORT).show();
  }
  public void onSpeakClicked(View view)
  {
    Toast.makeText(this, "Speak!", Toast.LENGTH_SHORT).show();
  }
}
