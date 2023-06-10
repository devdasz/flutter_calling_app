package com.example.flutter_calling_app

import android.content.BroadcastReceiver
import io.flutter.embedding.android.FlutterActivity

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.annotation.NonNull

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val BATTERY_CHANNEL = "batteryChannel.com"
    private lateinit var battery_channel: MethodChannel
    private val EVENT_CHANNEL =  "chargingChannel.com"
    private lateinit var charging_channel: EventChannel
    private val CALLING_CHANNEL =  "callingChannel.com"
    private lateinit var calling_channel: EventChannel

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        battery_channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BATTERY_CHANNEL)
        charging_channel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL)
        charging_channel.setStreamHandler(MyStreamHandler(context))
        calling_channel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, CALLING_CHANNEL)
        calling_channel.setStreamHandler(MyCallingStreamHandler(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            val batteryLevel= getBatteryLevel()
            battery_channel.invokeMethod("reportBatteryLevel", batteryLevel)
        },0)

    }
    private fun getBatteryLevel(): Int {
        val batteryLevel: Int
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryLevel = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

        return batteryLevel
    }

}
class MyStreamHandler(private  val context:Context) : EventChannel.StreamHandler{
    private var receiver: BroadcastReceiver? = null
    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
        print(events)
        if (events== null) return
        receiver = initReceiver(events)
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onCancel(arguments: Any?) {
    context.unregisterReceiver(receiver)
        receiver = null
    }

    private fun initReceiver(events:EventSink):BroadcastReceiver{
        return object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS,-1)
                events.success(status.toString())
//                when(status){
//                    BatteryManager.BATTERY_STATUS_CHARGING -> events.success("Battery is charging")
//                    BatteryManager.BATTERY_STATUS_FULL -> events.success("Battery is full")
//                    BatteryManager.BATTERY_STATUS_DISCHARGING -> events.success("Battery is discharging")
//
//                }
            }
        }
    }
}
class MyCallingStreamHandler(private  val context:Context) : EventChannel.StreamHandler{
    private var receiver: BroadcastReceiver? = null
    override fun onListen(arguments: Any?, events: EventChannel.EventSink) {

        if (events== null) return
        receiver = initReceiver(events)
        val filter = IntentFilter()
        filter.addAction("android.intent.action.PHONE_STATE")
        context.registerReceiver(receiver, filter)
    }

    override fun onCancel(arguments: Any?) {
        context.unregisterReceiver(receiver)
        receiver = null
    }

    private fun initReceiver(events:EventSink):BroadcastReceiver{
        return object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {

                val status = intent?.getIntExtra(TelephonyManager.EXTRA_STATE,-1)
                events.success(status)
//
            }
        }
    }
}