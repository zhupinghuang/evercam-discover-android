package io.evercam.connect.helper;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper
{
    // calculate time difference between two time points
    public static String getTimeDifference(String time)
    {

        String diff = "";
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date;
        try
        {
            date = formatter.parse(time);
            long diff_long = now.getTime() - date.getTime();

            day = diff_long / (24 * 60 * 60 * 1000);
            hour = (diff_long / (60 * 60 * 1000) - day * 24);
            min = ((diff_long / (60 * 1000)) - day * 24 * 60 - hour * 60);
            sec = (diff_long / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
            diff = "" + day + "day" + hour + "hour" + min + "min" + sec + "sec";
        }
        catch(ParseException e)
        {
            Log.e("Error", "Time difference error");
        }
        if(day != 0)
        {
            if(day == 1)
            {
                diff = day + " " + "day ago";
            }
            else
            {
                diff = day + " " + "days ago";
            }
        }
        else if(day == 0)
        {
            if(hour != 0)
            {
                if(hour == 1)
                {
                    diff = hour + " " + "hour ago";
                }
                else
                {
                    diff = hour + " " + "hours ago";
                }
            }
            else if(hour == 0)
            {
                if(min == 1)
                {
                    diff = min + " " + "minute ago";
                }
                else if(min == 0)
                {
                    diff = "now";
                }
                else
                {
                    diff = min + " " + "minutes ago";
                }
            }
        }
        return diff;
    }
}
