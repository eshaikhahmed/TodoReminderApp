package com.app.mytodo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.java.data.StaticData;
import com.java.database.SQLiteController;
import com.java.util.DateUtility;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDetail extends AppCompatActivity {

    TextView dueDate,subCompleteCount, lblStatus;
    EditText goal,description;

    static Calendar myCalendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        setTitle("My Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializedComponents();
        setComponentsFromTodoModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int completeCount = StaticData.todo.getSelected().equalsIgnoreCase("1") ? 0 : getPendingSubTaskCount();
        subCompleteCount.setText(""+completeCount);
    }

    private void setComponentsFromTodoModel() {
        if(StaticData.todo != null) {
            Date dueDte = DateUtility.getDateUtility().getDateFromString(StaticData.todo.getDueDate());
            String humanRedable = DateUtility.getDateUtility().getStringHumanReadableDate(dueDte);
            dueDate.setText(StaticData.todo.getDueDate() != null ? humanRedable : "Add date/time");
            goal.setText(StaticData.todo.getName() != null ? StaticData.todo.getName() : "Your Goal...");
            description.setText(StaticData.todo.getDescription() != null ? StaticData.todo.getDescription() : "");
        }
    }

    private void initializedComponents() {
        dueDate = (TextView)findViewById(R.id.dueDate);
        goal = (EditText) findViewById(R.id.editGoal);
        description = (EditText) findViewById(R.id.editDescription);
        subCompleteCount = (TextView) findViewById(R.id.subCompleteCount);
        lblStatus = (TextView) findViewById(R.id.lblStatus);
        goal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                   updateGoalToDB(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateDescToDB(s.length() == 0 ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if(StaticData.todo.getSelected().equalsIgnoreCase("1")){
            lblStatus.setTextColor(getResources().getColor(R.color.statusGreen));
            lblStatus.setText("Complete");
        }
    }

    private int getPendingSubTaskCount() {
        int count = 0;
        SQLiteController controller = new SQLiteController(this);
        try {

            String query = "SELECT * FROM subtodo WHERE active_status = '0' AND selected ='0' AND todo_id = ? ";
            String values[] = {StaticData.todo.getId()};
            Cursor cursor = controller.safeRetrieve(query, values);

            count = cursor.getCount();

            cursor.close();

            } catch (Exception ex){
            Log.d("ERROR!", ex.getMessage());
        }

        controller.close();
        return count;
    }

    private void updateGoalToDB(String goalStr) {

        SQLiteController controller = new SQLiteController(this);
        try {


            String query = "UPDATE todo SET name = ? WHERE todo_id = ?";
            String[] values = {goalStr, StaticData.todo.getId()};
            String status = controller.fireSafeQuery(query, values);

            if(status.equals("Done")) {
                StaticData.todo.setName(goalStr);
            } else {
                Log.d("SQL", status);
            }
        } catch (Exception ex) {
            Log.d("ERROR!", ex.getMessage());
        }
        controller.close();
    }

    private void updateDescToDB(String goalStr) {

        SQLiteController controller = new SQLiteController(this);
        try {

            String query = "UPDATE todo SET descr = ? WHERE todo_id = ?";
            String[] values = {goalStr, StaticData.todo.getId()};
            String status = controller.fireSafeQuery(query, values);

            if(status.equals("Done")) {
                StaticData.todo.setName(goalStr);
            } else {
                Log.d("SQL", status);
            }
        } catch (Exception ex) {
            Log.d("ERROR!", ex.getMessage());
        }
        controller.close();
    }

    public void onAddDueDate(View v) {

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dueDate.setText(DateUtility.getDateUtility().getStringHumanReadableDate(myCalendar.getTime()));
                updateLabel();
            }
        };

        new DatePickerDialog(TaskDetail.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void onAddSubTask(View v) {
        Intent intent = new Intent(this, SubTaskList.class);
        startActivity(intent);
    }
    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat fomateConverter = new SimpleDateFormat(myFormat, Locale.US);

        SQLiteController controller = new SQLiteController(this);
        try {


            String query = "UPDATE todo SET due_date = ? WHERE todo_id = ?";
            String[] values = {fomateConverter.format(myCalendar.getTime()), StaticData.todo.getId()};
            String status = controller.fireSafeQuery(query, values);

            if(status.equals("Done")) {
                String strDueDate = fomateConverter.format(myCalendar.getTime());

                StaticData.todo.setDueDate(strDueDate);
            } else {
                Log.d("SQL", status);
            }
        } catch (Exception ex) {
            Log.d("ERROR!", ex.getMessage());
        }
        controller.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_detail_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            deleteTask();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteTask() {

          showAlertBox();
    }

    private void showAlertBox() {

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.delete_alert, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //On Cancel button event
        dialogView.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        //On Delete button event
        dialogView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTaskFromDatabase();
                alertDialog.cancel();
                finish();
            }
        });

    }

    private void deleteTaskFromDatabase() {
        SQLiteController controller = new SQLiteController(this);
        try {
            String query = "UPDATE todo SET active_status = '1' WHERE todo_id = ?";
            String values[] = {StaticData.todo.getId()};

            String status1 = controller.fireSafeQuery(query, values);

            String query2 = "UPDATE subtodo SET active_status = '1' WHERE todo_id = ?";

            String status2 = controller.fireSafeQuery(query2, values);

            if(status1.equals("Done") && status2.equals("Done")) {
                Toast toast = Toast.makeText(this, "You have deleted task!", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.color.colorPrimary);
                view.setPadding(10,10,10,10);
                toast.show();
                //Toast.makeText(this, "You have deleted task!", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception ex) {
            Log.d("ERROR!", ex.getMessage());
        }
        controller.close();
    }

}
