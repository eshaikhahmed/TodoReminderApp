package com.java.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtility {

    private static DateUtility dateUtility;
    public static  DateUtility getDateUtility() {
        if(dateUtility == null) {
            dateUtility = new DateUtility();
        }
        return dateUtility;
    }

    public String getCurrentDate() {
        DateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.ENGLISH);
        String currentDate = format.format(new Date());
        return currentDate;
    }
    public Date getCurrentFormateDate() {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        Date returnTo = null;
        try {
            returnTo = format.parse(getCurrentDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnTo;
    }

    public Date getTomorrowDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        date = c.getTime();
        return  date;
    }
    public String getCurrentDateYear(Date date) {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String currentDate = format.format(date);
        return currentDate;
    }

    public Date getDateFromString(String date) {
        Date dateFormate = null;
        try {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            dateFormate = format.parse(date);
        } catch (ParseException ex){

        }

        return dateFormate;
    }

    public Date converDateToSpecifiedFormate(Date date) {

        Date dateFormate = null;
        try {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            dateFormate = format.parse(getCurrentDateYear(date));
        } catch (ParseException ex){

        }

        return dateFormate;
    }

    public String getStringHumanReadableDate(Date date){
        DateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        return format.format(date);
    }


    public String getComputerReadableDate(String toString) {
        String returnTo = "";
        DateFormat format = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        DateFormat destFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            Date date = format.parse(toString);
            returnTo = destFormat.format(date);
        } catch (ParseException ex){

        }
        return returnTo;
    }

}
