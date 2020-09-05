package com.example.acadroidquizadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acadroidquizadmin.Adapter.QuestionsAdapter;
import com.example.acadroidquizadmin.Model.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AddQuestionSMActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button add_btn, excel_btn;
    QuestionsAdapter adapter;
    public static List<QuestionModel> list;
    Dialog loadingBar;
    String categoryNme;
    String setId;
    TextView loadText;
    DatabaseReference myRef;
    public static final int CELL_COUNT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question_s_m);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myRef = FirebaseDatabase.getInstance().getReference();

        categoryNme = getIntent().getStringExtra("category");
        setId = getIntent().getStringExtra("setId");
        getSupportActionBar().setTitle(categoryNme);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);

        loadingBar = new Dialog(this);
        loadingBar.setContentView(R.layout.loading_ailog);
        loadingBar.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_design));
        loadingBar.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingBar.setCancelable(false);
        loadText = loadingBar.findViewById(R.id.loadText);

        list = new ArrayList<>();

        adapter = new QuestionsAdapter(list, categoryNme, new QuestionsAdapter.DeleteListener() {
            @Override
            public void onLongClick(final int position, final String id) {
                new AlertDialog.Builder(AddQuestionSMActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Question")
                        .setMessage("Press confirm to delete or cancel to be safe")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingBar.show();
                                myRef.child("Sets").child(setId).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            list.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            Toast.makeText(AddQuestionSMActivity.this, "Question Deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(AddQuestionSMActivity.this, "Error Occurred! :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(R.drawable.warning)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
        getDataFromDatabase(categoryNme, setId);


        add_btn = findViewById(R.id.add_Btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddQuestionIntent();
            }
        });
        excel_btn = findViewById(R.id.excel_btn);
        excel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ImportFromExcel();
                if (ActivityCompat.checkSelfPermission(AddQuestionSMActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    SelectFromExcel();
                } else {
                    ActivityCompat.requestPermissions(AddQuestionSMActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectFromExcel();
            } else {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SelectFromExcel() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                String filepath = data.getData().getPath();
                if (filepath.endsWith(".xlsx")) {
                    readFile(data.getData());
                } else {
                    Toast.makeText(this, "please choose correct file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getDataFromDatabase(String categoryNme, final String setId) {
        loadingBar.show();
        FirebaseDatabase.getInstance().getReference()
                .child("Sets").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.getKey();
                    String quest = dataSnapshot.child("question").getValue().toString();
                    String optA = dataSnapshot.child("optiona").getValue().toString();
                    String optB = dataSnapshot.child("optionb").getValue().toString();
                    String optC = dataSnapshot.child("optionc").getValue().toString();
                    String optD = dataSnapshot.child("optiond").getValue().toString();
                    String correctAnswer = dataSnapshot.child("correctAns").getValue().toString();

                    list.add(new QuestionModel(id, quest, optA, optB, optC, optD, correctAnswer, setId));
                }
                loadingBar.dismiss();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddQuestionSMActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                finish();
            }
        });
    }

    private void AddQuestionIntent() {
        //Add question to database code here
        Intent questionIntent = new Intent(this, TypeQuestionActivity.class);
        questionIntent.putExtra("category", categoryNme);
        questionIntent.putExtra("setId", setId);
        startActivity(questionIntent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void readFile(final Uri fileUri) {
        loadText.setText("scanning document...");
        loadingBar.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final HashMap<String, Object> parentMap = new HashMap<>();
                final List<QuestionModel> tempList = new ArrayList<>();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(fileUri);
                    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                    int rowsCount = sheet.getPhysicalNumberOfRows();
                    if (rowsCount > 0) {
                        for (int r = 0; r < rowsCount; r++) {
                            Row row = sheet.getRow(r);

                            if (row.getPhysicalNumberOfCells() == CELL_COUNT) {
                                String question = getCellData(row, 0, formulaEvaluator);
                                String a = getCellData(row, 1, formulaEvaluator);
                                String b = getCellData(row, 2, formulaEvaluator);
                                String c = getCellData(row, 3, formulaEvaluator);
                                String d = getCellData(row, 4, formulaEvaluator);
                                String correctAns = getCellData(row, 5, formulaEvaluator);

                                if (correctAns.equals(a) || correctAns.equals(b) || correctAns.equals(c) || correctAns.equals(d)) {
                                    HashMap<String, Object> questionMap = new HashMap<>();
                                    questionMap.put("question", question);
                                    questionMap.put("optiona", a);
                                    questionMap.put("optionb", b);
                                    questionMap.put("optionc", c);
                                    questionMap.put("optiond", d);
                                    questionMap.put("correctAns", correctAns);
                                    questionMap.put("setId", setId);

                                    final String id = UUID.randomUUID().toString();
                                    parentMap.put(id, questionMap);

                                    tempList.add(new QuestionModel(id, question, a, b, c, d, correctAns, setId));
                                } else {
                                    final int finalR = r;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadText.setText("loading...");
                                            loadingBar.dismiss();
                                            Toast.makeText(AddQuestionSMActivity.this, "Row no. " + (finalR + 1) + " has no correct option", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                            } else {
                                final int finalR1 = r;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadText.setText("loading...");
                                        loadingBar.dismiss();
                                        Toast.makeText(AddQuestionSMActivity.this, "Row no. " + (finalR1 + 1) + " has incorrect data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadText.setText("Uploading question");
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Sets").child(setId).updateChildren(parentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            list.addAll(tempList);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            loadText.setText("loading");
                                            Toast.makeText(AddQuestionSMActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadText.setText("loading");
                                loadingBar.dismiss();
                                Toast.makeText(AddQuestionSMActivity.this, "File is empty!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadText.setText("loading");
                            loadingBar.dismiss();
                            Toast.makeText(AddQuestionSMActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadText.setText("loading");
                            loadingBar.dismiss();
                            Toast.makeText(AddQuestionSMActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    private String getCellData(Row row, int cellPosition, FormulaEvaluator formulaEvaluator) {
        String value = "";
        Cell cell = row.getCell(cellPosition);
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return value + cell.getBooleanCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                return value + cell.getNumericCellValue();

            case Cell.CELL_TYPE_STRING:
                return value + cell.getStringCellValue();

            default:
                return value;
        }
    }

}