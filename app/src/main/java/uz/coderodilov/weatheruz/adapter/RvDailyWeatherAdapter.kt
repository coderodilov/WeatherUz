package uz.coderodilov.weatheruz.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import uz.coderodilov.weatheruz.databinding.DailyWeatherItemBinding
import uz.coderodilov.weatheruz.model.Day

/* 
* Created by Coder Odilov on 27/05/2023
*/

class RvDailyWeatherAdapter(private val list:ArrayList<Day>):RecyclerView.Adapter<RvDailyWeatherAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: DailyWeatherItemBinding):RecyclerView.ViewHolder(binding.root){
        fun onBInd(day: Day){
            binding.tvTemp.text = day.maxtemp_c
            binding.tvDesc.text = day.text
            binding.tvRegion.text = day.region
            binding.tvDate.text = day.date

            val url = "https:${day.icon}"
            Picasso.get().load(url).into(binding.imageIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvDailyWeatherAdapter.ViewHolder {
        val view = DailyWeatherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBInd(list[position])
    }

    override fun getItemCount(): Int {
       return list.size
    }

}