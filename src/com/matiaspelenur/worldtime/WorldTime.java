package com.matiaspelenur.worldtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WorldTime extends ListActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ListView lv = getListView();
    lv.setTextFilterEnabled(true);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, getTimes()));
  }

  private List<String> getTimes() {
    Date now = new Date();
    Pattern tzPattern = Pattern.compile("/(\\w+)");
    String[] timezones = new String[] {
      "America/Los_Angeles",
      "America/New_York",
      "America/Buenos_Aires",
      "America/Montevideo",
      "Europe/London",
      "Africa/Windhoek"
    };
    List<String> times = new ArrayList<String>();
    for (String tzName : timezones) {
      TimeZone tz = TimeZone.getTimeZone(tzName);
      if (tz != null) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(tz);
        Matcher matcher = tzPattern.matcher(tzName);
        matcher.find();
        times.add(dateFormat.format(now) + " in " + matcher.group(1).replace("_", " "));
      }
    }
    return times;
  }
}
