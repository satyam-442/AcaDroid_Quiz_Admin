package com.example.acadroidquizadmin.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acadroidquizadmin.DemoActivity;
import com.example.acadroidquizadmin.Model.QuestionModel;
import com.example.acadroidquizadmin.R;
import com.example.acadroidquizadmin.TypeQuestionActivity;

import java.util.List;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {

    private List<QuestionModel> list;

    private String category;
    private DeleteListener listener;

    public QuestionsAdapter(List<QuestionModel> list, String category, DeleteListener listener) {
        this.list = list;
        this.category = category;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_question_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String question = list.get(position).getQuestion();
        String answer = list.get(position).getCorrectAns();

        holder.setData(question,answer,position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView question, answer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);
        }

        private void setData(String question, String answer, final int position){
            this.question.setText(position+1+". "+question);
            this.answer.setText(answer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent editIntent = new Intent(itemView.getContext(), DemoActivity.class);
                    Intent editIntent = new Intent(itemView.getContext(), TypeQuestionActivity.class);
                    editIntent.putExtra("category",category);
                    editIntent.putExtra("sets",list.get(position).getSetNo());
                    editIntent.putExtra("position",position);
                    itemView.getContext().startActivity(editIntent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClick(position,list.get(position).getId());
                    return false;
                }
            });
        }
    }

    public interface DeleteListener{
        void onLongClick(int position, String id);
    }

}
