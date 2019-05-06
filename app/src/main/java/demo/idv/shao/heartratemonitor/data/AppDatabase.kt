package demo.idv.shao.heartratemonitor.data

import androidx.room.*
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Observable


@Database(entities = [ExerciseRecord::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseRecordDao(): ExerciseRecordDao
}

@Dao
interface ExerciseRecordDao {
    @Query("SELECT * FROM exerciserecord")
    fun getAll(): List<ExerciseRecord>

    @Insert
    fun insertAll(vararg records: ExerciseRecord)

    @Delete
    fun delete(record: ExerciseRecord)

    @Query("SELECT * FROM exerciserecord")
    fun getAllObservable(): Observable<List<ExerciseRecord>>
}

@Entity
data class ExerciseRecord(
    @ColumnInfo(name = "time") val time: Long?,
    @ColumnInfo(name = "start_time") val startTime: Long?,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "heart_rates") val heartRates: List<Int>?
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}

class Converters {
    @TypeConverter
    fun listToJson(value: List<Int>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<Int>? {
        val objects = Gson().fromJson(value, Array<Int>::class.java) as Array<Int>
        return objects.toList()
    }
}