package com.example.acadroidquizadmin.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acadroidquizadmin.Model.CategoryModel;
import com.example.acadroidquizadmin.R;
import com.example.acadroidquizadmin.SetsActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewHolder> {

    private List<CategoryModel> categoryList;
    DeleteListener deleteListener;

    public CategoryAdapter(List<CategoryModel> categoryList, DeleteListener deleteListener) {
        this.categoryList = categoryList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.setData(categoryList.get(position).getUrll(),categoryList.get(position).getNamee(), categoryList.get(position).getKeyy(), position);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView title;
        ImageView deleteBtn;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title);
            deleteBtn = itemView.findViewById(R.id.deleteCategory);
        }

        private void setData(String url, final String title, final String key, final int position){
            Glide.with(itemView.getContext()).load(url).into(imageView);
            this.title.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent categories = new Intent(itemView.getContext(), SetsActivity.class);
                    categories.putExtra("title", title);
                    categories.putExtra("position", position);
                    categories.putExtra("key", key);
                    imageView.getContext().startActivity(categories);
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteListener.onDelete(key, position);
                }
            });
        }
    }

    public interface DeleteListener{
        public void onDelete(String key, int position);
    }

}
