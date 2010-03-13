package com.matiaspelenur.worldtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ViewSwitcher;

public class WorldTime extends Activity {

  BroadcastReceiver timeTickReceiver;
  ListView mainListView;
  ListView allTZListView;
  private ViewSwitcher switcher;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mainListView = new ListView(this);
    allTZListView = new ListView(this);
    allTZListView.setTextFilterEnabled(true);
    
    switcher = new ViewSwitcher(this);
    switcher.addView(mainListView);
    switcher.addView(allTZListView);
    
    setContentView(switcher);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (switcher.getNextView() == allTZListView && 
        allTZListView.getAdapter() == null) {
      fillTZList();
    }
    switcher.showNext();
    return true;
  }

  private void fillTZList() {
    allTZListView.setAdapter(new ArrayAdapter<String>(
        this, R.layout.list_item, TimeZone.getAvailableIDs()));
  }

  @Override
  protected void onResume() {
    super.onResume();
    timeTickReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        updateTimes();
      }
    };
    this.registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    updateTimes();
  }

  @Override
  protected void onPause() {
    super.onPause();
    this.unregisterReceiver(timeTickReceiver);
  }

  private void updateTimes() {
    mainListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, getTimes()));
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
