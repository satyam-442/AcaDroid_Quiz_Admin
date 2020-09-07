package com.example.acadroidquizadmin.SpeedMath;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.acadroidquizadmin.Category.AddQuestionActivity;
import com.example.acadroidquizadmin.Category.TypeQuestionActivity;
import com.example.acadroidquizadmin.Model.QuestionModel;
import com.example.acadroidquizadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class TypeQuestionSMActivity extends AppCompatActivity {

    Dialog loadingBar;
    String categoryNme, id;
    String setId;
    private EditText question;
    private RadioGroup options;
    private LinearLayout answers;
    Button addQuestion;
    private QuestionModel questionModel;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_question_s_m);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        categoryNme = getIntent().getStringExtra("category");
        setId = getIntent().getStringExtra("setId");
        position = getIntent().getIntExtra("position",-1);
        if (setId == null ){
            finish();
            return;
        }

        if (position != -1){
            questionModel = AddQuestionActivity.list.get(position);
            setData();
        }

        /*getSupportActionBar().setTitle(categoryNme + "/set " + setNo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        question = (EditText) findViewById(R.id.questionType);
        options = findViewById(R.id.options);
        answers = findViewById(R.id.answers);
        addQuestion = findViewById(R.id.button);
        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (question.getText().toString().isEmpty()){
                    question.setError("Required!");
                    return;
                }
                addQuestionToDatabase();
            }
        });

        loadingBar = new Dialog(this);
        loadingBar.setContentView(R.layout.loading_ailog);
        loadingBar.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_design));
        loadingBar.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingBar.setCancelable(false);

    }

    private void setData() {

        question.setText(questionModel.getQuestionn());
        ((EditText)answers.getChildAt(0)).setText(questionModel.getOptionaa());
        ((EditText)answers.getChildAt(1)).setText(questionModel.getOptionbb());
        ((EditText)answers.getChildAt(2)).setText(questionModel.getOptioncc());
        ((EditText)answers.getChildAt(3)).setText(questionModel.getOptiondd());

        for (int i = 0; i < answers.getChildCount(); i++){
            if ( ((EditText)answers.getChildAt(i)).getText().toString().equals(questionModel.getCorrectAnss())){
                RadioButton radioButton = (RadioButton) options.getChildAt(i);
                radioButton.setChecked(true);
                break;
            }
        }
    }

    private void addQuestionToDatabase() {
        int correct = -1;
        for (int i = 0; i < options.getChildCount();i++) {
            EditText answer = (EditText) answers.getChildAt(i);
            if (answer.getText().toString().isEmpty()){
                answer.setError("Required");
                return;
            }

            RadioButton radioButton = (RadioButton) options.getChildAt(i);;
            if (radioButton.isChecked()){
                correct = i;
                break;
            }
        }
        if (correct == -1){
            Toast.makeText(this, "Please mark correct answer.", Toast.LENGTH_SHORT).show();
            return;
        }

        final HashMap<String, Object> map = new HashMap<>();
        map.put("correctAns",((EditText)answers.getChildAt(correct)).getText().toString());
        map.put("optiona",((EditText)answers.getChildAt(0)).getText().toString());
        map.put("optionb",((EditText)answers.getChildAt(1)).getText().toString());
        map.put("optionc",((EditText)answers.getChildAt(2)).getText().toString());
        map.put("optiond",((EditText)answers.getChildAt(3)).getText().toString());
        map.put("question",question.getText().toString());
        map.put("setId",setId);

        if (position != -1){
            id = questionModel.getIdd();
        }else {
            id = UUID.randomUUID().toString();
        }

        loadingBar.show();
        FirebaseDatabase.getInstance().getReference().child("Sets").child(setId).child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    QuestionModel questionModel = new QuestionModel(id,
                            map.get("question").toString(),
                            map.get("optiona").toString(),
                            map.get("optionb").toString(),
                            map.get("optionc").toString(),
                            map.get("optiond").toString(),
                            map.get("correctAns").toString(),
                            map.get("setId").toString());

                    if (position != -1){
                        AddQuestionSMActivity.list.set(position,questionModel);
                    }else {
                        AddQuestionSMActivity.list.add(questionModel);
                    }
                    finish();
                    Toast.makeText(TypeQuestionSMActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(TypeQuestionSMActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}