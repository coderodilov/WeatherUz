package uz.coderodilov.weatheruz.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import uz.coderodilov.weatheruz.databinding.WeatherItemHourlyBinding
import uz.coderodilov.weatheruz.model.HourlyWeather

/* 
* Created by Coder Odilov on {27/05/2023}
*/

class RvHourlyWeatherAdapter(private val list:ArrayList<HourlyWeather>):RecyclerView.Adapter<RvHourlyWeatherAdapter.ViewHolder>(){
    inner class ViewHolder(private val binding:WeatherItemHourlyBinding):RecyclerView.ViewHolder(binding.root){
        fun onBind(hourLyWeather: HourlyWeather){
            binding.tvTime.text = hourLyWeather.time
            binding.tvTemp.text = hourLyWeather.temp
            val url = "https://openweathermap.org/img/wn/${hourLyWeather.iconUrl}@2x.png"
            Picasso.get().load(url).into(binding.imageIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvHourlyWeatherAdapter.ViewHolder {
        val view = WeatherItemHourlyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
       return list.size
    }
}