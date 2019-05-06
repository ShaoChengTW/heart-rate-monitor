package demo.idv.shao.heartratemonitor

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.room.Room
import demo.idv.shao.heartratemonitor.data.AppDatabase
import demo.idv.shao.heartratemonitor.data.ExerciseRecord
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.screen_heart_rate.*
import java.util.*
import java.util.concurrent.TimeUnit

class HeartRateFragment: Fragment() {

    companion object {
        const val HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
        val UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT)
    }

    private val disposible = CompositeDisposable()

    private var startTime: Long? = null
    private val heartRates: MutableList<Int> = mutableListOf()
    private var disposable: Disposable? = null

    private val heartRateSubject = BehaviorSubject.createDefault(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.screen_heart_rate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (toolbarInclude as Toolbar).title = "Home"

        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().finish()
            return@addCallback true
        }

        exerciseButton.setOnClickListener {
            if (startTime == null) {
                startTime = Calendar.getInstance().timeInMillis

                heartRates.clear()
                disposable = Observable.interval(1, TimeUnit.SECONDS)
                    .map { heartRateSubject.value }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( {
                        heartRates.add(it ?: 0)

                        val millis = Calendar.getInstance().timeInMillis - (startTime ?: 0)
                        val time = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                            TimeUnit.MILLISECONDS.toSeconds(millis) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))

                        timerTextView.text = time
                    }, { err ->
                        throw IllegalStateException(err)
                    })
                exerciseButton.setText(R.string.stop_exercise)
            } else {
                disposable?.dispose()

                exerciseButton.setText(R.string.start_exercise)

                showAlert()
            }
        }
    }

    private fun showAlert() {
        val view = layoutInflater.inflate(R.layout.alert_exercise_info, null)

        val noteEditText = view.findViewById<EditText>(R.id.noteEditText)
        val typeSpinner = view.findViewById<Spinner>(R.id.typeSpinner)

        val context = context ?: throw IllegalStateException()

        val type = arrayOf("general", "resting", "running")
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, type)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter

        val alert = AlertDialog.Builder(context)
            .setTitle("Exercise Info")
            .setCancelable(false)
            .setIcon(R.drawable.ic_action_heart)
            .setView(view)
            .setPositiveButton("OK") { dialog, _ ->

                val mainActivity = (activity as? MainActivity) ?: throw IllegalStateException()

                val db = mainActivity.db ?: throw IllegalStateException()

                val time = Calendar.getInstance().timeInMillis - (startTime ?: 0)
                val note = noteEditText.text.toString()
                val type = typeSpinner.selectedItem as String

                Completable.fromAction {
                    db.exerciseRecordDao().insertAll(ExerciseRecord(time, startTime, type, note, heartRates))
                }
                    .subscribeOn(Schedulers.io())
                    .subscribe()

                dialog.dismiss()
            }
            .create()

        alert.show()
    }

    override fun onStart() {
        super.onStart()

        val connectionObservable = (activity as? MainActivity)?.connectionObservable ?: throw IllegalStateException()
        disposible.add(
            connectionObservable
                .flatMap { conn -> conn.setupNotification(UUID_HEART_RATE_MEASUREMENT) }
                .flatMap { observable -> observable }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( { value ->
                    fun unsignedByteToInt(byte: Byte): Int = byte.toInt() and 0xFF
                    val heartRate = unsignedByteToInt(value[1])
                    heartRateTextView.text = "$heartRate"

                    heartRateSubject.onNext(heartRate)
                }, { err ->
                        throw IllegalStateException(err)
                })
        )
    }

    override fun onStop() {
        super.onStop()
        disposible.dispose()
    }
}

