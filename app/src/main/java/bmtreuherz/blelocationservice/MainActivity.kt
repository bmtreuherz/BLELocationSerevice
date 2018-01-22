package bmtreuherz.blelocationservice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import bmtreuherz.blelocationservice.GattStuff.Client.ScanActivity
import bmtreuherz.blelocationservice.GattStuff.Server.GattServerActivity

class MainActivity : Activity() {

    lateinit private var emitButton: Button
    lateinit private var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emitButton = findViewById(R.id.emitButton) as Button
        scanButton = findViewById(R.id.scanButton) as Button

        emitButton.setOnClickListener {
            var intent = Intent(this, GattServerActivity::class.java)
            startActivity(intent)
        }

        scanButton.setOnClickListener {
//            var intent = Intent(this, ListenerActivity::class.java)
            var intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)

        }
    }
}
