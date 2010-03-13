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

  private String[] getTimes() {
    Date now = new Date();
    String[] timezones = new String[] {
      "America/Los_Angeles",
      "Africa/Windhoek"
    };
    List<String> entries = new ArrayList<String>();
    for (String tzName : timezones) {
      TimeZone tz = TimeZone.getTimeZone(tzName);
      if (tz != null) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(tz);
        entries.add(dateFormat.format(now) + " in " + tz.getDisplayName());
      }
    }
    
    return entries.toArray(new String[0]);
  }
}
