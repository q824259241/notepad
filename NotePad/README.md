# NotePad 期中报告
## 时间戳的实现
### 实现过程
#### 1. Notelist时间戳
在notelist_item.xml中增加时间戳的布局
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_height="match_parent"
    android:layout_width="match_parent"
    android:orientation="vertical">
    <TextView xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@android:id/text1"
        android:layout_width="match_parent"
        android:layout_height="?android:attr/listPreferredItemHeight"
        android:gravity="center_vertical"
        android:paddingLeft="5dip"
        android:singleLine="true"
        android:textAppearance="?android:attr/textAppearanceLarge" />
    <TextView
        android:id="@android:id/text2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:paddingLeft="5dip"/>
</LinearLayout>
```
PROJECTION中增加修改的时间的显示
```java
 private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//2
    };
```
增加时间戳部分需要的参数
```java
  String[] dataColumns = {NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE};
  int[] viewIDs = {android.R.id.text1, android.R.id.text2};
```
#### 2. Insert时间戳
时间戳的格式转化
```java
 SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 String dateStr = dateformat.format(System.currentTimeMillis());
```
```java
  if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE,dateStr);
  }//写入创建时间
  
  if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,dateStr);
  }//写入修改时间
```
#### 3. 在updateNote函数中对写入的修改时间进行格式转化
```java
 SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 String dateStr = dateformat.format(System.currentTimeMillis());//时间日期的转换
 values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, dateStr);
```
### 实现效果
 ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200604133411341.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDQzMzE4NQ==,size_16,color_FFFFFF,t_70)

## 查询功能的实现
#### 实现过程
#### 1. 新增note_search.xml布局用于实现查询功能
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <SearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        android:queryHint="@string/hint" />

    <ListView
        android:id="@android:id/list"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```
#### 2. 增加搜索按钮
```xml
  <item
        android:id="@+id/menu_search"
        android:icon="@android:drawable/ic_search_category_default"
        android:showAsAction="always"
        android:title="Search" />
```
#### 3. 编写NoteSearch类
首先定义查询后需要显示的数据包括哪些部分
```java
 private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2 修改时间
    };

```
Searchview增加监听器
```java
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
```
利用toast返回查询到的笔记数目
```java
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
```
onQueryTextChange函数中主要利用sql语句实现了模糊查询
```java
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
```
点击笔记时的方法和NoteList中一致
```java
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
```
#### 4.在NoteList中的onOptionsItemSelected函数中增加一个搜索的case
```java
case R.id.menu_search:
        Intent intent = new Intent();
        intent.setClass(NotesList.this, NoteSearch.class);
        NotesList.this.startActivity(intent);
        return true;
```
### 实现结果
实现动态查询及模糊查询

 ![在这里插入图片描述](https://img-blog.csdnimg.cn/20200604133648663.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDQzMzE4NQ==,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200604133703874.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDQzMzE4NQ==,size_16,color_FFFFFF,t_70)

