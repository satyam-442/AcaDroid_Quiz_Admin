package com.example.acadroidquizadmin.Logical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.acadroidquizadmin.Adapter.GridAdapter;
import com.example.acadroidquizadmin.Adapter.GridLogicalAdapter;
import com.example.acadroidquizadmin.Adapter.LogicalAdapter;
import com.example.acadroidquizadmin.Category.CategoryActivity;
import com.example.acadroidquizadmin.Category.SetsActivity;
import com.example.acadroidquizadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.UUID;

public class LogicalSetsActivity extends AppCompatActivity {

    GridView gridView;
    GridLogicalAdapter adapter;
    String categoryName;
    Dialog loadingBar;
    DatabaseReference myRef;
    //List<String> sets = CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getSetss();
    List<String> sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logical_sets);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryName = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(categoryName);

        myRef = FirebaseDatabase.getInstance().getReference();

        loadingBar = new Dialog(this);
        loadingBar.setContentView(R.layout.loading_ailog);
        loadingBar.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_design));
        loadingBar.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingBar.setCancelable(false);

        sets = LogicalActivity.list.get(getIntent().getIntExtra("position", 0)).getSetss();

        gridView = findViewById(R.id.grid_view);
        adapter = new GridLogicalAdapter(sets, getIntent().getStringExtra("title"), new GridLogicalAdapter.GridListener() {
            @Override
            public void addSet() {
                loadingBar.show();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final String uid = UUID.randomUUID().toString();
                database.getReference().child("Logical").child(getIntent().getStringExtra("key")).child("Sets").child(uid).setValue("SET ID").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //adapter.sets = adapter.sets+1;
                            sets.add(uid);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(LogicalSetsActivity.this, "Set Added", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            Toast.makeText(LogicalSetsActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                        loadingBar.dismiss();
                    }
                });
            }

            @Override
            public void onLongClick(final int setNo, final String setId) {
                new AlertDialog.Builder(LogicalSetsActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Set " + setNo)
                        .setMessage("Press confirm to delete or cancel to be safe")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingBar.show();
                                myRef
                                        .child("Sets").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myRef.child("Categories").child(LogicalActivity.list.get(getIntent().getIntExtra("position", 0)).getKeyy()).child("Sets").child(setId).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                sets.remove(setId);
                                                                adapter.notifyDataSetChanged();
                                                            } else {
                                                                Toast.makeText(LogicalSetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                            }
                                                            loadingBar.dismiss();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(LogicalSetsActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
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
        gridView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}