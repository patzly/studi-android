package com.hstrobel.lsfplan.gui.download;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hstrobel.lsfplan.Constants;
import java.util.LinkedList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.zedler.patrick.studi.R;
import xyz.zedler.patrick.studi.net.IcsFileDownloader;
import xyz.zedler.patrick.studi.net.LoginTask;
import xyz.zedler.patrick.studi.util.NetUtil;

public class NativeSelector extends AppCompatActivity {

  private static final String TAG = "LSF";
  PlanExportLoader exportLoader = null;
  PlanOverviewLoader overviewLoader = null;
  LoginTask loginTask = null;
  private NativeSelector local;
  private ExpandableListView listView;
  private CourseListAdapter listAdapter;
  private ProgressBar spinner;
  private CourseGroup selectedCourseGroup = null;
  private CourseGroup.Course selectedCourse = null;

  private GlobalState state;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_selector);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(view -> loadExportUrl());

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    local = this;
    listView = findViewById(R.id.listView);
    listAdapter = new CourseListAdapter(this);
    listView.setAdapter(listAdapter);

    spinner = findViewById(R.id.progressBarHtml);

    state = GlobalState.getInstance();
    loadOverview();

    listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
      private View lastHighlight = null;

      public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
          int childPosition, long id) {
        //mark active entry
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.background, typedValue, true);

        v.setBackgroundResource(R.color.orange);
        if (lastHighlight != null) {
          lastHighlight.setBackgroundColor(typedValue.data);
        }
        lastHighlight = v;

        //get details
        int group_index = parent.getFlatListPosition(ExpandableListView
            .getPackedPositionForGroup(groupPosition));
        int child_index = parent.getFlatListPosition(ExpandableListView
            .getPackedPositionForChild(groupPosition, childPosition));

        selectedCourseGroup = (CourseGroup) parent.getItemAtPosition(group_index);
        selectedCourse = (CourseGroup.Course) parent.getItemAtPosition(child_index);

        //enable download button
        fab.setVisibility(View.VISIBLE);
        return true;
      }

    });

  }


  @Override
  protected void onStop() {
    super.onStop();

    if (exportLoader != null) {
      exportLoader.cancel(true);
    }
    if (overviewLoader != null) {
      overviewLoader.cancel(true);
    }
    if (loginTask != null) {
      loginTask.cancel(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_html_web, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_refresh) {
      GlobalState.getInstance().cachedPlans = null;
      loadOverview();
    }

    return super.onOptionsItemSelected(item);
  }


  private void loadExportUrl() {
    Log.d(TAG, "loadExportUrl");
    if (selectedCourse == null) {
      return;
    }

    if (selectedCourse.URL.equals(Constants.MAGIC_WORD_LOGIN)) {
      showLoginForm();
    } else {
      //UI
      enableLoading();

      exportLoader = new PlanExportLoader();
      exportLoader.execute(selectedCourse.URL);
      Log.i(TAG, "loadExportUrl: hello " + selectedCourse.URL);
    }
  }


  public void loginCallback(String loginCookie) {
    if (loginCookie == null) {
      Toast.makeText(this, "Login failed. Check your username/password.", Toast.LENGTH_LONG).show();
      disableLoading();
      return;
    }

    exportLoader = new PlanExportLoader();
    exportLoader.execute(NetUtil.getPersonalPlanUrl(this), loginCookie);
  }

  private void exportCallback(String url) {
    if (url == null) {
      disableLoading();
      if (selectedCourse.URL.equals(Constants.MAGIC_WORD_LOGIN)) {
        //handle no courses corbercase
        Toast.makeText(this,
            "Download failed! Check your connection and make sure that you subscribed to some courses.",
            Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, "Download failed! Check your connection", Toast.LENGTH_LONG).show();
      }
      return;
    }

    Log.d(TAG, "exportCallback");
    state.icsDownloader = new IcsFileDownloader(this, url, () -> {
      try {
        Looper.prepare();
        GlobalState state = GlobalState.getInstance();

        if (state.isDownloadInvalid()) {
          //not a ics file
          Snackbar.make(findViewById(android.R.id.content), R.string.webView_fileNotValid,
              Snackbar.LENGTH_LONG).setAction("Action", null).show();
          spinner.setVisibility(View.GONE);
          return;
        }

        state.SetNewCalendar(this);

        //navigate back to main
        Snackbar.make(findViewById(android.R.id.content), R.string.webView_fileLoaded,
            Snackbar.LENGTH_SHORT).show();
        NavUtils.navigateUpFromSameTask(this);

      } catch (Exception ex) {
        Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        Log.e("LSF", "DL: FileLoaded: ", ex);
        spinner.setVisibility(View.GONE);
      }
    });
    new Thread(state.icsDownloader).start();
  }

  private void loadOverview() {
    Log.d(TAG, "loadOverview");

    if (state.cachedPlans != null) {
      // got a saved version
      overviewCallback(state.cachedPlans);
    } else {
      enableLoading();

      String url = NetUtil.getCoursesOverviewUrl(this);
      overviewLoader = new PlanOverviewLoader();
      overviewLoader.execute(url);
    }
  }

  private void overviewCallback(List<CourseGroup> results) {
    if (results == null) {
      Toast.makeText(this, "Download failed! Check your Connection", Toast.LENGTH_LONG).show();
      return;
    }
    Log.d(TAG, "overviewCallback");
    state.cachedPlans = results;

    listAdapter.clear();
    listAdapter.addPlanGroup(getString(R.string.html_login_head));
    listAdapter.addPlanItem(getString(R.string.html_login_head), getString(R.string.html_login_sub),
        Constants.MAGIC_WORD_LOGIN);

    for (CourseGroup group : results) {
      listAdapter.addPlanGroup(group.name);
      for (CourseGroup.Course item : group.items) {
        listAdapter.addPlanItem(group.name, item.name, item.URL);
      }
    }
    listView.invalidateViews();

    disableLoading();
  }


  private void showLoginForm() {
    // Create Object of Dialog class
    final Dialog login = new Dialog(this);
    // Set GUI of login screen
    login.setContentView(R.layout.login_dialog);
    login.setTitle("Enter HTWG Login Data");

    // Init button of login GUI
    final Button btnLogin = login.findViewById(R.id.btnLogin);
    final Button btnCancel = login.findViewById(R.id.btnCancel);
    final EditText txtUsername = login.findViewById(R.id.txtUsername);
    final EditText txtPassword = login.findViewById(R.id.txtPassword);

    // Attached listener for login GUI button
    btnLogin.setOnClickListener(v -> {
      String user = txtUsername.getText().toString().trim();
      String pw = txtPassword.getText().toString().trim();

      if (!user.isEmpty() && !pw.isEmpty()) {
        login.dismiss();
        //login
        enableLoading();

        loginTask = new LoginTask(local);
        loginTask.execute(user, pw);
      }
    });
    btnCancel.setOnClickListener(v -> login.dismiss());

    // Make dialog box visible.
    login.show();
  }

  private void enableLoading() {
    spinner.setVisibility(View.VISIBLE);
    listView.setEnabled(false);
  }

  private void disableLoading() {
    listView.setEnabled(true);
    spinner.setVisibility(View.GONE);
  }

  public class PlanOverviewLoader extends AsyncTask<String, String, List<CourseGroup>> {

    @Override
    protected List<CourseGroup> doInBackground(String... params) {
      // Making HTTP request
      List<CourseGroup> list = new LinkedList<>();
      CourseGroup group;
      CourseGroup.Course item;

      try {
        Connection connection = NetUtil.setupAppConnection(params[0], getApplicationContext());
        Document doc = connection.get();

        Elements tableRows = doc.select("tr");
        for (Element row : tableRows) {
          if (row.children().size() != 3) {
            continue; //skip head row
          }
          Elements columns = row.children();

          //course name
          Element courseURL = columns.get(0).child(0); //row 0 --> a class --> inner text
          if (BuildConfig.DEBUG) {
            Log.d(TAG, courseURL.text());
          }
          group = new CourseGroup(courseURL.text());

          //course semesters
          for (Element ele : columns.get(1).children()) {
            if (!ele.tagName().equals("a")) {
              continue;
            }
            if (BuildConfig.DEBUG) {
              Log.d(TAG, String.format("%s : %s", ele.text(), ele.attr("href")));
            }
            item = new CourseGroup.Course(ele.text(), ele.attr("href"));
            group.items.add(item);
          }
          //course everthing
          Element allURL = columns.get(2).child(0); //row 0 --> a class --> inner text
          if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("%s : %s", allURL.text(), allURL.attr("href")));
          }
          item = new CourseGroup.Course(allURL.text(), allURL.attr("href"));
          group.items.add(item);

          list.add(group);
        }

      } catch (Exception ex) {
        Log.e(TAG, "FAIL DL: ", ex);
        list = null;
      }

      return list;
    }

    @Override
    protected void onPostExecute(List<CourseGroup> result) {
      super.onPostExecute(result);
      overviewCallback(result);
    }
  }

  //exctract the export ics url
  public class PlanExportLoader extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
      // Making HTTP request
      String url = null;
      try {
        Connection con = NetUtil.setupAppConnection(params[0], getApplicationContext());
        if (params.length > 1) {
          con.cookie("JSESSIONID", params[1]);
        }
        Document doc = con.get();

        Elements images = doc.select("img");
        for (Element imgs : images) {
          if (imgs.hasAttr("src")) {
            if (imgs.attr("src").equals("/QIS/images//calendar_go.svg")) {
              //found export/target ics file ;)
              Log.d(TAG, imgs.parent().attr("href"));
              url = imgs.parent().attr("href");
              break;
            }
          }
        }
        if (url == null) {
          Log.e(TAG, "Export URL not found! " + doc.text());
        }


      } catch (Exception ex) {
        Log.e(TAG, "FAIL DL: ", ex);
        url = null;
      }
      return url;
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      exportCallback(result);
    }
  }
}
