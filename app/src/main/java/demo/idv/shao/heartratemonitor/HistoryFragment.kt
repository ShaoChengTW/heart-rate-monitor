package demo.idv.shao.heartratemonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import demo.idv.shao.heartratemonitor.data.ExerciseRecord
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.screen_history.*
import java.text.SimpleDateFormat
import java.util.*


class HistoryFragment : Fragment() {

    private lateinit var viewAdapter: RecordAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.screen_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (toolbarInclude as Toolbar).title = "History"

        viewManager = LinearLayoutManager(context)
        viewAdapter = RecordAdapter() {
            // TODO
        }

        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }

    override fun onStart() {
        super.onStart()

        val mainActivity = (activity as? MainActivity) ?: throw IllegalStateException()

        val db = mainActivity.db ?: throw IllegalStateException()

        disposable.add(
            db.exerciseRecordDao().getAllObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ records ->
                    viewAdapter.records = records.toMutableList()
                }, { err ->
                    throw IllegalStateException(err)
                })
        )
    }

    override fun onStop() {
        super.onStop()

        disposable.dispose()
    }
}

class RecordAdapter(private val connectTo: (ExerciseRecord) -> Unit) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    var records: MutableList<ExerciseRecord> = mutableListOf()
        set(value) {
            records.clear()
            records.addAll(value)
            notifyDataSetChanged()
        }

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTextView = view.findViewById<TextView>(R.id.noteTextView)
        val avgHeartRateView = view.findViewById<TextView>(R.id.avgHeartRateView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val typeImage = view.findViewById<ImageView>(R.id.typeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.noteTextView.text = record.note
        holder.avgHeartRateView.text = record.heartRates?.average()?.toInt().toString()
        val imageRes= when(record.type) {
            "resting" -> R.drawable.ic_nature_people_black_24dp
            "walking" -> R.drawable.ic_walking_black_24dp
            "running" -> R.drawable.ic_running_black_24dp
            else -> throw IllegalStateException()
        }
        holder.typeImage.setImageResource(imageRes)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        holder.dateTextView.text = dateFormat.format(Date(record.startTime ?: 0))
        holder.itemView.setOnClickListener {
            connectTo(records[position])
        }
    }

    override fun getItemCount() = records.size
}