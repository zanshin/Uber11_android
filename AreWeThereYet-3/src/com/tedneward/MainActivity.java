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
    
    alarmCreate();
  }

  /** Registered in the layout XML */
  public void onSendClicked(View view)
  {
    Toast.makeText(this, "Sending " + message.getText() + " ...", 
      Toast.LENGTH_SHORT).show();
    sendSMS(smsNumber.getText().toString(), message.getText().toString());
  }
  public void onStartClicked(View view)
  {
    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    long interval = 15*1000;
    Log.v("MainActivity", "onStartClicked: " + (SystemClock.elapsedRealtime() + interval));
    manager.setRepeating(AlarmManager.ELAPSED_REALTIME,
      SystemClock.elapsedRealtime()+interval,
      interval,
      mAlarmIntent);
  }
  public void onStopClicked(View view)
  {
    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    manager.cancel(mAlarmIntent);
  }
  public void onSpeakClicked(View view)
  {
    Toast.makeText(this, "Speak!", Toast.LENGTH_SHORT).show();
  }

  private void sendSMS(String destination, String message)
  {
    // Wire up the notifications that the SmsManager will send
    final String ACTION_SENT = "com.tedneward.awty.SENT";
    final String ACTION_DELIVERED = "com.tedneward.awty.DELIVERED";
    final BroadcastReceiver sent = new BroadcastReceiver(){
      @Override
      public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
          case Activity.RESULT_OK:
            //Handle sent success
            Toast.makeText(context, "Sent success", Toast.LENGTH_SHORT).show();
          break;
          case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
          case SmsManager.RESULT_ERROR_NO_SERVICE:
          case SmsManager.RESULT_ERROR_NULL_PDU:
          case SmsManager.RESULT_ERROR_RADIO_OFF:
            //Handle sent error
            Toast.makeText(context, "Sent error", Toast.LENGTH_SHORT).show();
          break;
        }
        unregisterReceiver(this);
      }
    };
    final BroadcastReceiver delivered = new BroadcastReceiver(){
      @Override
      public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
          case Activity.RESULT_OK:
            //Handle delivery success
            Toast.makeText(context, "Delivery success", Toast.LENGTH_SHORT).show();
          break;
          case Activity.RESULT_CANCELED:
            //Handle delivery failure
            Toast.makeText(context, "Delivery failure", Toast.LENGTH_SHORT).show();
          break;
        }
        unregisterReceiver(this);
      }
    };
    
    PendingIntent sIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SENT), 0);
    PendingIntent dIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DELIVERED), 0);
    
    registerReceiver(sent, new IntentFilter(ACTION_SENT));
    registerReceiver(delivered, new IntentFilter(ACTION_DELIVERED));

    //Send the message
    SmsManager manager = SmsManager.getDefault();
    manager.sendTextMessage(destination, null, message, sIntent, dIntent);
  }

  private PendingIntent mAlarmIntent;
  public static class AlarmReceiver 
    extends BroadcastReceiver 
  {
    @Override
    public void onReceive(Context context, Intent intent) 
    {
      String message = intent.getStringExtra(Intent.EXTRA_TEXT);
      String destination = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

      Log.v("AlarmReceiver", "SMSing " + destination + " with " + message);
      
      //Send the message, this time without callback Intents
      SmsManager manager = SmsManager.getDefault();
      manager.sendTextMessage(destination, null, message, null, null);
    }
  }

  private void alarmCreate()
  {
    //Create the launch sender with the extra data required to do the SMS send
    Intent launchIntent = new Intent(this, AlarmReceiver.class);
    launchIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, smsNumber.getText());
    launchIntent.putExtra(Intent.EXTRA_TEXT, message.getText());
    mAlarmIntent = PendingIntent.getBroadcast(this, 0, launchIntent, 0);
  }
}
