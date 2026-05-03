package com.yonas.to_dolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yonas.to_dolist.Model.ToDoModel;
import com.yonas.to_dolist.Utils.DataBaseHelper;

import java.util.Calendar;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private EditText mEditText;
    private TextView mDueDateText, mTimeText;
    private Spinner mPrioritySpinner, mCategorySpinner;
    private Button mSaveButton;
    private String selectedDate = "";
    private String selectedTime = "";

    private DataBaseHelper myDB;

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditText = view.findViewById(R.id.editText);
        mDueDateText = view.findViewById(R.id.set_date);
        mTimeText = view.findViewById(R.id.set_time);
        mPrioritySpinner = view.findViewById(R.id.priority_spinner);
        mCategorySpinner = view.findViewById(R.id.category_spinner);
        mSaveButton = view.findViewById(R.id.addButton);

        myDB = new DataBaseHelper(getActivity());

        // Set up Category Spinner
        String[] categories = {
                getString(R.string.cat_academic),
                getString(R.string.cat_study),
                getString(R.string.cat_health),
                getString(R.string.cat_spiritual)
        };
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(catAdapter);

        // Set up Priority Spinner
        String[] priorities = {"Low Priority", "Medium Priority", "High Priority"};
        ArrayAdapter<String> prioAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        prioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPrioritySpinner.setAdapter(prioAdapter);
        mPrioritySpinner.setSelection(1); // Default Medium

        // Date Picker
        mDueDateText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                mDueDateText.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Time Picker
        mTimeText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                mTimeText.setText(selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        Bundle bundle = getArguments();
        boolean isUpdate = bundle != null;

        if (isUpdate) {
            mEditText.setText(bundle.getString("task"));
            selectedDate = bundle.getString("dueDate", "");
            selectedTime = bundle.getString("taskTime", "");
            if (!selectedDate.isEmpty()) mDueDateText.setText(selectedDate);
            if (!selectedTime.isEmpty()) mTimeText.setText(selectedTime);
            
            mPrioritySpinner.setSelection(bundle.getInt("priority", 1));
            String cat = bundle.getString("category");
            for(int i=0; i<categories.length; i++) {
                if(categories[i].equals(cat)) mCategorySpinner.setSelection(i);
            }
        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveButton.setEnabled(!s.toString().isEmpty());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mSaveButton.setOnClickListener(v -> {
            String text = mEditText.getText().toString();
            String category = mCategorySpinner.getSelectedItem().toString();
            int priority = mPrioritySpinner.getSelectedItemPosition();

            if (isUpdate) {
                myDB.updateTask(bundle.getInt("id"), text, selectedDate, selectedTime, priority, category);
            } else {
                ToDoModel item = new ToDoModel();
                item.setTask(text);
                item.setStatus(0);
                item.setCategory(category);
                item.setDueDate(selectedDate);
                item.setTaskTime(selectedTime);
                item.setPriority(priority);
                myDB.insertTask(item);
            }
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}
