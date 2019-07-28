package coffeecode.co.learnfirebasedispatcher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.text.DecimalFormat


class MyJobService : JobService() {

    companion object{
        val TAG = MyJobService::class.java.simpleName
        const val APP_ID = "a6d3678de683ec9de8944b08ff50e349"
        const val EXTRAS_CITY = "extras_city"

        const val CHANNEL_ID = "Channel_1"
        const val CHANNEL_NAME = "Job service channel"
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(job: JobParameters?): Boolean {
        job?.let { getCurrentWeather(it) }
        return true
    }

    private fun getCurrentWeather(job: JobParameters){
        val extras = job.extras

        if (extras == null){
            jobFinished(job,false)
        }else if (extras.isEmpty){
            jobFinished(job, false)
            return
        }

        val city = extras?.getString(EXTRAS_CITY)

        val client = AsyncHttpClient()
        val url = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=$APP_ID"
        client.get(url, object : AsyncHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                val result = responseBody?.let { String(it) }
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)
                    val currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description")
                    val tempInKelvin = responseObject.getJSONObject("main").getDouble("temp")

                    val tempInCelsius = tempInKelvin - 273
                    val temperature = DecimalFormat("##.##").format(tempInCelsius)

                    val title = "Current Weather"
                    val message = "$currentWeather, $description with $temperature celcius"
                    val notifyId = 100

                    showNotification(applicationContext, title, message, notifyId)

                    jobFinished(job, false)
                } catch (e: Exception) {
                    jobFinished(job, true)
                    e.printStackTrace()
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                jobFinished(job, true)
            }

        })
    }

    private fun showNotification(context: Context?, title: String, message: String, notifyId: Int) {
        val notificationManagerCompat:NotificationManager? = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_replay_30_black_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.black))
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

            builder.setChannelId(CHANNEL_ID)

            notificationManagerCompat?.createNotificationChannel(channel)
        }

        val notification = builder.build()

        notificationManagerCompat?.notify(notifyId, notification)
    }
}