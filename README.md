# Android Room with a view
### 3.Gradle文件

1.在build.gradle(Module:app)添加kapt插件
```
apply plugin: 'kotlin-kapt'
```
在该packagingOptions块内添加该块，android以将原子函数模块从包中排除，并防止出现警告
```
android {
    // other configuration (buildTypes, defaultConfig, etc.)

    packagingOptions {
        exclude 'META-INF/atomicfu.kotlin_module'
    }
}
```
在代码dependencies块的末尾添加以下代码。
```
// Room components
implementation "androidx.room:room-runtime:$rootProject.roomVersion"
implementation "androidx.room:room-ktx:$rootProject.roomVersion"
kapt "androidx.room:room-compiler:$rootProject.roomVersion"
androidTestImplementation "androidx.room:room-testing:$rootProject.roomVersion"

// Lifecycle components
implementation "androidx.lifecycle:lifecycle-extensions:$rootProject.archLifecycleVersion"
kapt "androidx.lifecycle:lifecycle-compiler:$rootProject.archLifecycleVersion"
androidTestImplementation "androidx.arch.core:core-testing:$rootProject.androidxArchVersion"

// ViewModel Kotlin support
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$rootProject.archLifecycleVersion"

// Coroutines
api "org.jetbrains.kotlinx:kotlinx-coroutines-android:$rootProject.coroutines"

// UI
implementation "com.google.android.material:material:$rootProject.materialVersion"

// Testing
androidTestImplementation "androidx.arch.core:core-testing:$rootProject.coreTestingVersion"
```
在build.gradle（Project：RoomWordsSample）文件中，将版本号添加到文件的末尾，如下面的代码所示。
```
ext {
    roomVersion = '2.2.1'
    archLifecycleVersion = '2.2.0-rc02'
    androidxArchVersion = '2.1.0'
    coreTestingVersion = "2.1.0"
    coroutines = '1.3.2'
    materialVersion = "1.0.0"
}
```

### 创建一个实体类

创建一个新的Kotlin类文件，称为Word包含Word 数据类。此类将为您的单词描述Entity（代表SQLite表）。类中的每个公共属性都代表表中的一列。Room最终将使用这些属性来创建表并从数据库中的行实例化对象。
```
data class Word(val word: String)
```
为了使Word该类对Room数据库有意义，您需要对其进行注释。注释标识此类的每个部分如何与数据库中的条目相关。Room使用此信息来生成代码。
```
@Entity(tableName = "word_table")
class Word(@PrimaryKey @ColumnInfo(name = "word") val word: String)
```
**让我们看看这些注释的作用：**

>- @Entity(tableName = "word_table")
每个@Entity类代表一个SQLite表。注释您的类声明以表明它是一个实体。如果希望表名与类名不同，则可以指定表名。这将表命名为“ word_table”。
>- @PrimaryKey
每个实体都需要一个主键。为了简单起见，每个单词都充当其自己的主键。
>- @ColumnInfo(name = "word")
如果您希望表中的列名称与成员变量的名称不同，则指定该名称。这将列命名为“单词”。
数据库中存储的每个属性都必须具有公共可见性，这是​​Kotlin的默认设置。

### 创建DAO
在DAO（data access object数据访问对象）中，指定SQL查询并将其与方法调用关联。编译器检查SQL，并从便捷注释中生成常见查询（例如）的查询@Insert。Room使用DAO为您的代码创建一个干净的API。

DAO必须是接口或抽象类。

默认情况下，所有查询必须在单独的线程上执行。

Room具有协程支持，允许您的查询使用suspend修饰符进行注释，然后从协程或另一个悬浮函数调用。

让我们编写一个DAO，它提供以下查询：

按字母顺序排列所有单词
插入一个词
删除所有单词
创建一个名为的新Kotlin类文件WordDao。将以下代码复制并粘贴到导入文件中，WordDao并根据需要对其进行修复以使其编译。

```
@Dao
interface WordDao {

    @Query("SELECT * from word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): List<Word>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    @Query("DELETE FROM word_table")
    suspend fun deleteAll()
}
```

让我们来看一下：

WordDao是一个接口；DAO必须是接口或抽象类。
该@Dao注解标识它作为一个房间DAO类。
suspend fun insert(word: Word)：声明一个暂停功能以插入一个单词。
该@Insert注释是一个特殊的DAO方法的注释，你不必提供任何SQL！（还有@Delete和@Update注释，用于删除和更新行，但您未在此应用中使用它们。）
onConflict = OnConflictStrategy.IGNORE：如果冲突中选择的策略与列表中已有的单词完全相同，则会忽略该单词。要了解有关可用冲突策略的更多信息，请查阅文档。
suspend fun deleteAll()：声明一个暂停功能以删除所有单词。
没有用于删除多个实体的便捷注释，因此使用generic进行注释@Query。
@Query("DELETE FROM word_table")： @Query要求您提供SQL查询作为注释的字符串参数，以允许进行复杂的读取查询和其他操作。
fun getAlphabetizedWords(): List<Word>：获取所有单词并使其返回的List方法Words。
@Query("SELECT * from word_table ORDER BY word ASC")：查询返回按升序排列的单词列表。

