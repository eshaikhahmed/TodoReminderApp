package com.java.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.mytodo.R;
import com.app.mytodo.TaskDetail;
import com.java.data.StaticData;
import com.java.database.SQLiteController;
import com.java.model.Todo;
import com.java.viewholder.TodoViewHolder;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoViewHolder> {

    private Activity activity;
    private List<Todo> taskList;
    private ItemTouchHelper touchHelper;

    public TodoAdapter(List<Todo> taskList, Activity activity) {
        this.taskList = taskList;
        this.activity = activity;
    }


    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(activity).inflate(R.layout.todo_item, viewGroup, false);

        final TodoViewHolder viewHolder= new TodoViewHolder(view);

        viewHolder.tickImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                String toggle = taskList.get(position).getSelected();
                setCheckAndUncheckBehaviour(viewHolder,toggle,position);

            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                StaticData.todo = taskList.get(position);
                Intent intent = new Intent(activity, TaskDetail.class);
                activity.startActivity(intent);
            }
        });


        return viewHolder;
    }

    private void setImageResource(ImageView myImgView, int ic_check_uncheck_24dp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myImgView.setImageDrawable(activity.getResources().getDrawable(ic_check_uncheck_24dp, activity.getApplicationContext().getTheme()));
        } else {
            myImgView.setImageDrawable(activity.getResources().getDrawable(ic_check_uncheck_24dp));
        }
    }

    private void setCheckAndUncheckBehaviour(TodoViewHolder viewHolder,String toggle ,int position){

        if(toggle.equals("0")) {
            viewHolder.taskName.setPaintFlags(viewHolder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.taskCount.setPaintFlags(viewHolder.taskCount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            setImageResource(viewHolder.tickImg,R.drawable.ic_check_uncheck_24dp);
            viewHolder.taskName.setTextColor(activity.getResources().getColor(R.color.unCheckColor));
            viewHolder.taskCount.setTextColor(activity.getResources().getColor(R.color.unCheckColor));
            updateSelected("1",position);
        } else {
            viewHolder.taskName.setPaintFlags(viewHolder.taskName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.taskCount.setPaintFlags(viewHolder.taskCount.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            setImageResource(viewHolder.tickImg,R.drawable.ic_check_black_24dp);
            viewHolder.taskName.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            viewHolder.taskCount.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            updateSelected("0",position);
        }

    }
    private void updateSelected(String s, int position) {
        try{
            SQLiteController controller = new SQLiteController(activity);
            String query = "UPDATE todo SET selected = ? WHERE todo_id = ? ";
            String values[] = {s, taskList.get(position).getId()};
            String status = controller.fireSafeQuery(query,values);
            if(status.equals("Done")) {
                Log.d("SUCCESS","Updated");
            }
            controller.close();
            taskList.get(position).setSelected(s);
        } catch (Exception ex) {
            Log.d("ERROR!",ex.getMessage());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

    }

    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder todoViewHolder, int i) {

        final Todo task = taskList.get(i);
        todoViewHolder.taskName.setText(task.getName());
        todoViewHolder.taskCount.setText(task.getSubTotalCount());

        String toggle = taskList.get(i).getSelected();
        if(toggle.equals("1")) {
            todoViewHolder.taskName.setPaintFlags(todoViewHolder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            todoViewHolder.taskCount.setPaintFlags(todoViewHolder.taskCount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            setImageResource(todoViewHolder.tickImg,R.drawable.ic_check_uncheck_24dp);
            todoViewHolder.taskName.setTextColor(activity.getResources().getColor(R.color.unCheckColor));
            todoViewHolder.taskCount.setTextColor(activity.getResources().getColor(R.color.unCheckColor));
        } else {
            todoViewHolder.taskName.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            todoViewHolder.taskCount.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            setImageResource(todoViewHolder.tickImg,R.drawable.ic_check_black_24dp);
            todoViewHolder.taskName.setPaintFlags(todoViewHolder.taskName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            todoViewHolder.taskCount.setPaintFlags(todoViewHolder.taskCount.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }

    }

    public void updateList(List<Todo> data){
        this.taskList = data;
        notifyDataSetChanged();
    }

    public void onViewMoved(int oldPosition, int newPosition) {

        Todo oldTodo = taskList.get(oldPosition);

        //Log.d("MOVED", oldTodo.getName()+" to "+taskList.get(newPosition).getName());

        updateViewOrder(newPosition, oldTodo.getId());
        updateViewOrder(oldPosition, taskList.get(newPosition).getId());

        taskList.remove(oldPosition);
        taskList.add(newPosition, oldTodo);

        notifyItemMoved(oldPosition, newPosition);
    }

    public void onViewSwiped(int position) {
        taskList.remove(position);
        notifyItemRemoved(position);
    }

    private void updateViewOrder(int viewOrder, String todo_id) {

        SQLiteController controller = new SQLiteController(activity);
        try{
            String query = "UPDATE todo SET view_order = ? WHERE todo_id = ?";
            String[] values = {viewOrder+"", todo_id};

            controller.fireSafeQuery(query, values);

            //Log.d("UPDATED", ""+viewOrder);
        }catch (Exception ex) {
            Log.d("ERROR", ex.getMessage());
        }
        controller.close();
    }

}
