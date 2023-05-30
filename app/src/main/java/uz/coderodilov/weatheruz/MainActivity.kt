package uz.coderodilov.weatheruz

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONException
import uz.coderodilov.weatheruz.adapter.RvCityAdapter
import uz.coderodilov.weatheruz.adapter.RvDailyWeatherAdapter
import uz.coderodilov.weatheruz.adapter.RvHourlyWeatherAdapter
import uz.coderodilov.weatheruz.data.Lists
import uz.coderodilov.weatheruz.databinding.ActivityMainBinding
import uz.coderodilov.weatheruz.databinding.CitiyBottomSheetDialogBinding
import uz.coderodilov.weatheruz.databinding.DailyBottomSheetDialogBinding
import uz.coderodilov.weatheruz.databinding.InfoBottomSheetDialogBinding
import uz.coderodilov.weatheruz.helper.ConnectivityReceiver
import uz.coderodilov.weatheruz.model.Day
import uz.coderodilov.weatheruz.model.HourlyWeather
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

@SuppressLint("SimpleDateFormat")
class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var cityAdapter: RvCityAdapter
    private lateinit var hourlyWeatherAdapter: RvHourlyWeatherAdapter
    private lateinit var rvDailyWeatherAdapter: RvDailyWeatherAdapter

    private val listDailyWeather = ArrayList<Day>()
    private val listHourly = ArrayList<HourlyWeather>()
    private val listTimeForInfoDialog = ArrayList<String>()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor

    private lateinit var cityName: String
    private var isNetworkConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("appConfig", MODE_PRIVATE)
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        cityName = getFromShared()
        getCurrentWeather(cityName)

        binding.btnChangeLocation.setOnClickListener {
            showRegionBottomSheet()
        }

        binding.btnDailyWeatherList.setOnClickListener {
            showDailyWeatherBottomSheet()
        }

        binding.btnInfo.setOnClickListener {
            showInfoBottomSheet()
        }

        registerReceiver(
            ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

    }

    //region API Volley
    private fun getCurrentWeather(city: String) {
        binding.shimmerContainer.visibility = View.VISIBLE
        binding.shimmerContainer.startShimmerAnimation()
        binding.rvHourly.visibility = View.GONE

        binding.tvCity.text = ""
        binding.tvTemp.text = ""
        binding.tvDesc.text = ""

        getWeeklyWeather(city)
        val key = "fbd46ace8692435995f111103232705"
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=${key}&q=${city}&days=1&aqi=no&alerts=no"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    listHourly.clear()
                    listTimeForInfoDialog.clear()

                    val current = response.getJSONObject("current")
                    val currentTemp = current.getDouble("temp_c").roundToInt().toString().plus("°")
                    val currentDesc = current.getJSONObject("condition").getString("text")

                    binding.tvTemp.text = currentTemp
                    binding.tvCity.text = cityName
                    binding.tvDesc.text = currentDesc

                    val forecast = response.getJSONObject("forecast")
                    val day = forecast.getJSONArray("forecastday")
                    val objectOfDay = day.getJSONObject(0)
                    val listHoursOfDay = objectOfDay.getJSONArray("hour")

                    listTimeForInfoDialog.add(day.getJSONObject(0).getJSONObject("astro").getString("sunrise"))
                    listTimeForInfoDialog.add(day.getJSONObject(0).getJSONObject("astro").getString("sunset"))
                    listTimeForInfoDialog.add(day.getJSONObject(0).getJSONObject("astro").getString("moonrise"))
                    listTimeForInfoDialog.add(day.getJSONObject(0).getJSONObject("astro").getString("moonset"))

                    for (i in 0 until listHoursOfDay.length()) {
                        if (listHoursOfDay.getJSONObject(i).getString("time")
                                .split(" ")[0] == getCurrentDate()
                        ) {
                            val hour = listHoursOfDay.getJSONObject(i).getString("time")
                                .split(" ")[1].substring(0, 5)
                            val temp = listHoursOfDay.getJSONObject(i).getDouble("temp_c").roundToInt().toString().plus("°")

                            val icon = listHoursOfDay.getJSONObject(i).getJSONObject("condition")
                                .getString("icon")
                            val hourlyWeather = HourlyWeather(hour, temp, icon)
                            listHourly.add(hourlyWeather)
                        }
                    }

                    binding.shimmerContainer.stopShimmerAnimation()
                    binding.rvHourly.visibility = View.VISIBLE
                    binding.shimmerContainer.visibility = View.GONE

                    setupRvWeather(listHourly)
                    binding.swipeRefreshLayout.isRefreshing = false

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            },

            {
                it.printStackTrace()
            })

        val queue: RequestQueue = Volley.newRequestQueue(this)
        queue.add(jsonObjectRequest)

    }

    private fun getWeeklyWeather(city: String) {
        val key = "fbd46ace8692435995f111103232705"
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=${key}&q=${city}&days=7&aqi=no&alerts=no"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    listDailyWeather.clear()
                    val forecast = response.getJSONObject("forecast")
                    val location = response.getJSONObject("location")
                    var region = location.getString("region")

                    region += ", ${location.getString("country")}"
                    val forecastday = forecast.getJSONArray("forecastday")

                    for (i in 0 until forecastday.length()) {
                        val day = forecastday.getJSONObject(i).getJSONObject("day")
                        val temp = day.getDouble("maxtemp_c").roundToInt().toString()
                        val desc = day.getJSONObject("condition").getString("text")
                        val icon = day.getJSONObject("condition").getString("icon")
                        val date = forecastday.getJSONObject(i).getString("date")

                        listDailyWeather.add(Day(temp, desc, icon, region, date))
                    }

                    Log.d("MyList", listDailyWeather[1].maxtemp_c)


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            {
                it.printStackTrace()
            })

        val queue: RequestQueue = Volley.newRequestQueue(this)
        queue.add(jsonObjectRequest)

    }

    //endregion

    //region Utils
    private fun setupRvWeather(list: ArrayList<HourlyWeather>) {
        binding.shimmerContainer.startShimmerAnimation()
        binding.shimmerContainer.visibility = View.GONE
        binding.rvHourly.visibility = View.VISIBLE


        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        val offset = this.resources.displayMetrics.widthPixels
        val itemWidth = 80

        hourlyWeatherAdapter = RvHourlyWeatherAdapter(list)
        binding.rvHourly.layoutManager = layoutManager
        binding.rvHourly.adapter = hourlyWeatherAdapter

        layoutManager.scrollToPositionWithOffset(
            getCurrentHourPosition(list),
            offset / 2 - itemWidth
        )
    }

    private fun getCurrentHourPosition(list: ArrayList<HourlyWeather>): Int {
        var counter = 0
        while (counter < list.size) {
            if (list[counter].time.substring(0, 2) == getCurrentHour()) {
                break
            } else {
                counter++
            }
        }
        return counter
    }

    //endregion

    //region DateAndTime
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date())
    }

    private fun getCurrentHour(): String {
        val rightNow: Calendar = Calendar.getInstance()
        val currentHour: Int = rightNow.get(Calendar.HOUR_OF_DAY)
        var tempHour = ""
        if (currentHour.toString().length == 1) {
            tempHour += "0$currentHour"
            return tempHour
        }
        return currentHour.toString().substring(0, 2)
    }
    //endregion

    //region Bottomsheet
    private fun showRegionBottomSheet() {
        val dialogBinding = CitiyBottomSheetDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogStyle)

        dialog.setContentView(dialogBinding.root)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        cityAdapter = RvCityAdapter(Lists.list)
        dialogBinding.rvCity.adapter = cityAdapter

        cityAdapter.onCityClickListener {
            cityName = Lists.list[it]
            getCurrentWeather(cityName)
            saveToShared(cityName)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDailyWeatherBottomSheet() {
        val dialogBinding = DailyBottomSheetDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogStyle)

        dialog.setContentView(dialogBinding.root)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        rvDailyWeatherAdapter = RvDailyWeatherAdapter(listDailyWeather)
        dialogBinding.rvDailyWeather.adapter = rvDailyWeatherAdapter

        dialog.show()
    }

    private fun showInfoBottomSheet() {
        val dialogBinding = InfoBottomSheetDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogStyle)
        dialog.setContentView(dialogBinding.root)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        dialogBinding.apply {
            if (listTimeForInfoDialog.isNotEmpty()) {
                tvSunRiseTime.text = listTimeForInfoDialog[0]
                tvSunSetTime.text = listTimeForInfoDialog[1]

                tvMoonriseTime.text = listTimeForInfoDialog[2]
                tvMoonsetTime.text = listTimeForInfoDialog[3]
            }
        }

        dialog.show()
    }

    //endregion

    //region SharedPref
    private fun saveToShared(city: String) {
        editor = sharedPreferences.edit()
        editor.putString("cityName", city).apply()
    }

    private fun getFromShared(): String {
        return sharedPreferences.getString("cityName", "Andijan")!!
    }

    //endregion

    //region Override

    override fun onRefresh() {
        if (isNetworkConnected){
            getCurrentWeather(cityName)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    @SuppressLint("SetTextI18n")
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            getCurrentWeather(cityName)
        } else {
            listDailyWeather.clear()
            listHourly.clear()

            binding.tvCity.text = "offline"
            binding.tvTemp.text = ""
            binding.tvDesc.text = ""

            binding.shimmerContainer.visibility = View.VISIBLE
            binding.shimmerContainer.startShimmerAnimation()
            binding.rvHourly.visibility = View.GONE
        }

        isNetworkConnected = isConnected
    }

    //endregion

}