### 6. LiveData类
数据更改时，通常需要采取一些措施，例如在UI中显示更新的数据。这意味着您必须观察数据，以便在数据更改时可以做出反应。

根据数据的存储方式，这可能很棘手。观察应用程序多个组件之间的数据更改可以在组件之间创建明确的，严格的依赖路径。除其他事项外，这使测试和调试变得困难。

LiveData，用于数据观察的生命周期库类可解决此问题。LiveData在方法说明中使用类型的返回值，并且Room将生成所有必需的代码以在更新LiveData数据库时进行更新。


在中WordDao，更改getAlphabetizedWords()方法签名，以便返回的List<Word>内容包含LiveData。
```
@Query("SELECT * from word_table ORDER BY word ASC")
   fun getAlphabetizedWords(): LiveData<List<Word>>
```

### 7. Add a Room database

创建数据库
```
// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = arrayOf(Word::class), version = 1, exportSchema = false)
public abstract class WordRoomDatabase : RoomDatabase() {

   abstract fun wordDao(): WordDao

   companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time. 
        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        fun getDatabase(context: Context): WordRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        WordRoomDatabase::class.java, 
                        "word_database"
                    ).build()
                INSTANCE = instance
                return instance
            }
        }
   }
}
```

Room的数据库类必须为abstract并且扩展RoomDatabase
您使用注释该类为Room数据库，@Database并使用注释参数声明该数据库中的实体并设置版本号。每个实体对应一个将在数据库中创建的表。数据库迁移不在此代码实验室的范围内，因此exportSchema在此处设置为false以避免生成警告。在实际的应用程序中，您应考虑为Room设置目录以用于导出架构，以便可以将当前架构签入版本控制系统。
您通过为每个@Dao创建一个抽象的“ getter”方法来使数据库提供其DAO。
我们定义了singleton，WordRoomDatabase,以防止同时打开多个数据库实例。
getDatabase返回单例。它将在首次访问数据库时使用Room的数据库构建器RoomDatabase在类的应用程序上下文中创建一个对象WordRoomDatabase并将其命名，从而创建数据库"word_database"。

### 8.创建存储库
什么是存储库？
存储库类抽象了对多个数据源的访问。该存储库不是体系结构组件库的一部分，但是建议用于代码分离和体系结构的最佳实践。Repository类提供了一个干净的API，用于对应用程序其余部分的数据访问。

存储库管理查询，并允许您使用多个后端。在最常见的示例中，存储库实现了用于确定是从网络中获取数据还是使用本地数据库中缓存的结果的逻辑。

创建一个名为的Kotlin类文件WordRepository，并将以下代码粘贴到其中：

```
// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class WordRepository(private val wordDao: WordDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allWords: LiveData<List<Word>> = wordDao.getAlphabetizedWords()
 
    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }
}
```

DAO被传递到存储库构造函数，而不是整个数据库。这是因为它只需要访问DAO，因为DAO包含数据库的所有读/写方法。无需将整个数据库公开到存储库。
单词列表是公共财产。通过LiveData从Room 获取单词列表进行初始化；之所以可以这样做，是因为我们定义了在“ LiveData类”步骤中getAlphabetizedWords返回的方法LiveData。Room在单独的线程上执行所有查询。然后，LiveData当数据已更改时，观察者将在主线程上通知观察者。
该suspend修饰符告诉编译器，这需要从协同程序或其他暂停功能调用。

### 9.创建ViewModel
该ViewModel的作用是提供数据的UI和生存的配置更改。A ViewModel充当存储库和UI之间的通信中心。您还可以使用ViewModel在片段之间共享数据。ViewModel是生命周期库的一部分
The ViewModel's role is to provide data to the UI and survive configuration changes. A ViewModel acts as a communication center between the Repository and the UI. You can also use a ViewModel to share data between fragments. The ViewModel is part of the lifecycle library.

mplement the ViewModel
Create a Kotlin class file for WordViewModel and add this code to it:
```
// Class extends AndroidViewModel and requires application as a parameter.
class WordViewModel(application: Application) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    private val repository: WordRepository
    // LiveData gives us updated words when they change.
    val allWords: LiveData<List<Word>>

    init {
        // Gets reference to WordDao from WordRoomDatabase to construct
        // the correct WordRepository. 
        val wordsDao = WordRoomDatabase.getDatabase(application).wordDao()
        repository = WordRepository(wordsDao)
        allWords = repository.allWords
    }

    /**
     * The implementation of insert() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on 
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called 
     * viewModelScope which we can use here.
     */
    fun insert(word: Word) = viewModelScope.launch {
        repository.insert(word)
    }
}
```

