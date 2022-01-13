package com.java.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.mytodo.R;

public class TodoViewHolder  extends RecyclerView.ViewHolder{

    public ImageView tickImg;
    public TextView taskName, taskCount;

    public TodoViewHolder(@NonNull View itemView) {
        super(itemView);

        taskName = (TextView) itemView.findViewById(R.id.taskName);
        taskCount = (TextView) itemView.findViewById(R.id.taskCount);
        tickImg = (ImageView) itemView.findViewById(R.id.tickImg);
    }
}
