package hfad.com.seminar_message;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

    private String SMS_RECEIVED = "SMS_RECEIVED_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] sms = null;
        String str = "SMS from ";
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            sms = new SmsMessage[pdus.length];
            for (int i = 0; i < sms.length; i++) {
                sms[i] = SmsMessage.createFromPdu((byte[]) pdus[i]) ;
                if (i == 0) {
                    str += sms[i].getOriginatingAddress();
                    str += ": ";
                }
                str += sms[i].getMessageBody().toString();
            }
        }
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SMS_RECEIVED);
        broadcastIntent.putExtra("sms", str);
        context.sendBroadcast(broadcastIntent);
    }

}
