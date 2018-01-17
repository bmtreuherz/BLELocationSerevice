package bmtreuherz.blelocationservice

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit private var emitButton: Button
    lateinit private var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emitButton = findViewById(R.id.emitButton)
        scanButton = findViewById(R.id.scanButton)

        emitButton.setOnClickListener {
            var intent = Intent(this, EmitterActivity::class.java)
            startActivity(intent)
        }

        scanButton.setOnClickListener {
//            var intent = Intent(this, ListenerActivity::class.java)
            Log.d("MAIN ACTIVITY", "GOING TO LISTENER!")
            var intent = Intent(this, JavaListenerActivity::class.java)
            startActivity(intent)

        }
    }
}
