package com.example.acadroidquizadmin.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.acadroidquizadmin.Category.AddQuestionActivity;
import com.example.acadroidquizadmin.R;

import java.util.List;

public class GridLogicalAdapter extends BaseAdapter {

    public List<String> sets;
    String category;
    private GridListener listener;

    public GridLogicalAdapter(List<String> sets, String category, GridListener listener) {
        this.sets = sets;
        this.category = category;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return sets.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sets_item, parent, false);
        } else {
            view = convertView;
        }

        if (position == 0) {
            ((TextView) view.findViewById(R.id.textview)).setText("+");
        } else {
            ((TextView) view.findViewById(R.id.textview)).setText(String.valueOf(position));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    //addCode
                    listener.addSet();
                } else {
                    Intent intent = new Intent(parent.getContext(), AddQuestionActivity.class);
                    intent.putExtra("category", category);
                    intent.putExtra("setId", sets.get(position - 1));
                    parent.getContext().startActivity(intent);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (position != 0){
                    listener.onLongClick(position,sets.get(position-1));
                }
                return false;
            }
        });
        return view;
    }

    public interface GridListener {
        public void addSet();

        void onLongClick(int setNo, String setId);
    }

}
