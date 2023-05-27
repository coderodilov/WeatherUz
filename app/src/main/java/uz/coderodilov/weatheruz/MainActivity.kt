package uz.coderodilov.weatheruz

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONException
import uz.coderodilov.weatheruz.adapter.RvCityAdapter
import uz.coderodilov.weatheruz.adapter.RvHourlyWeatherAdapter
import uz.coderodilov.weatheruz.data.Lists
import uz.coderodilov.weatheruz.databinding.ActivityMainBinding
import uz.coderodilov.weatheruz.databinding.CitiyBottomSheetDialogBinding
import uz.coderodilov.weatheruz.helper.ConnectivityReceiver
import uz.coderodilov.weatheruz.model.HourlyWeather
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(),SwipeRefreshLayout.OnRefreshListener,
    ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var binding: ActivityMainBinding
    private val apikey = "e0203aac4b814861a7e8f2a2fc1f204f"

    private lateinit var cityAdapter: RvCityAdapter
    private lateinit var hourlyWeatherAdapter:RvHourlyWeatherAdapter

    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var editor: Editor

    private lateinit var cityName:String
    private var isNetworkConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("appConfig", MODE_PRIVATE)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        cityName = getFromShared()

        getWeather(cityName)

        binding.btnChangeLocation.setOnClickListener{
            showMapBottomSheet()
        }

        registerReceiver(
            ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

    }


    @SuppressLint("SetTextI18n")
    private fun getWeather(city: String) {
        getHourlyWeather(cityName)
        val url = "https://api.openweathermap.org/data/2.5/weather?q=${city}&appid=$apikey"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
              try {
                  val main = response.getJSONObject("main")
                  val weather = response.getJSONArray("weather").getJSONObject(0)
                  val temp = main.getDouble("temp")
                  val desc = weather.getString("description")

                  val roundedTemp = convertTempFromFtoC(temp)
                  binding.tvCity.text = cityName
                  binding.tvTemp.text = "${roundedTemp}°"
                  binding.tvDesc.text = desc

              } catch (e:JSONException){
                  e.printStackTrace()
              }
            },
            {
                it.printStackTrace()
            })

        val queue:RequestQueue = Volley.newRequestQueue(this)
        queue.add(jsonObjectRequest)
    }

    private fun getHourlyWeather(city:String){
        binding.shimmerContainer.startShimmerAnimation()
        binding.shimmerContainer.visibility = View.VISIBLE
        binding.rvHourly.visibility = View.GONE

        val url = "https://api.openweathermap.org/data/2.5/forecast?q=${city}&appid=$apikey"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            {  response ->
                try {
                    val listOfHours = ArrayList<HourlyWeather>()
                    val list = response.getJSONArray("list")

                    for (i in 0 until list.length()){
                        if (list.getJSONObject(i).getString("dt_txt").split(" ")[0] == getCurrentDate()){
                            val hour = list.getJSONObject(i).getString("dt_txt").split(" ")[1].substring(0,5)
                            var temp = list.getJSONObject(i).getJSONObject("main").getString("temp")
                            temp = convertTempFromFtoC(temp.toDouble()).toString() + "°"

                            val icon = list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon")

                            val hourlyWeather = HourlyWeather(hour, temp, icon)
                            listOfHours.add(hourlyWeather)
                            Log.d("MyList", icon)
                        }
                    }

                    hourlyWeatherAdapter = RvHourlyWeatherAdapter(listOfHours)
                    binding.rvHourly.adapter = hourlyWeatherAdapter

                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.shimmerContainer.stopShimmerAnimation()
                    binding.shimmerContainer.visibility = View.GONE
                    binding.rvHourly.visibility = View.VISIBLE


                } catch (e:JSONException){
                    e.printStackTrace()
                }

            }, {
                it.printStackTrace()
            })

        val queue:RequestQueue = Volley.newRequestQueue(this)
        queue.add(jsonObjectRequest)
    }

    @SuppressLint("SimpleDateFormat")
    private  fun getCurrentDate():String{
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date())
    }


    private fun showMapBottomSheet(){
        val dialogBinding = CitiyBottomSheetDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogStyle)

        dialog.setContentView(dialogBinding.root)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        cityAdapter = RvCityAdapter(Lists.list)
        dialogBinding.rvCity.adapter = cityAdapter

        cityAdapter.onCityClickListener{
            cityName = Lists.list[it]
            getWeather(cityName)
            saveToShared(cityName)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun saveToShared(city: String){
        editor = sharedPreferences.edit()
        editor.putString("cityName", city).apply()
    }

    private fun getFromShared():String{
        return  sharedPreferences.getString("cityName", "Andijan")!!
    }


    private fun convertTempFromFtoC(fahrenheit:Double):Int{
        val result = fahrenheit - 273.15
        return result.roundToInt()
    }


    override fun onRefresh() {
        getWeather(cityName)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        getWeather(cityName)
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        getWeather(cityName)
        isNetworkConnected = isConnected
    }


}