package com.solt.thiochat

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.StrictMode
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        installSplashScreen()
//        StrictMode.setThreadPolicy(
//            StrictMode.ThreadPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .build()
//        )
//        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
       binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        //Bind the navBar to the Nav Controller
        binding.bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{controller,destination,bundle,->
            when(destination.id){
                R.id.authenticationFragment , R.id.groupMessagesPage->{
                    binding.bottomNavigationView.visibility = View.GONE
                }

                else-> binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }


    }
    fun showMessageSuccess(message:String){
        TransitionManager.beginDelayedTransition(binding.main, Slide())
        binding.notificationTextview.apply {
            text = message
            setBackgroundColor(resources.getColor(R.color.background_success,theme))
            setTextColor(resources.getColor(R.color.success_green,theme))
            visibility = View.VISIBLE
        }
        lifecycleScope.launch {
            startTimer(2500)
        }
    }
    fun showMessageFailure(message: String){
        TransitionManager.beginDelayedTransition(binding.main, Slide())
        binding.notificationTextview.apply {
            text = message
            setBackgroundColor(resources.getColor(R.color.background_error,theme))
            setTextColor(resources.getColor(R.color.error_red,theme))
            visibility = View.VISIBLE
        }
        lifecycleScope.launch {
            startTimer(2500)
        }
    }

    fun startTimer(time:Long){
           val timer = object : CountDownTimer(time,10){
               override fun onTick(millisUntilFinished: Long) {

               }

               override fun onFinish() {
                   TransitionManager.beginDelayedTransition(binding.main,Fade())
                   binding.notificationTextview.visibility = View.GONE

           }
       }
           timer.start()

   }


}