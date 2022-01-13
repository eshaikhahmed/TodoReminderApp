package com.app.mytodo;

import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.java.adapter.TodoAdapter;
import com.java.database.SQLiteController;
import com.java.model.Todo;
import com.java.util.DateUtility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TodoAdapter adapter;

    RecyclerView taskRecycler;
    EditText editText;
    TextView lblToday;
    FloatingActionButton fab;
    NavigationView navigationView;
    String dateOfFilter;
    List<Todo> todoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializedLayoutComponents();
        setFilterListAndPageLabel(savedInstanceState);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editText.getText().toString() != null) {
                     insertYourGoalIntoDatabase(view);
                    hideKeypad();
                    editText.setText("");
                } else {
                    editText.requestFocus();
                }

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addMenuItemInNavMenuDrawer();

        todoData = getListTodo();
        taskRecycler = (RecyclerView) findViewById(R.id.taskRecycler);


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        adapter = new TodoAdapter(todoData,this);
        taskRecycler.setAdapter(adapter);
        taskRecycler.setLayoutManager(linearLayoutManager);


        //Initialized swipping
        initSwapping();

    }

    private void initSwapping() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder from, RecyclerView.ViewHolder to) {
                adapter.onViewMoved(from.getAdapterPosition(), to.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.onViewSwiped(position);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    float alpha = 1 - (Math.abs(dX) / recyclerView.getWidth());
                    viewHolder.itemView.setAlpha(alpha);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(taskRecycler);
    }

    private void setFilterListAndPageLabel(Bundle savedInstanceState) {

        if(savedInstanceState != null) {
            dateOfFilter = savedInstanceState.getString("filterDate");
            lblToday.setText(savedInstanceState.getString("lblShow"));
        } else {
            dateOfFilter = DateUtility.getDateUtility().getCurrentDateYear(new Date());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CHECK","COMING");

        todoData = getListTodo();
        adapter.updateList(todoData);
        updateMenuList();
        Log.d("CHECK","COMING2");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filterDate", dateOfFilter);
        outState.putString("lblShow", lblToday.getText().toString());
    }

    private void initializedLayoutComponents() {


        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        editText = (EditText)findViewById(R.id.goalInput);
        lblToday = (TextView)findViewById(R.id.lbbTitle);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)
                {
                    insertYourGoalIntoDatabase(fab);
                    hideKeypad();
                    editText.setText("");
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    fab.setImageResource(R.drawable.ic_check_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void hideKeypad() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d("ERROR!",e.getMessage());
        }
    }

    private void insertYourGoalIntoDatabase(View v) {
        try {
            String yourGoal = editText.getText().toString();

            if(yourGoal!=null && !yourGoal.equals("")){
                SQLiteController controller = new SQLiteController(this);

                String currentDate = DateUtility.getDateUtility().getCurrentDate();
                String values[] ={yourGoal, "0", currentDate, "0", dateOfFilter};


                String goal_query = "INSERT INTO todo(name, selected, created_date, active_status, due_date)  VALUES(?, ?, ?, ?, ?); ";
                String status = controller.fireSafeQuery(goal_query, values);

                Snackbar snackbar = Snackbar.make(v, status, Snackbar.LENGTH_LONG);
                View snackBarView = snackbar.getView();
                snackBarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                if(status.equals("Done")) {
                    snackbar.setText("You have added task!").show();
                } else {
                    snackbar.show();
                }

                controller.close();
                
                lblToday.requestFocus();
                updateTodoData();

            }
        } catch (Exception ex){
            Log.d("ERROR!", ex.getMessage());
        }
        fab.setImageResource(R.drawable.ic_add_blue_24dp);
    }

    private void updateTodoData() {


        try {
            SQLiteController controller = new SQLiteController(this);

            String query = "SELECT * FROM todo WHERE active_status = '0' ORDER BY todo_id DESC ";
            Cursor cursor = controller.safeRetrieve(query,null);
            if(cursor!=null && cursor.getCount()>0) {
                cursor.moveToFirst();

                Todo todo = new Todo();
                String name=cursor.getString(cursor.getColumnIndex("name"));
                String todo_id=cursor.getString(cursor.getColumnIndex("todo_id"));
                String selected=cursor.getString(cursor.getColumnIndex("selected"));
                String due_date=cursor.getString(cursor.getColumnIndex("due_date"));
                String descr=cursor.getString(cursor.getColumnIndex("descr"));
                int count = getPendingSubTaskCount(todo_id, controller);

                todo.setName(name);
                todo.setSelected(selected);
                todo.setId(todo_id);
                todo.setDueDate(due_date);
                todo.setDescription(descr);
                todo.setSubTotalCount(count == 0 ? "" : count+"");

                todoData.add(todo);


            }
            cursor.close();
            controller.close();
        } catch (Exception ex) {
            Log.d("ERROR!",ex.getMessage());
        }
        adapter.notifyDataSetChanged();
    }

    private List<Todo> getListTodo() {
        List<Todo> toReturn= new ArrayList<>();

        try {
            SQLiteController controller = new SQLiteController(this);

            String query = "SELECT * FROM todo WHERE active_status = '0' AND due_date = ? ORDER BY view_order ASC";
            String values[] = {dateOfFilter};
            Cursor cursor = controller.safeRetrieve(query, values);
            if(cursor!=null && cursor.getCount()>0) {
                cursor.moveToFirst();
                do {

                    Todo todo = new Todo();

                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String todo_id = cursor.getString(cursor.getColumnIndex("todo_id"));
                    String selected = cursor.getString(cursor.getColumnIndex("selected"));
                    String due_date = cursor.getString(cursor.getColumnIndex("due_date"));
                    String description = cursor.getString(cursor.getColumnIndex("descr"));
                    String order_view = cursor.getString(cursor.getColumnIndex("view_order"));
                    int count = getPendingSubTaskCount(todo_id, controller);

                    todo.setName(name);
                    todo.setSelected(selected);
                    todo.setId(todo_id);
                    todo.setDueDate(due_date);
                    todo.setDescription(description);
                    todo.setSubTotalCount(count == 0 ? "" : count+"");
                    Log.d("LIST ", name+" : "+order_view);
                    toReturn.add(todo);

                } while (cursor.moveToNext());
                cursor.close();
                controller.close();
            }
        } catch (Exception ex) {
            Log.d("ERROR!",ex.getMessage());
        }
        return toReturn;
    }

    private int getPendingSubTaskCount(String todo_id, SQLiteController controller) {
        int count = 0;

        try {

            String query = "SELECT * FROM subtodo WHERE active_status = '0' AND selected ='0' AND todo_id = ? ";
            String values[] = {todo_id};
            Cursor cursor = controller.safeRetrieve(query, values);

            count = cursor.getCount();

            cursor.close();

        } catch (Exception ex){
            Log.d("ERROR!", ex.getMessage());
        }


        return count;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void addMenuItemInNavMenuDrawer() {

        Menu menu = navigationView .getMenu();
        SQLiteController controller = new SQLiteController(this);
        try {

            String query = "SELECT DISTINCT due_date FROM todo WHERE active_status = '0'";

            int after = 103;
            Cursor cursor = controller.safeRetrieve(query, null);

            Date tomorrowDate = DateUtility.getDateUtility().getTomorrowDate(new Date());
            tomorrowDate = DateUtility.getDateUtility().converDateToSpecifiedFormate(tomorrowDate);
            Date current = new Date();
            String strCurrent = DateUtility.getDateUtility().getCurrentDateYear(new Date());

            if(cursor!=null && cursor.getCount()>0) {
                cursor.moveToFirst();
                do {

                    String due_date = cursor.getString(cursor.getColumnIndex("due_date"));

                    if(due_date != null) {

                        Date dueDate = DateUtility.getDateUtility().getDateFromString(due_date);

                        if ((!due_date.equalsIgnoreCase(strCurrent)) && dueDate.before(current)) {

                            // GROUP ID,ITEM ID, ORDER, TITLE
                            after++;
                            MenuItem menuItem = menu.add(R.id.checklist, after, after, DateUtility.getDateUtility().getStringHumanReadableDate(dueDate));
                            menuItem.setCheckable(true);
                            Log.d("BEFORE", due_date);

                        } else if (dueDate.after(tomorrowDate)) {

                            after++;
                            MenuItem menuItem = menu.add(R.id.checklist, after, after, DateUtility.getDateUtility().getStringHumanReadableDate(dueDate));
                            menuItem.setCheckable(true);
                            Log.d("AFTER", due_date);

                        }
                    }

                } while (cursor.moveToNext());
                cursor.close();


            }

        } catch (Exception ex) {
            Log.d("ERROR!", ex.getMessage());
        }
        controller.close();
    }

    private void updateMenuList() {
        Menu menu = navigationView .getMenu();
        menu.clear();

        // GROUP ID,ITEM ID, ORDER, TITLE
        menu.add(R.id.checklist, R.id.today, 100, "Today").setCheckable(true);
        menu.add(R.id.checklist, R.id.tommorrow, 100, "Tommorrow").setCheckable(true);

        addMenuItemInNavMenuDrawer();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.today) {
            // Handle the camera action
            setFilterContent("Today", new Date());

        } else if (id == R.id.tommorrow) {
            setFilterContent("Tommorrow", DateUtility.getDateUtility().getTomorrowDate(new Date()));

        }
        else {
            lblToday.setText(item.getTitle().toString());
            dateOfFilter = DateUtility.getDateUtility().getComputerReadableDate(item.getTitle().toString());
            todoData = getListTodo();
            adapter.updateList(todoData);
        }
     /*   } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }else if (id == R.id.nav_send) {

        }
 */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setFilterContent(String day, Date date) {
        lblToday.setText(day);
        dateOfFilter = DateUtility.getDateUtility().getCurrentDateYear(date);
        todoData = getListTodo();
        adapter.updateList(todoData);
    }

}
