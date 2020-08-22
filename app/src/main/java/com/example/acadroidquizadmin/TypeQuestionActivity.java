package com.example.acadroidquizadmin;
//18:18
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

import com.example.acadroidquizadmin.Model.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class TypeQuestionActivity extends AppCompatActivity {

    Dialog loadingBar;
    String categoryNme, id;
    int setNo;
    private EditText question;
    private RadioGroup options;
    private LinearLayout answers;
    Button addQuestion;
    private QuestionModel questionModel;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_question);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        categoryNme = getIntent().getStringExtra("category");
        setNo = getIntent().getIntExtra("sets",-1);
        position = getIntent().getIntExtra("position",-1);
        if (setNo == -1 ){
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

        question.setText(questionModel.getQuestion());
        ((EditText)answers.getChildAt(0)).setText(questionModel.getOptiona());
        ((EditText)answers.getChildAt(1)).setText(questionModel.getOptionb());
        ((EditText)answers.getChildAt(2)).setText(questionModel.getOptionc());
        ((EditText)answers.getChildAt(3)).setText(questionModel.getOptiond());

        for (int i = 0; i < answers.getChildCount(); i++){
            if ( ((EditText)answers.getChildAt(i)).getText().toString().equals(questionModel.getCorrectAns())){
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
        map.put("setNo",setNo);

        if (position != -1){
            id = questionModel.getId();
        }else {
            id = UUID.randomUUID().toString();
        }

        loadingBar.show();
        FirebaseDatabase.getInstance().getReference().child("Sets").child(categoryNme).child("questions").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                            (int) map.get("setNo"));

                    if (position != -1){
                        AddQuestionActivity.list.set(position,questionModel);
                    }else {
                        AddQuestionActivity.list.add(questionModel);
                    }
                    finish();
                    Toast.makeText(TypeQuestionActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(TypeQuestionActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
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