package uz.coderodilov.weatheruz.ui.weather

import uz.coderodilov.weatheruz.model.Day

/* 
* Created by Coder Odilov on 26/05/2023
*/

interface WeatherContract {
    interface Presenter{
       fun showDailyWeather()
    }

    interface View{
        fun setWeatherCurrent()
        fun setWeatherDaysOfWeek()

    }

    interface Repository{
        fun getWeatherCurrent(cityName:String)
        fun getWeatherDaysOfWeek(cityName:String):ArrayList<Day>
    }
}