创建了一个名为的类WordViewModel，该类获取Application作为参数并扩展AndroidViewModel。
添加了一个私有成员变量来保存对存储库的引用。
添加了一个公共LiveData成员变量来缓存单词列表。
创建的init该得到一个参考块WordDao从WordRoomDatabase.
在init块中，WordRepository基于构造WordRoomDatabase。
在该init块中，allWords使用存储库初始化LiveData。
创建了一个包装insert()方法，该方法调用存储库的insert()方法。这样，insert()UI 的实现就被封装了。我们不想insert阻塞主线程，所以我们要启动一个新的协程并调用存储库的insert，这是一个暂停函数。如前所述，ViewModel基于生命周期具有协程范围viewModelScope，我们在这里使用它。

### 10. Add XML layout
 
 Add a style for list items in values/styles.xml:
 ```
 <!-- The default font for RecyclerView items is too small.
The margin is a simple delimiter between the words. -->
<style name="word_title">
   <item name="android:layout_width">match_parent</item>
   <item name="android:layout_marginBottom">8dp</item>
   <item name="android:paddingLeft">8dp</item>
   <item name="android:background">@android:color/holo_orange_light</item>
   <item name="android:textAppearance">@android:style/TextAppearance.Large</item>
</style>
 ```
 Add a layout/recyclerview_item.xml layout:
 ```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <TextView
        android:id="@+id/textView"
        style="@style/word_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@android:color/holo_orange_light" />
</LinearLayout>
 ```
 In layout/activity_main.xml, replace the TextView with a RecyclerView and add a floating action button (FAB). Now your layout should look like this:
 ```
 <?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerview"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:background="@android:color/darker_gray"
        tools:listitem="@layout/recyclerview_item"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
 ```
 
 ### 11. Add a RecyclerView
 Create a Kotlin class file for WordListAdapter that extends RecyclerView.Adapter. Here is the code.
 ```
 class WordListAdapter internal constructor(
        context: Context
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var words = emptyList<Word>() // Cached copy of words

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordItemView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return WordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val current = words[position]
        holder.wordItemView.text = current.word
    }

    internal fun setWords(words: List<Word>) {
        this.words = words
        notifyDataSetChanged()
    }

    override fun getItemCount() = words.size
}
 ```
### 12.填充数据库
数据库中没有数据。您将通过两种方式添加数据：打开数据库时添加一些数据，并添加Activity用于添加单词的。

要在启动应用程序时删除所有内容并重新填充数据库，请创建RoomDatabase.Callback和覆盖onOpen()。由于您无法在UI线程上执行Room数据库操作，因此请onOpen()在IO Dispatcher上启动协程。

To launch a coroutine we need a CoroutineScope. Update the getDatabase method of the WordRoomDatabase class, to also get a coroutine scope as parameter:
要启动协程，我们需要一个CoroutineScope。更新类的getDatabase方法WordRoomDatabase，以获取协程范围作为参数：

```
fun getDatabase(
       context: Context,
       scope: CoroutineScope
  ): WordRoomDatabase {
...
}
```
在的init块中更新数据库检索初始化程序WordViewModel也要通过范围。
```
val wordsDao = WordRoomDatabase.getDatabase(application, viewModelScope).wordDao()
```
In the WordRoomDatabase, we create a custom implementation of the RoomDatabase.Callback(), that also gets a CoroutineScope as constructor parameter. Then, we override the onOpen method to populate the database.
```
private class WordDatabaseCallback(
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        INSTANCE?.let { database ->
            scope.launch {
                populateDatabase(database.wordDao())
            }
        }
    }

    suspend fun populateDatabase(wordDao: WordDao) {
        // Delete all content here.
        wordDao.deleteAll()

        // Add sample words.
        var word = Word("Hello")
        wordDao.insert(word)
        word = Word("World!")
        wordDao.insert(word)

        // TODO: Add your own words!
    }
}
```
Finally, add the callback to the database build sequence right before calling .build() on the Room.databaseBuilder().
```
.addCallback(WordDatabaseCallback(scope))
```

Here is what the final code should look like:

