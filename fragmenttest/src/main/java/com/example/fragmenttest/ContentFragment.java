package com.example.fragmenttest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContentFragment extends Fragment {

    private  static final String[] data={"a","b","c","d","e"};
    private ListView listView;
    private List<String> list;
    private ArrayAdapter<String> adapter;
    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_content,container,false);
        listView = view.findViewById(R.id.content_list);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        Collections.addAll(list, data);
        adapter.notifyDataSetChanged();
        return view;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


}