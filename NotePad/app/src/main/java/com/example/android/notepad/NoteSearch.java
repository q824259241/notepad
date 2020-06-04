package com.example.android.notepad;


import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class NoteSearch extends ListActivity implements SearchView.OnQueryTextListener {
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2 修改时间
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        SearchView searchview = (SearchView) findViewById(R.id.search_view);
        searchview.setSubmitButtonEnabled(true);//显示提交按钮
        searchview.setOnQueryTextListener(NoteSearch.this);//为searchview增加监听器
    }

    @Override
    public boolean onQueryTextSubmit(String query) {//提交方法
        if(getListAdapter().getCount()==0){
            Toast.makeText(this, "no found", Toast.LENGTH_SHORT).show();//若无查询结果则提示无结果
        }
        else{
            Toast.makeText(this, getListAdapter().getCount()+" "+"found", Toast.LENGTH_SHORT).show();//提示找到几条数据
        }
        SearchView searchview = (SearchView) findViewById(R.id.search_view);
        if (searchview!= null) {
            // 得到输入管理对象
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchview.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法
            }
            searchview.clearFocus(); // 不获取焦点
        }
        return true;

    }

    @Override
    public boolean onQueryTextChange(String newText) {//文本框有变化时做的方法
        String selection = NotePad.Notes.COLUMN_NAME_TITLE + " Like ? ";
        String[] selectionArgs = {"%" + newText + "%"};
        Cursor cursor = managedQuery(
                getIntent().getData(),
                PROJECTION,
                selection,     // The columns for the where clause
                selectionArgs, // The values for the where clause
                NotePad.Notes.DEFAULT_SORT_ORDER  // Use the default sort order.
        );
        String[] dataColumns = {NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE};
        int[] viewIDs = {android.R.id.text1, android.R.id.text2};
        SimpleCursorAdapter adapter
                = new SimpleCursorAdapter(
                this,                             // The Context for the ListView
                R.layout.noteslist_item,          // Points to the XML for a list item
                cursor,                           // The cursor to get items from
                dataColumns,
                viewIDs
        );
        setListAdapter(adapter);
        return true;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {//点击搜索到的笔记时调用的方法

        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
