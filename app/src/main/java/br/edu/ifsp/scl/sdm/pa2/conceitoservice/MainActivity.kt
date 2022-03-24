package br.edu.ifsp.scl.sdm.pa2.conceitoservice

import android.app.Service
import android.content.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.sdm.pa2.conceitoservice.LifetimeStartedService.Companion.EXTRA_LIFETIME
import br.edu.ifsp.scl.sdm.pa2.conceitoservice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val lifetimeServiceIntent: Intent by lazy {
        //Intent(this, LifetimeStartedService::class.java)
        Intent(this, LifetimeBoundService::class.java)
    }
    private lateinit var lifetimeBoundService: LifetimeBoundService
    private var connected = false
    private val serviceConnection: ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            lifetimeBoundService =
                (binder as LifetimeBoundService.LifetimeBoundServiceBinder).getService()
            connected = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            connected = false
        }
    }

    private inner class LifetimeServiceHandler(lifetimeServiceLooper: Looper) : Handler(lifetimeServiceLooper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (connected) {
                runOnUiThread {
                    activityMainBinding.serviceLifetimeTv.text =
                        lifetimeBoundService.lifetime.toString()
                }
                obtainMessage().also {
                    sendMessageDelayed(it, 1000)
                }
            }
        }
    }
    private lateinit var lifetimeServiceHandler: LifetimeServiceHandler

    /* BroadcastReceiver que recebe o lifetime do serviço */
/*    private val receiveLifetimeBr: BroadcastReceiver by lazy {
        object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                intent?.getIntExtra(EXTRA_LIFETIME, 0).also{ lifetime ->
                    activityMainBinding.serviceLifetimeTv.text = lifetime.toString()
                }
            }
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        HandlerThread("LifetimeHandlerThread").apply {
            start()
            lifetimeServiceHandler = LifetimeServiceHandler(looper)
        }

        with(activityMainBinding) {
            iniciarServicoBt.setOnClickListener {
                bindService(lifetimeServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                //startService(lifetimeServiceIntent)
                lifetimeServiceHandler.obtainMessage().also {
                    lifetimeServiceHandler.sendMessageDelayed(it, 1000)
                }
            }
            finalizarServicoBt.setOnClickListener {
                unbindService(serviceConnection)
                connected = false
                //stopService(lifetimeServiceIntent)
            }
        }
    }

 /*
    override fun onStart() {
        super.onStart()
        registerReceiver(receiveLifetimeBr,
            IntentFilter("ACTION_RECEIVE_LIFETIME"))
    }

    override fun onStop() {
        super.onStop()
        //unregisterReceiver(receiveLifetimeBr)
    }
    */
}