package coffeecode.co.learnfirebasedispatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.jobdispatcher.*
import kotlinx.android.synthetic.main.activity_main.*
import android.R.string.cancel




class MainActivity : AppCompatActivity() {

    companion object{
        private val DISPATCHER_TAG = "mydispatcher"
        private const val CITY = "Jakarta"
    }

    private lateinit var mDispatcher: FirebaseJobDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        onClick()
    }

    private fun onClick() {
        btnSetScheduler.setOnClickListener {
            startDispatcher()
            Toast.makeText(this, "Dispatcher Created", Toast.LENGTH_SHORT).show();
        }

        btnCancelScheduler.setOnClickListener {
            cancelDispatcher()
            Toast.makeText(this, "Dispatcher Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private fun startDispatcher(){
        val myExtrasBundle = Bundle()
        myExtrasBundle.putString(MyJobService.EXTRAS_CITY, CITY)

        val myJob = mDispatcher.newJobBuilder()
                .setService(MyJobService::class.java)
                .setTag(DISPATCHER_TAG)
                .setRecurring(true)
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setTrigger(Trigger.executionWindow(0, 60))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK, Constraint.DEVICE_CHARGING)
                .setExtras(myExtrasBundle)
                .build()

        mDispatcher.mustSchedule(myJob)
    }

    private fun cancelDispatcher() {
        mDispatcher.cancel(DISPATCHER_TAG)
    }
}
