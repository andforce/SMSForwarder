package com.andforce.smsforwarder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SMSContentObserver extends ContentObserver {

    private static final String TAG = "SMSForwarder";

    private Context mContext;

    SMSContentObserver(Handler handler, Context context) {
        super(handler);
        this.mContext = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "Sms received");

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Sms> smsList = getSmsFromPhone();

                if (smsList != null) {
                    for (Sms s : smsList) {
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

                        pushSMS(pushData);
                    }
                }

            }
        }).start();

    }

    private void pushSMS(PushData sms) {
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
                String  result = response.body().string();
                if (!TextUtils.isEmpty(result)){
                    if (result.contains("objectId")){
                        // success
                        setSmsRead(sms.getData().getAps().getSms().getId());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri SMS_INBOX = Uri.parse("content://sms/inbox");

    private List<Sms> getSmsFromPhone() {

        ContentResolver cr = mContext.getContentResolver();
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

            if (true){


            }  else {

            }

            Log.d(TAG, "\r\n================================" + cur.getPosition());
        }
        cur.close();

        return smsList;
    }

    private void setSmsRead(String id) {
        Log.d(TAG, "设置已读：" + id);
        String query = "_id=" + id;
        ContentResolver cr = mContext.getContentResolver();
        ContentValues ct = new ContentValues();
        ct.put("read", 1);
        cr.update(SMS_INBOX, ct, query, null);
    }
}
