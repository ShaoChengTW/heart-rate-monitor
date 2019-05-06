package demo.idv.shao.heartratemonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.screen_home.*


class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.screen_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigationView.selectedItemId = R.id.action_home

        childFragmentManager.beginTransaction()
            .add(R.id.container, HeartRateFragment())
            .commit()

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            if (bottomNavigationView.selectedItemId == menuItem.itemId) {
                return@setOnNavigationItemSelectedListener true
            } else {
                when(menuItem.itemId) {
                    R.id.action_home -> {
                        childFragmentManager.beginTransaction()
                            .replace(R.id.container, HeartRateFragment())
                            .commit()
                    }
                    R.id.action_history -> {
                        childFragmentManager.beginTransaction()
                            .replace(R.id.container, HistoryFragment())
                            .commit()
                    }
                    R.id.action_profile -> {
                        childFragmentManager.beginTransaction()
                            .replace(R.id.container, PlaceholderFragment())
                            .commit()
                    }
                    else -> throw IllegalStateException()
                }
                return@setOnNavigationItemSelectedListener true
            }
        }
    }
}
