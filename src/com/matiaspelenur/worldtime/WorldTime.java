package com.matiaspelenur.worldtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WorldTime extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, getTimes()));
    
    ListView lv = getListView();
    lv.setTextFilterEnabled(true);
  }

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
  
  private String[] getTimes() {
    String[] timezones = new String[] {
      "America/Los_Angeles",
      "Africa/Windhoek"
    };
    List<String> entries = new ArrayList<String>();
    for (String tzName : timezones) {
      TimeZone tz = TimeZone.getTimeZone(tzName);
      if (tz != null) {
        Calendar cal = Calendar.getInstance(tz);
        Date date = cal.getTime();
        entries.add(dateFormat.format(date) + " in " + tz.getDisplayName());
      }
    }
    
    return entries.toArray(new String[0]);
  }
}
