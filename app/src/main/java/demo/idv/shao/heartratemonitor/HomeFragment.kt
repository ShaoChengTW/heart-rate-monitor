package demo.idv.shao.heartratemonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.screen_home.*
import java.util.*

class HomeFragment: Fragment() {

    companion object {
        const val HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
        val UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT)
    }

    private val disposible = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.screen_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (toolbarInclude as Toolbar).title = "Home"

        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().finish()
            return@addCallback true
        }
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

