package com.yonas.to_dolist.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yonas.to_dolist.AddNewTask;
import com.yonas.to_dolist.MainActivity;
import com.yonas.to_dolist.Model.ToDoModel;
import com.yonas.to_dolist.R;
import com.yonas.to_dolist.Utils.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> mList;
    private List<ToDoModel> mListFull; // For search filtering
    private MainActivity activity;
    private DataBaseHelper myDB;

    public ToDoAdapter (DataBaseHelper myDB, MainActivity activity){
        this.activity=activity;
        this.myDB=myDB;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ToDoModel item = mList.get(position);
        holder.checkBox.setText(item.getTask());
        
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(toBoolean(item.getStatus()));

        // Bind Due Date
        if (item.getDueDate() != null && !item.getDueDate().isEmpty()) {
            holder.dueDateText.setText("📅 " + item.getDueDate());
            holder.dueDateText.setVisibility(View.VISIBLE);
        } else {
            holder.dueDateText.setVisibility(View.GONE);
        }

        // Bind Time
        if (item.getTaskTime() != null && !item.getTaskTime().isEmpty()) {
            holder.taskTimeText.setText("⏰ " + item.getTaskTime());
            holder.taskTimeText.setVisibility(View.VISIBLE);
        } else {
            holder.taskTimeText.setVisibility(View.GONE);
        }

        // Bind Category
        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            holder.categoryText.setText("🏷️ " + item.getCategory());
            holder.categoryText.setVisibility(View.VISIBLE);
        } else {
            holder.categoryText.setVisibility(View.GONE);
        }

        // Bind Priority
        switch (item.getPriority()) {
            case 2:
                holder.priorityText.setText("HIGH");
                holder.priorityText.setBackgroundColor(Color.RED);
                break;
            case 1:
                holder.priorityText.setText("MED");
                holder.priorityText.setBackgroundColor(Color.parseColor("#FFA500")); // Orange
                break;
            default:
                holder.priorityText.setText("LOW");
                holder.priorityText.setBackgroundColor(Color.GREEN);
                break;
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    myDB.updateStatus(item.getId(), 1);
                } else {
                    myDB.updateStatus(item.getId(), 0);
                }
            }
        });
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    public Context getContext() {
        return activity;
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void setTasks(List<ToDoModel> mList) {
        this.mList = mList;
        this.mListFull = new ArrayList<>(mList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        mList.clear();
        if (query.isEmpty()) {
            mList.addAll(mListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (ToDoModel item : mListFull) {
                if (item.getTask().toLowerCase().contains(lowerCaseQuery) || 
                    (item.getCategory() != null && item.getCategory().toLowerCase().contains(lowerCaseQuery))) {
                    mList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void deleteTask(int position) {
        ToDoModel item = mList.get(position);
        myDB.deleteTask(item.getId());
        mList.remove(position);
        mListFull.remove(item);
        notifyItemRemoved(position);
    }

    public void editItems(int position) {
        ToDoModel item = mList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putString("dueDate", item.getDueDate());
        bundle.putString("taskTime", item.getTaskTime());
        bundle.putInt("priority", item.getPriority());
        bundle.putString("category", item.getCategory());

        AddNewTask task = new AddNewTask();
        task.setArguments(bundle);
        task.show(activity.getSupportFragmentManager(), task.getTag());
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView dueDateText, categoryText, priorityText, taskTimeText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            dueDateText = itemView.findViewById(R.id.due_date_text);
            taskTimeText = itemView.findViewById(R.id.task_time_text);
            categoryText = itemView.findViewById(R.id.category_text);
            priorityText = itemView.findViewById(R.id.priority_text);
        }
    }
}
