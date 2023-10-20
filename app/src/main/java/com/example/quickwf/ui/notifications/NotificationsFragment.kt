package com.example.quickwf.ui.notifications

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.app.Fragment
import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.example.quickwf.MainActivity
import com.example.quickwf.data.DeliveryList
import com.example.quickwf.R
import com.example.quickwf.data.DBDeliveryList
import com.example.quickwf.data.DBDeliveryStart
import com.example.quickwf.data.DBRiderList
import com.example.quickwf.databinding.FragmentNotificationsBinding
import com.example.quickwf.network.WoojeonApiService
import com.example.quickwf.network.networkMng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var arDeliveryList : ArrayList<DeliveryList> = ArrayList()
    private var deliveryListAdapter : DeliveryListAdapter? = null
    private var selList : ArrayList<String> = ArrayList()
    private var selResion = "WF"
    private var ccode = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val notificationsViewModel =
//            ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
//                NotificationsViewModel::class.java
//            )

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvDeliveryList.layoutManager = layoutManager
        deliveryListAdapter = DeliveryListAdapter(context!!, arDeliveryList)
        binding.rvDeliveryList.adapter = deliveryListAdapter

        binding.txtNmRider.text = MainActivity.nmRider +"("+ MainActivity.noRider +")"
        binding.txtHpRider.text = MainActivity.hpRider
