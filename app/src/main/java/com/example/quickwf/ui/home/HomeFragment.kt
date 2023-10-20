package com.example.quickwf.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.example.quickwf.MainActivity
import com.example.quickwf.R
import com.example.quickwf.data.*
import com.example.quickwf.databinding.FragmentHomeBinding
import com.example.quickwf.hideKeyboard
import com.example.quickwf.network.WoojeonApiService
import com.example.quickwf.network.networkMng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private var arRiderList : ArrayList<RiderList> = ArrayList()
    private var riderListAdapter : RiderListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val homeViewModel =
//            ViewModelProvider(
//                this,
//                ViewModelProvider.NewInstanceFactory()
//            ).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvRsRiderList.layoutManager = layoutManager
        riderListAdapter = RiderListAdapter(context!!, arRiderList)
        binding.rvRsRiderList.adapter = riderListAdapter

        binding.btnRsSearch.setOnClickListener(({
            var temp = binding.etxtRsNoRider.text.toString()
            if (temp.isEmpty() || temp.isBlank())
                Toast.makeText(context, resources.getText(R.string.rs_err_norider), Toast.LENGTH_LONG).show()
            else {
                temp = temp.replace(" ","")
                activity?.let { it1 -> hideKeyboard(it1) }
                getRiderList(temp)
            }
        }))

        if (MainActivity.confirm == "Y") {
            MainActivity.confirm = "N"
            binding.etxtRsNoRider.setText(MainActivity.noRider)
            getRiderList(MainActivity.noRider)
        }
//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }


        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getRiderList(noRider:String) {
        arRiderList.clear()
        if (networkMng(context!!).checkNetworkState()) {
            val api = WoojeonApiService.create()
//            val selList2 : ArrayList<String> = ArrayList()
            api.selectRiderList(noRider).enqueue(object : Callback<DBRiderList> {
                override fun onResponse(call: Call<DBRiderList>, response: Response<DBRiderList>) {
                    response.body()?.results?.let {
                        arRiderList.addAll(it)
                        riderListAdapter!!.notifyDataSetChanged()
                        if (it.size==0) Toast.makeText(context,"라이더 등록을 해주세요", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<DBRiderList>, t: Throwable) {
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

    inner class RiderListAdapter(private val context: Context, arLmList: ArrayList<RiderList>) : RecyclerView.Adapter<RiderListAdapter.ViewHolder>() {
        private val mList = arLmList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(
                R.layout.row_rs_list,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textNm.text = mList[position].nmRider
            holder.textHp.text = mList[position].hpRider

            holder.btnSel.setOnClickListener{
                MainActivity.nmRider = mList[position].nmRider!!
                MainActivity.noRider = binding.etxtRsNoRider.text.toString()
                MainActivity.hpRider = mList[position].hpRider!!
                MainActivity.riding = mList[position].riding!!
//                val dashboardViewModel = DashboardViewModel()
//                dashboardViewModel.setRider(mList[position].nmRider!!,mList[position].nmRider!!,mList[position].nmRider!!)
//                val navController = findNavController(parentFragment!!)
//                navController.navigate(R.id.navigation_dashboard)
                findNavController().navigate(R.id.action_navigation_home_to_navigation_notifications)
            }
        }

        override fun getItemCount(): Int {
            return mList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textNm : TextView = itemView.findViewById(R.id.txtRowRsName)
            val textHp : TextView = itemView.findViewById(R.id.txtRowRsTel)
            val btnSel : Button = itemView.findViewById(R.id.btnRsSearchbutton)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}