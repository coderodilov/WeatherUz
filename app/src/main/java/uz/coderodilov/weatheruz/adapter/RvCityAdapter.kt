package uz.coderodilov.weatheruz.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.coderodilov.weatheruz.databinding.CityItemBinding

/* 
* Created by Coder Odilov on {26/05/2023}
*/

class RvCityAdapter(private val list: List<String>):RecyclerView.Adapter<RvCityAdapter.ViewHolder>(){

    private lateinit var onCityClicked:OnCityClicked

    inner class ViewHolder(private val binding: CityItemBinding):RecyclerView.ViewHolder(binding.root){
        fun onBind(city:String){
            binding.tvCityName.text = city

            binding.root.setOnClickListener{
                onCityClicked.setOnclickListener(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    fun onCityClickListener(listener:OnCityClicked){
        onCityClicked = listener
    }

    fun interface OnCityClicked{
        fun setOnclickListener(position: Int)
    }
}