package uz.coderodilov.weatheruz


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
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
import uz.coderodilov.weatheruz.model.HourlyWeather

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val api_key = "e0203aac4b814861a7e8f2a2fc1f204f"

    private lateinit var cityAdapter: RvCityAdapter
    private lateinit var hourlyWeatherAdapter:RvHourlyWeatherAdapter

    private var cityName = "Andijan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getWeather(cityName)

        binding.btnChangeLocation.setOnClickListener{
            showMapBottomSheet()
        }
    }


    private fun getWeather(city: String) {
        getHourlyWeather(cityName)
        val url = "https://api.openweathermap.org/data/2.5/weather?q=${city}&appid=$api_key"
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
        val url = "https://api.openweathermap.org/data/2.5/forecast?q=${city}&appid=$api_key"
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
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun convertTempFromFtoC(fahrenheit:Double):Int{
        val result = fahrenheit - 273.15
        return result.roundToInt()
    }

}