//        val textView: TextView = binding.textNotifications
//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        binding.ibtnRefresh.setOnClickListener(({
            getDeliveryList()
        }))
        binding.txtResion.setOnClickListener(({
            if (selResion == "강남") {
                selResion = "WA"
                binding.txtResion.text = "수서"
                binding.txtResion.setTextColor(resources.getColor(R.color.out_blue, null))
            } else {
                selResion = "WF"
                binding.txtResion.text = "강남"
                binding.txtResion.setTextColor(resources.getColor(R.color.out_red, null))
            }
        }))

        binding.btnDeliveryList.setOnClickListener(({
            if (chkList("List")) {
                startDelivery()
            }
        }))
        binding.btnDeliveryInput.setOnClickListener(({
            if (chkList("Input")) {
                startDeliveryInput()
            }
        }))
        getDeliveryList()
        return root
    }

    private fun chkList(gbn:String) : Boolean {
        if (MainActivity.noRider.isEmpty()) {
            Toast.makeText(context,"라이더를 먼저 선택/등록 해주세요", Toast.LENGTH_LONG).show()
            return false
        }
        if (gbn=="List" && selList.size == 0) {
            Toast.makeText(context,"리스트에서 선택해 주세요", Toast.LENGTH_LONG).show()
            return false
        }

        ccode = if (binding.rb031.isChecked) "1000"
        else if (binding.rb032.isChecked) "3000"
        else if (binding.rb033.isChecked) "5000"
        else ""

        if (ccode == "") {
            Toast.makeText(context,"법인을 선택해 주세요", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun getDeliveryList() {
        arDeliveryList.clear()
        if (networkMng(context!!).checkNetworkState()) {
            val api = WoojeonApiService.create()
            api.selectQuickList(selResion).enqueue(object : Callback<DBDeliveryList> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<DBDeliveryList>, response: Response<DBDeliveryList>) {
                    response.body()?.results?.let {
                        arDeliveryList.addAll(it)
                        deliveryListAdapter!!.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<DBDeliveryList>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        } else {
//            runOnUiThread {
//                arDeliveryList.clear()
//                deliveryListAdapter!!.notifyDataSetChanged()
//                Toast.makeText(baseContext, "인터넷 연결문제발생", Toast.LENGTH_LONG).show()
//                binding.txtTitle.text = "인터넷 문제발생 공유기 재시작해주세요"
//            }
        }
    }

    // 배송출발시 데이타 넘기기
    private fun startDelivery() {
        var temp = ""
        for (item in selList)
            temp = "$temp$item:"
        temp = temp.substring(0, temp.length - 1)

        val noRider = MainActivity.noRider
        val hpRider = MainActivity.hpRider
        val riding = MainActivity.riding

        val api = WoojeonApiService.create()
        api.updateQuickStart(temp, noRider, hpRider, riding, ccode).enqueue(object : Callback<DBDeliveryStart> {
            override fun onResponse(
                call: Call<DBDeliveryStart>,
                response: Response<DBDeliveryStart>
            ) {
                response.body()?.results?.let {
                    if (it == "YES") {
//                        selList.clear()
//                        getDeliveryList()
                        // 영업담당자 문자보내기 ,우전앱 채팅방 문자 등록 등등 임시 막음
                        writeMsg(temp, hpRider)
                        MainActivity.noRider = ""
                        MainActivity.hpRider = ""
                        MainActivity.nmRider = ""
                        MainActivity.riding = ""
                        Toast.makeText(context, "전송완료 되었습니다.", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_navigation_notifications_to_navigation_home)
                    } else {
                        Toast.makeText(context, "등록시 문제발생", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onFailure(call: Call<DBDeliveryStart>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun startDeliveryInput() {
        val noRider = MainActivity.noRider
        val hpRider = MainActivity.hpRider
        val nmRider = MainActivity.nmRider
        val riding = MainActivity.riding
        val nmhosp = binding.etxtNmHosp.text.toString()

        val api = WoojeonApiService.create()
        api.insertDeliveryInput(noRider, nmRider, hpRider, nmhosp, riding).enqueue(object : Callback<DBDeliveryStart> {
            override fun onResponse(
                call: Call<DBDeliveryStart>,
                response: Response<DBDeliveryStart>
            ) {
                response.body()?.results?.let {
                    if (it == "YES") {
                        MainActivity.noRider = ""
                        MainActivity.hpRider = ""
                        MainActivity.nmRider = ""
                        MainActivity.riding = ""
                        Toast.makeText(context, "전송완료 되었습니다.", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_navigation_notifications_to_navigation_home)
                    } else {
                        Toast.makeText(context, "등록시 문제발생", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onFailure(call: Call<DBDeliveryStart>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun writeMsg(list: String, hpRider: String) {
        val api = WoojeonApiService.create()
        api.updateQuickStartMSG(
            "http://iclkorea.com/woojeon/m/rider_info_ok_WJ.asp",
            list, hpRider).enqueue(object : Callback<DBDeliveryStart> {
            override fun onResponse(
                call: Call<DBDeliveryStart>,
                response: Response<DBDeliveryStart>
            ) {
                response.body()?.results?.let {
                    if (it == "YES") {
                        Toast.makeText(context, "전송완료, 문자전송 완료 되었습니다.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "전송완료, 문자실패 되었습니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<DBDeliveryStart>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    inner class DeliveryListAdapter(private val context: Context, arLmList: ArrayList<DeliveryList>) : RecyclerView.Adapter<DeliveryListAdapter.ViewHolder>() {
        private val mList = arLmList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.row_out_list,
                parent,
                false
            )
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.chk.isChecked = false
            holder.imgLogo.clipToOutline = true
            holder.imgLogo.setImageResource(R.drawable.logo_icl_new)
            when (mList[position].gbn) {
                "ICL" -> holder.imgLogo.setImageResource(R.drawable.logo_icl_new)
                "Physiol" -> holder.imgLogo.setImageResource(R.drawable.logo_physiol_new)
                else -> holder.imgLogo.setImageResource(android.R.drawable.ic_menu_add)
            }

            if (selList.size > 0)
                for (item in selList) {
                    if (item == mList[position].noReq)
                        holder.chk.isChecked = true
                }
            else
                holder.chk.isChecked = false

//            binding.txtDeliCnt.text = "(${selList.size}건)"
            holder.textHosp.text = mList[position].nmHosp
            holder.textNoReq.text = " (${mList[position].noReq})"
            holder.textAddress.text = mList[position].addr
            holder.textDtReq.text = mList[position].dtAccept

            // chkbox click..
            holder.chk.setOnClickListener {
                if (holder.chk.isChecked) {
                    selList.add(mList[position].noReq!!)
                } else {
                    selList.remove(mList[position].noReq)
                }
                if (selList.size == 0)
                    binding.txtCnt.text = ""
                else
                    binding.txtCnt.text = "${selList.size}건"
            }

            //layout click.
            holder.llrow.setOnClickListener{
                holder.chk.toggle()
                if (holder.chk.isChecked) {
                    selList.add(mList[position].noReq!!)
                } else {
                    selList.remove(mList[position].noReq)
                }
                if (selList.size == 0)
                    binding.txtCnt.text = ""
                else
                    binding.txtCnt.text = "${selList.size}건"
            }
        }

        override fun getItemCount(): Int {
            return mList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textHosp : TextView = itemView.findViewById(R.id.txtDeliveryHosp)
            val textNoReq : TextView = itemView.findViewById(R.id.txtNoReq)
            val textAddress : TextView = itemView.findViewById(R.id.txtAddress)
            val textDtReq : TextView = itemView.findViewById(R.id.txtDtReq)
            val chk : CheckBox = itemView.findViewById(R.id.select)
            val llrow : LinearLayout = itemView.findViewById(R.id.llRow)
            val imgLogo : ImageView = itemView.findViewById(R.id.imgLogo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}