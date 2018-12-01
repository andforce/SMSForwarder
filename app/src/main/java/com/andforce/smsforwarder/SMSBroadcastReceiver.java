package com.andforce.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private static final String TAG = "SMSForwarder";

    public SMSBroadcastReceiver() {
        Log.i(TAG, "new SMSBroadcastReceiver");
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "sms received!");

        if (!SMS_RECEIVED.equals(intent.getAction())) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        Cursor cur = null;
        long maxIndex = -1;
        try {
            ContentResolver cr = context.getContentResolver();
            cur = cr.query(SMS_INBOX, null, null, null, "date desc");
            if (cur != null && cur.moveToFirst()) {
                maxIndex = cur.getLong(cur.getColumnIndex("_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) {
                cur.close();
                cur = null;
            }
        }


        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length == 0){
            return;
        }

        final SmsMessage[] messages = new SmsMessage[pdus.length];

        final List<ContentValues> contentValuesList = new ArrayList<>();

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            messages[i] = smsMessage;

            String msgBody = smsMessage.getMessageBody();
            String msgAddress = smsMessage.getOriginatingAddress();
            long msgDate = smsMessage.getTimestampMillis();
            String smsToast = "New SMS received from : " + msgAddress + "\n'" + msgBody + "'";
            Toast.makeText(context, smsToast, Toast.LENGTH_LONG).show();
            Log.d(TAG, "message from: " + msgAddress + ", message body: " + msgBody + ", message date: " + msgDate);

            ContentValues contentValues = new ContentValues();
            contentValues.put("address", smsMessage.getOriginatingAddress());
            contentValues.put("sim_id", smsMessage.getIndexOnIcc());
            contentValues.put("date", smsMessage.getTimestampMillis());
            contentValues.put("body", smsMessage.getMessageBody());
            contentValues.put("read", 1);
            contentValues.put("type", 1);//1：inbox  2：sent 3：draft56  4：outbox  5：failed  6：queued
            contentValues.put("protocol", 0);//0：SMS_RPOTO, 1：MMS_PROTO
            contentValues.put("person", "");
            contentValuesList.add(contentValues);

        }

        final long finalMaxIndex = maxIndex;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = context.getContentResolver();
                ContentValues[] toInsert = new ContentValues[contentValuesList.size()];
                Log.d(TAG, "Insert Sms Count:===>>> " + contentValuesList.size());
                contentResolver.bulkInsert(Uri.parse("content://sms/inbox"), contentValuesList.toArray(toInsert));

                List<Sms> smsList = getSmsFromPhone(finalMaxIndex, messages);
                if (smsList != null) {
                    for (Sms s : smsList) {
                        Log.d(TAG, "PushSMS");
                        PushData pushData = new PushData();
                        pushData.setProd("dev");

                        Where where = new Where();
                        where.setDeviceType("ios");
                        pushData.setWhere(where);

                        Data data = new Data();
                        Aps aps = new Aps();
                        Alert alert = new Alert();
                        alert.setTitle(s.getAddress());
                        alert.setBody(s.getBody());
                        aps.setAlert(alert);
                        aps.setBadge("Increment");
                        aps.setSound("default");

                        aps.setSms(s);

                        data.setAps(aps);
                        pushData.setData(data);

                        pushSMS(context, pushData);
                    }
                }
            }
        }).start();
    }

    private List<Sms> getSmsFromPhone(long index, SmsMessage[] smsMessages) {

        List<Sms> smsList = new ArrayList<>();

        long id = index + 1;
        for (SmsMessage smsMessage : smsMessages) {
            Sms sms = new Sms();
            sms.setId("" + id++);
            sms.setAddress(smsMessage.getOriginatingAddress());
            sms.setSimId("" + smsMessage.getIndexOnIcc());
            sms.setDate("" + smsMessage.getTimestampMillis());
            sms.setBody(smsMessage.getMessageBody());
            sms.setRead("0");
            sms.setType("1");
            sms.setProtocol("0");
            sms.setPerson("");

            smsList.add(sms);
        }

        return smsList;
    }

    private void pushSMS(Context context, PushData sms) {
        OkHttpClient client = new OkHttpClient();

        String pushJson = new Gson().toJson(sms);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, pushJson);
        Request request = new Request.Builder()
                .url("https://6g0ounut.push.lncld.net/1.1/push")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("x-lc-id", "6G0ouNUTlUdRYE2ARgwAhiJM-gzGzoHsz")
                .addHeader("x-lc-key", "nGciHu1gAOH8A93ja2i9OOVY,wL3ha2OcwKHbBMYTULnhp9bY")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                String result = response.body().string();
                if (!TextUtils.isEmpty(result)) {
                    if (result.contains("objectId")) {
                        // success
                        setSmsRead(context, sms.getData().getAps().getSms().getId());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    private List<Sms> getSmsFromPhone(Context context) {

        ContentResolver cr = context.getContentResolver();
        String[] projection = null;//new String[]{"_id", "address", "sim_id", "date", "body", "read", "type", "protocol", "person"};
        String where = "read=0";// 推送未读短信
        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");

        if (null == cur) {
            return null;
        }

        List<Sms> smsList = new ArrayList<>();

        String[] names = cur.getColumnNames();
        while (cur.moveToNext()) {

            for (String n : names) {
                Log.d(TAG, "\r\nKey:" + n + "\t\t\t\t\tValues:" + cur.getString(cur.getColumnIndex(n)));
            }

            Sms sms = new Sms();
            sms.setId(cur.getString(cur.getColumnIndex("_id")));
            sms.setAddress(cur.getString(cur.getColumnIndex("address")));
            sms.setSimId(cur.getString(cur.getColumnIndex("sim_id")));
            sms.setDate(cur.getString(cur.getColumnIndex("date")));
            sms.setBody(cur.getString(cur.getColumnIndex("body")));
            sms.setRead(cur.getString(cur.getColumnIndex("read")));
            sms.setType(cur.getString(cur.getColumnIndex("type")));
            sms.setProtocol(cur.getString(cur.getColumnIndex("protocol")));
            sms.setPerson(cur.getString(cur.getColumnIndex("person")));

            smsList.add(sms);

            if (true) {


            } else {

            }

            Log.d(TAG, "\r\n================================" + cur.getPosition());
        }
        cur.close();

        return smsList;
    }

    private void setSmsRead(Context context, String id) {
        Log.d(TAG, "设置已读：" + id);
        String query = "_id=" + id;
        ContentResolver cr = context.getContentResolver();
        ContentValues ct = new ContentValues();
        ct.put("read", 1);
        cr.update(SMS_INBOX, ct, query, null);
    }
}