```
@Database(entities = arrayOf(Word::class), version = 1, exportSchema = false)
abstract class WordRoomDatabase : RoomDatabase() {

   abstract fun wordDao(): WordDao

   private class WordDatabaseCallback(
       private val scope: CoroutineScope
   ) : RoomDatabase.Callback() {

       override fun onOpen(db: SupportSQLiteDatabase) {
           super.onOpen(db)
           INSTANCE?.let { database ->
               scope.launch {
                   var wordDao = database.wordDao()

                   // Delete all content here.
                   wordDao.deleteAll()

                   // Add sample words.
                   var word = Word("Hello")
                   wordDao.insert(word)
                   word = Word("World!")
                   wordDao.insert(word)

                   // TODO: Add your own words!
                   word = Word("TODO!")
                   wordDao.insert(word)
               }
           }
       }
   }

   companion object {
       @Volatile
       private var INSTANCE: WordRoomDatabase? = null

       fun getDatabase(
           context: Context,
           scope: CoroutineScope
       ): WordRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        WordRoomDatabase::class.java,
                        "word_database"
                )
                 .addCallback(WordDatabaseCallback(scope))
                 .build()
                INSTANCE = instance
                // return instance
                instance
        }
   }
}

```

# 13. Add NewWordActivity
Add these string resources in values/strings.xml:
```
<string name="hint_word">Word...</string>
<string name="button_save">Save</string>
<string name="empty_not_saved">Word not saved because it is empty.</string>
```
Add this color resource in value/colors.xml:
```
<color name="buttonLabel">#d3d3d3</color>
```
Add these dimension resources in values/dimens.xml:

```
<dimen name="small_padding">6dp</dimen>
<dimen name="big_padding">16dp</dimen>
```

Create a new empty Android Activity with the Empty Activity template:
```
<?xml version="1.0" encoding="utf-8"?>

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
   android:orientation="vertical" android:layout_width="match_parent"
   android:layout_height="match_parent">

   <EditText
       android:id="@+id/edit_word"
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:fontFamily="sans-serif-light"
       android:hint="@string/hint_word"
       android:inputType="textAutoComplete"
       android:padding="@dimen/small_padding"
       android:layout_marginBottom="@dimen/big_padding"
       android:layout_marginTop="@dimen/big_padding"
       android:textSize="18sp" />

   <Button
       android:id="@+id/button_save"
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:background="@color/colorPrimary"
       android:text="@string/button_save"
       android:textColor="@color/buttonLabel" />

</LinearLayout>
```
```
class NewWordActivity : AppCompatActivity() {

    private lateinit var editWordView: EditText

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_word)
        editWordView = findViewById(R.id.edit_word)

        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editWordView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val word = editWordView.text.toString()
                replyIntent.putExtra(EXTRA_REPLY, word)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
    }
}
```
### 14. Connect with the data
In MainActivity, create a member variable for the ViewModel:
```
private lateinit var wordViewModel: WordViewModel
```
In onCreate() below the RecyclerView code block, get a ViewModel from the ViewModelProvider.

```
wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)

```

The onChanged() method (the default method for our Lambda) fires when the observed data changes and the activity is in the foreground.
```
wordViewModel.allWords.observe(this, Observer { words ->
            // Update the cached copy of the words in the adapter.
            words?.let { adapter.setWords(it) }
})
```
We want to open the NewWordActivity when tapping on the FAB and, once we are back in the MainActivity, to either insert the new word in the database or show a Toast. To achieve this, let's start by defining a request code:
```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
        data?.getStringExtra(NewWordActivity.EXTRA_REPLY)?.let {
            val word = Word(it)
            wordViewModel.insert(word)
        }
    } else {
        Toast.makeText(
            applicationContext,
            R.string.empty_not_saved,
            Toast.LENGTH_LONG).show()
    }
}
```
In MainActivity,start NewWordActivity when the user taps the FAB. In the MainActivity onCreate, find the FAB and add an onClickListener with this code:
```
val fab = findViewById<FloatingActionButton>(R.id.fab)
fab.setOnClickListener {
  val intent = Intent(this@MainActivity, NewWordActivity::class.java)
  startActivityForResult(intent, newWordActivityRequestCode)
}
```
Your finished code should look like this:

```
class MainActivity : AppCompatActivity() {

   private const val newWordActivityRequestCode = 1
   private lateinit var wordViewModel: WordViewModel

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_main)
       setSupportActionBar(toolbar)

       val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
       val adapter = WordListAdapter(this)
       recyclerView.adapter = adapter
       recyclerView.layoutManager = LinearLayoutManager(this)

       wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)
       wordViewModel.allWords.observe(this, Observer { words ->
           // Update the cached copy of the words in the adapter.
           words?.let { adapter.setWords(it) }
       })

       val fab = findViewById<FloatingActionButton>(R.id.fab)
       fab.setOnClickListener {
           val intent = Intent(this@MainActivity, NewWordActivity::class.java)
           startActivityForResult(intent, newWordActivityRequestCode)
       }
   }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       super.onActivityResult(requestCode, resultCode, data)

       if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
           data?.getStringExtra(NewWordActivity.EXTRA_REPLY)?.let {
               val word = Word(it)
               wordViewModel.insert(word)
           }
       } else {
           Toast.makeText(
               applicationContext,
               R.string.empty_not_saved,
               Toast.LENGTH_LONG).show()
       }
   }
}
```






