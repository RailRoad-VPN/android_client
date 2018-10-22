package net.rroadvpn.services;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RroadLogger {

    public static void writeLog(String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(msg);

                //to create a Text file name "logcat.txt" in SDCard
                File sdCard = Environment.getExternalStorageDirectory();

                Date currentDateTime = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");


                File logFile = new File(sdCard.getAbsolutePath() + "/RroadLog", "log_" + dateFormat.format(currentDateTime) + ".log");

//                File dir = new File(sdCard.getAbsolutePath() + "/myLogcat");
//                dir.mkdirs();
//                File file = new File(dir, "logcat.txt");

                if (!logFile.exists()) {
                    if (!logFile.getParentFile().exists()) {
                        logFile.getParentFile().mkdirs();
                    }
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(logFile, true);
                    OutputStreamWriter osw = new OutputStreamWriter(fOut);
                    osw.append(msg + "\n");
                    osw.flush();
                    osw.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy");
        String formattedDate = df.format(c);
        System.out.println(formattedDate);

        writeLog("test log 1");

    }
}
