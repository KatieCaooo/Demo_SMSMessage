package hfad.com.seminar_message;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {

    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";
    private String FAILURE = "GENERIC_ERROR";
    private String NO_SERVICE = "NO_SERVICE";
    private String NULL_PDU = "NULL_PDU";
    private String RADIO_OFF = "RADIO_OFF";
    private String NOT_DELIVERED = "SMS_NOT_DELIVERED";
    private PendingIntent sentPI;
    private PendingIntent deliveredPI;
    private BroadcastReceiver smsSentReceiver;
    private BroadcastReceiver smsDeliveredReceiver;
    private IntentFilter filter;
    private String SMS_RECEIVED = "SMS_RECEIVED_ACTION";
    private TextView txtMessge;
    private EditText edtPhoneNumber;
    private EditText edtMessage;
    private EditText edtEmail;

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            txtMessge = (TextView) findViewById(R.id.txtSMSMessage);
            txtMessge.setText(intent.getExtras().getString("sms").toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtMessage = findViewById(R.id.txtMessage);
        edtPhoneNumber = findViewById(R.id.txtPhoneNum);
        edtEmail = findViewById(R.id.edtEmail);
        txtMessge = (TextView) findViewById(R.id.txtSMSMessage);

            filter = new IntentFilter();
            filter.addAction(SMS_RECEIVED);

            registerReceiver(intentReceiver, filter);
    }

    public void clickToSendSMS(View view) {
        String phoneNo = edtPhoneNumber.getText().toString();
        String content = edtMessage.getText().toString();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, content, null, null);
    }

    public void clickToSendSMSIntent(View view) {
        Uri uri = Uri.parse("smsto:" + edtPhoneNumber.getText().toString());
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(uri);
        intent.putExtra("sms_body", edtMessage.getText().toString());
        startActivity(intent);
    }

    public void clickToSendSMSFB(View view) {
        SmsManager sms = SmsManager.getDefault();

        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        filter = new IntentFilter();
        filter.addAction(SMS_RECEIVED);

        registerReceiver(intentReceiver, filter);

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "smsSentReceiver: " + SENT, Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "smsSentReceiver: " + FAILURE, Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "smsSentReceiver: " + FAILURE, Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "smsSentReceiver: " + NULL_PDU, Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "smsSentReceiver: " + RADIO_OFF, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "smsDeliveredReceiver: " + DELIVERED, Toast.LENGTH_LONG).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "smsDeliveredReceiver: " + NOT_DELIVERED, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(SENT));
        sms.sendTextMessage(edtPhoneNumber.getText().toString(), null,
                edtMessage.getText().toString(), sentPI, deliveredPI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (intentReceiver != null) {
            unregisterReceiver(intentReceiver);
        }
        if (smsSentReceiver != null) {
            unregisterReceiver(smsSentReceiver);
        }
        if (smsDeliveredReceiver != null) {
            unregisterReceiver(smsDeliveredReceiver);
        }
    }

    private void sendEmail(String[] adress, String[] cc, String subject, String msg) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, adress);
        emailIntent.putExtra(Intent.EXTRA_CC, cc);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);
        emailIntent.setType("message/rfc822");
        startActivity(Intent.createChooser(emailIntent, "Email"));
    }

    public void clickToSendEmail(View view) {
        String[] to = {"danhltnse130015@fpt.edu.vn"};
        String[] cc = {"danhltnse130015@fpt.edu.vn"};
        sendEmail(to, cc, "Email from Android", "Test of Email Msg\n DanhLTN");
    }

    public void clickToSendEmailInApp(View view) {
        String content = edtEmail.getText().toString();
        new EmailUtil().execute(content);
    }

    private class EmailUtil extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args) {
            try {
                Properties prop = new Properties();
                prop.put("mail.smtp.host", "smtp.gmail.com");
                prop.put("mail.smtp.port", "587");
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.starttls.enable", "true");

                Authenticator authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(InfoUserEmail.username, InfoUserEmail.password);
                    }
                };
                Session session = Session.getInstance(prop, authenticator);
                Message message = new MimeMessage(session);
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse("x"));
                message.setSubject("Testing Email in app");
                message.setText(args[0]);
                Transport.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
