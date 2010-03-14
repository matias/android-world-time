package com.matiaspelenur.worldtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;

public class WorldTime extends Activity {

  private BroadcastReceiver timeTickReceiver;
  private ListView mainListView;
  private ExpandableListView timeZonesListView;
  private ViewSwitcher switcher;
  private LinkedHashMultimap<String,String> timeZonesByRegion;
  
  // TODO(matias): let the user enable or disable regions
  private static final List<String> enabledRegions = ImmutableList.of(
      "America", "Europe"/*, "Africa", "Indian", "Pacific"*/);
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mainListView = new ListView(this);
    timeZonesListView = new ExpandableListView(this);
    timeZonesListView.setTextFilterEnabled(true);
    
    switcher = new ViewSwitcher(this);
    switcher.addView(mainListView);
    switcher.addView(timeZonesListView);
    
    setContentView(switcher);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (timeZonesListView.getAdapter() == null) {
//      fillTZList();
      timeZonesListView.setAdapter(new RegionCityListAdapter());
    }
    switcher.showNext();
    return true;
  }

  private void fillTZList() {
    if (timeZonesByRegion == null) {
      populateTZRegionMap();
    }
    
    List<Map<String, String>> regionList = Lists.newArrayList();
    List<List<Map<String, String>>> cityList = Lists.newArrayList();
    int id = 0;
    for (String region: enabledRegions) {
      regionList.add(ImmutableMap.of("region", region, "_id", String.valueOf(id)));
      id++;
      List<Map<String, String>> cities = Lists.newArrayList();
      for (String city : timeZonesByRegion.get(region)) {
        cities.add(ImmutableMap.of("city", city, "_id", String.valueOf(id)));
        id++;
      }
      cityList.add(cities);
    }
//    Log.i(this.toString(), String.valueOf(regionList));
//    Log.i(this.toString(), String.valueOf(cityList));
    
    /*SimpleExpandableListAdapter listAdapter = new SimpleExpandableListAdapter(
        this,
        regionList,
        R.layout.regions_parent_row,
        new String[] { "_id", "region" },
        new int[] { R.id.region_name},
        cityList,
        R.layout.regions_child_row,
        new String[] { "_id", "city" },
        new int[] { R.id.city_name }
        );
    timeZonesListView.setAdapter(listAdapter);*/
  }
  
  public class RegionCityListAdapter extends BaseExpandableListAdapter {

    private String[] regions = { "Africa", "Europe" };
    private String[][] children = {
        {"1", "2", "3", "4", "5", "6", "7", "8", "9"},
        {"10","11","12","13","14","15","16","17","18"}
    };
    
    private boolean[][] childrenValue = new boolean[regions.length][children[0].length];
    
    public RegionCityListAdapter() {
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
      return children[groupPosition][childPosition];
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
      return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
        View convertView, ViewGroup parent) {
      LinearLayout layout = new LinearLayout(WorldTime.this);
      TextView textView = new TextView(WorldTime.this);
      textView.setText(getChild(groupPosition, childPosition).toString());
      
      CheckBox checkbox = new CheckBox(WorldTime.this);
      checkbox.setChecked(childrenValue[groupPosition][childPosition]);
      checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          Log.i("matias:", "isChecked=" + isChecked + " for " + buttonView);
          childrenValue[groupPosition][childPosition] = isChecked;
        }
      });
      layout.addView(checkbox);
      layout.addView(textView);
      return layout;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      return children[groupPosition].length;
    }

    @Override
    public Object getGroup(int groupPosition) {
      return regions[groupPosition];
    }

    @Override
    public int getGroupCount() {
      return regions.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
      return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
        ViewGroup parent) {
      TextView textView = new TextView(WorldTime.this);
      textView.setText(getGroup(groupPosition).toString());
      textView.setPadding(36, 10, 0, 10);
      return textView;
    }

    @Override
    public boolean hasStableIds() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      // TODO Auto-generated method stub
      return false;
    }
    
  }

  private void populateTZRegionMap() {
    timeZonesByRegion = LinkedHashMultimap.create();
    Pattern tzPattern = Pattern.compile("(\\w+)/(\\w+)");
    for (String tz : TimeZone.getAvailableIDs()) {
      Matcher match = tzPattern.matcher(tz);
      if (match.matches()) {
        String region = match.group(1);
        String city = match.group(2);
        timeZonesByRegion.put(region, city);
      }
    }
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
