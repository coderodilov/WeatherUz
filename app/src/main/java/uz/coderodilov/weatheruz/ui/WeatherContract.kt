package uz.coderodilov.weatheruz.ui

/* 
* Created by Coder Odilov on {26/05/2023}
*/
interface WeatherContract {
    interface Presenter{

    }

    interface View{

    }

    interface Repository{
        fun getObjectFromJson()
    }
}