package com.tedneward;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
    
    // Check to see if a recognition activity is present
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activities = pm.queryIntentActivities(
      new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    if (activities.size() != 0) {
      // one is present      
    } else {
      // one isn't present
      Button btnSpeak = (Button)findViewById(R.id.btnSpeak);
      btnSpeak.setText("(No speech!)");
      btnSpeak.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          new AlertDialog.Builder(MainActivity.this)
            .setTitle("Sorry!")
            .setMessage("We can't find a speech recognizer on this device. You'll have to buy a new device.")
            .setPositiveButton("Right-o, boss!", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Great!", Toast.LENGTH_SHORT).show();
              }
            })
            .setNeutralButton("Aw, crud", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Great!", Toast.LENGTH_SHORT).show();
              }
            })
            .setNegativeButton("No way!", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Your choice (Bozo).", Toast.LENGTH_SHORT).show();
              }
            })
            .show();
        }
      });
    }

    
    alarmCreate();
  }

  /** Registered in the layout XML */
  public void onSendClicked(View view)
  {
    Toast.makeText(this, "Sending " + message.getText() + " ...", 
      Toast.LENGTH_SHORT).show();
    sendSMS(smsNumber.getText().toString(), message.getText().toString());
  }

  // ============ BEGIN STEP TWO

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

  // ============== BEGIN STEP THREE

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
  
  // ============== BEGIN STEP FOUR
  
  /*
  Technically, we should check to see if there's a recognizer installed;
  do that by doing the following in onCreate():
  
  
   */
  
  private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
  
  public void onSpeakClicked(View view)
  {
    startVoiceRecognitionActivity();
  }

	private void startVoiceRecognitionActivity()
  {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
      RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}
  /**
   * Handle the results from the recognition activity.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) 
  {
    if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && 
        resultCode == RESULT_OK) 
    {
      // Fill the list view with the strings the recognizer
      // thought it could have heard
      ArrayList<String> matches = 
        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      String msg = "";
      for (String m : matches)
        msg += (m + " ");
      message.setText(msg);
    }

    super.onActivityResult(requestCode, resultCode, data);
  }  
}
