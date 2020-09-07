package com.example.acadroidquizadmin.SpeedMath;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.acadroidquizadmin.Adapter.SpeedMathAdapter;
import com.example.acadroidquizadmin.MainActivity;
import com.example.acadroidquizadmin.Model.SpeedMathModel;
import com.example.acadroidquizadmin.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SpeedmathActivity extends AppCompatActivity {

    RecyclerView rv;
    DatabaseReference categories, setsRef;
    public static List<SpeedMathModel> list;
    Dialog loadingBar, addCategoryDialog;

    //DIALOG FOR ADD CATEGORY
    EditText addCatName;
    CircleImageView addCategoryImage;
    Button addCategory;

    //UPLOAD IMAGE VARIABLES DECLARATION
    Uri image;
    String downloadUrl;

    FirebaseDatabase database;
    DatabaseReference databaseRef;
    SpeedMathAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedmath);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SpeedMath");

        //loadAds();
        categories = FirebaseDatabase.getInstance().getReference().child("SpeedMath");
        setsRef = FirebaseDatabase.getInstance().getReference().child("Sets");

        loadingBar = new Dialog(this);
        loadingBar.setContentView(R.layout.loading_ailog);
        loadingBar.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_design));
        loadingBar.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingBar.setCancelable(false);

        setCategoryDaialog();

        rv = findViewById(R.id.rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        rv.setLayoutManager(manager);

        list = new ArrayList<>();

        adapter = new SpeedMathAdapter(list, new SpeedMathAdapter.DeleteListener() {
            @Override
            public void onDelete(final String key, final int position) {
                new AlertDialog.Builder(SpeedmathActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete SpeedMath Category")
                        .setMessage("Press confirm to delete or cancel to be safe")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingBar.show();
                                categories.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            for (String setIds : list.get(position).getSetss()){
                                                setsRef.child(setIds).removeValue();
                                            }
                                            list.remove(position);
                                            adapter.notifyDataSetChanged();
                                            loadingBar.dismiss();
                                        } else {
                                            Toast.makeText(SpeedmathActivity.this, "Error Occurred! :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(R.drawable.warning)
                        .show();
            }
        });
        rv.setAdapter(adapter);

        loadingBar.show();

        categories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    List<String> sets = new ArrayList<>();
                    for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("Sets").getChildren()

                    ){
                        sets.add(dataSnapshot2.getKey());
                    }

                    list.add(new SpeedMathModel(dataSnapshot1.child("name").getValue().toString(),
                            dataSnapshot1.child("url").getValue().toString(),
                            dataSnapshot1.getKey(),
                            sets
                    ));
                }
                adapter.notifyDataSetChanged();
                loadingBar.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SpeedmathActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                finish();
            }
        });
    }

    /*private void loadAds() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addBtn) {
            addCategoryDialog.show();
        }
        if (item.getItemId() == R.id.logout) {
            new AlertDialog.Builder(SpeedmathActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadingBar.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent main = new Intent(SpeedmathActivity.this, MainActivity.class);
                            startActivity(main);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(R.drawable.logoutblack)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCategoryDaialog() {
        addCategoryDialog = new Dialog(this);
        addCategoryDialog.setContentView(R.layout.add_category_layout);
        addCategoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_design));
        addCategoryDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addCategoryDialog.setCancelable(true);

        addCategoryImage = addCategoryDialog.findViewById(R.id.image);
        addCatName = addCategoryDialog.findViewById(R.id.categoryname);
        addCategory = addCategoryDialog.findViewById(R.id.add);

        addCategoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 101);
            }
        });

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addCatName.getText().toString().isEmpty() || addCatName.getText() == null) {
                    addCatName.setError("Required");
                    return;
                }
                for (SpeedMathModel model : list) {
                    if (addCatName.getText().toString().equals(model.getNamee())) {
                        addCatName.setError("Category Already Present!");
                        return;
                    }
                }
                if (image == null) {
                    Toast.makeText(SpeedmathActivity.this, "Please select Image", Toast.LENGTH_SHORT).show();
                    return;
                }
                addCategoryDialog.dismiss();
                uploadData();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                image = data.getData();
                addCategoryImage.setImageURI(image);
            }
        }
    }

    private void uploadData() {
        loadingBar.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageRef = storageReference.child("SpeedMath").child(image.getLastPathSegment());

        UploadTask uploadTask = imageRef.putFile(image);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUrl = task.getResult().toString();
                            uploadCategoryName();
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(SpeedmathActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    Toast.makeText(SpeedmathActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void uploadCategoryName() {
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("name", addCatName.getText().toString());
        categoryMap.put("Sets", 0);
        categoryMap.put("url", downloadUrl);

        final String id = UUID.randomUUID().toString();

        database = FirebaseDatabase.getInstance();
        database.getReference().child("SpeedMath").child(id).setValue(categoryMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    list.add(new SpeedMathModel(addCatName.getText().toString(), downloadUrl, id, new ArrayList<String>()));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(SpeedmathActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }
}