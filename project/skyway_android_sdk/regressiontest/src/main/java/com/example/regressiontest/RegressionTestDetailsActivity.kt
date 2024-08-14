package com.example.regressiontest


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.regressiontest.adapter.RecyclerViewAdapterSubscription
import com.example.regressiontest.databinding.ActivityRegressionTestDetailsBinding
import com.example.regressiontest.manager.RoomManager
import com.example.regressiontest.manager.SessionManager
import com.example.regressiontest.util.Util
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RegressionTestDetailsActivity : AppCompatActivity() {
    val TAG = this.javaClass.simpleName

    private var controller = RoomManager()
    private var recyclerViewAdapterSubscription: RecyclerViewAdapterSubscription? = null

    private lateinit var binding: ActivityRegressionTestDetailsBinding
    private lateinit var session: SessionManager

    private val listener = SessionManager.Listener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegressionTestDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runBlocking {
            controller.room = P2PRoom.find(intent.getStringExtra("RoomName")!!)
            if (controller.room == null) {
                Util.showToast(applicationContext, "Room.find Failed")
                return@runBlocking
            }

            val memberName = intent.getStringExtra("MemberName")!!
            val metadata = Util.getClientMetadata()
            val memberInit = RoomMember.Init(memberName, metadata)
            controller.localPerson = controller.room!!.join(memberInit)

            if (controller.localPerson == null) {
                Util.showToast(applicationContext, "Room.join Failed")
                return@runBlocking
            }

            initUI()
            initButtons()

            session = SessionManager(listener, controller)
            session.subscribeToControllerDataStream()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        binding.roomName.text = controller.room?.name
        binding.memberName.text = controller.localPerson?.name
        binding.tvClientName.text = "Client: " + Util.getClientName()

        recyclerViewAdapterSubscription = RecyclerViewAdapterSubscription()
        binding.rvSubscriptionList.layoutManager = LinearLayoutManager(this)
        binding.rvSubscriptionList.adapter = recyclerViewAdapterSubscription

        listener.onInitSuccessHandler = {
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.tvReady.visibility = View.VISIBLE
            }
        }

        listener.onInitFailedHandler = {
            runOnUiThread {
                Util.showToast(applicationContext, it)
                binding.progressBar.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
            }
        }

        listener.onSubscribeHandler = {
            runOnUiThread {
                recyclerViewAdapterSubscription?.addData(it)
            }
        }

        listener.onNewTaskHandler = {
            runOnUiThread {
                binding.tvTaskName.text = "Task: $it"
            }
        }

        listener.onCloseTaskHandler = {
            runOnUiThread {
                recyclerViewAdapterSubscription?.clearData()
            }
        }
    }

    private fun initButtons() {
        binding.btnLeaveRoom.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                controller.localPerson?.leave()
                onBackPressed()
            }
        }
    }
}
