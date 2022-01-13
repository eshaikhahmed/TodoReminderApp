package com.app.mytodo;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Canvas;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.java.adapter.SubTodoAdapter;
import com.java.adapter.TodoAdapter;
import com.java.data.StaticData;
import com.java.database.SQLiteController;
import com.java.model.Todo;
import com.java.util.DateUtility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubTaskList extends AppCompatActivity {

    SubTodoAdapter adapter;
    RecyclerView taskRecycler;
    EditText editText;
    FloatingActionButton fab;
    List<Todo> todoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_task_list);

        setTitle("Sub Tasks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializedLayoutComponents();
        todoData = getListTodo();


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        adapter = new SubTodoAdapter(todoData,this);
        taskRecycler.setAdapter(adapter);
        taskRecycler.setLayoutManager(linearLayoutManager);

    }
    private void initializedLayoutComponents() {

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        taskRecycler = (RecyclerView) findViewById(R.id.taskRecycler);
        editText = (EditText)findViewById(R.id.goalInput);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setImageResource(R.drawable.ic_check_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editText.getText().toString() != null) {
                    insertYourGoalIntoDatabase(view);
                    view.setVisibility(View.GONE);
                    hideKeypad();
                    editText.setText("");
                }

            }
        });

        //Initiate swapping and dragging on recycler view
        initSwapping();

    }

    private void initSwapping() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {

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
    private void hideKeypad() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d("ERROR!",e.getMessage());
        }
    }

    private List<Todo> getListTodo() {
        List<Todo> toReturn= new ArrayList<>();

        try {
            SQLiteController controller = new SQLiteController(this);

            String query = "SELECT * FROM subtodo WHERE active_status = '0' AND todo_id = ? ORDER BY view_order ASC ";
            String values[] = {StaticData.todo.getId()};
            Cursor cursor = controller.safeRetrieve(query, values);
            if(cursor!=null && cursor.getCount()>0) {
                cursor.moveToFirst();
                do {

                    Todo todo = new Todo();

                    String name=cursor.getString(cursor.getColumnIndex("name"));
                    String todo_id=cursor.getString(cursor.getColumnIndex("subtodo_id"));
                    String selected=cursor.getString(cursor.getColumnIndex("selected"));
                    String due_date=cursor.getString(cursor.getColumnIndex("due_date"));
                    String descr=cursor.getString(cursor.getColumnIndex("descr"));

                    todo.setName(name);
                    todo.setSelected(selected);
                    todo.setId(todo_id);
                    todo.setDueDate(due_date);


                    toReturn.add(todo);

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception ex) {
            Log.d("ERROR!",ex.getMessage());
        }
        return toReturn;
    }

    private void insertYourGoalIntoDatabase(View v) {
        try {
            String yourgoal = editText.getText().toString();
            if(yourgoal!=null && !yourgoal.equals("")){
                SQLiteController controller = new SQLiteController(this);

                String currentDate = DateUtility.getDateUtility().getCurrentDate();
                String values[] ={yourgoal, "0", currentDate, "0", StaticData.todo.getId(), StaticData.todo.getDueDate()};


                String goal_query = "INSERT INTO subtodo(name, selected, created_date, active_status, todo_id, due_date)  VALUES(?, ?, ?, ?, ?, ?); ";
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


                updateTodoData();

            }
        } catch (Exception ex){
            Log.d("ERROR!", ex.getMessage());
        }
    }

    private void updateTodoData() {


        try {
            SQLiteController controller = new SQLiteController(this);

            String query = "SELECT * FROM subtodo WHERE active_status = '0' ORDER BY subtodo_id DESC ";
            Cursor cursor = controller.safeRetrieve(query,null);
            if(cursor!=null && cursor.getCount()>0) {
                cursor.moveToFirst();

                Todo todo = new Todo();
                String name=cursor.getString(cursor.getColumnIndex("name"));
                String todo_id=cursor.getString(cursor.getColumnIndex("subtodo_id"));
                String selected=cursor.getString(cursor.getColumnIndex("selected"));
                String due_date=cursor.getString(cursor.getColumnIndex("due_date"));
                String descr=cursor.getString(cursor.getColumnIndex("descr"));

                todo.setName(name);
                todo.setSelected(selected);
                todo.setId(todo_id);
                todo.setDueDate(due_date);
                todoData.add(todo);

                cursor.close();
            }
            controller.close();
        } catch (Exception ex) {
            Log.d("ERROR!",ex.getMessage());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
