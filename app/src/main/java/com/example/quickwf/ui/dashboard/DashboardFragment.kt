package com.example.quickwf.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.support.v4.app.Fragment
import android.arch.lifecycle.ViewModelProvider
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.quickwf.MainActivity
import com.example.quickwf.R
import com.example.quickwf.data.DBDeliveryStart
import com.example.quickwf.data.DBRiderList
import com.example.quickwf.databinding.FragmentDashboardBinding
import com.example.quickwf.network.WoojeonApiService
import com.example.quickwf.network.networkMng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val dashboardViewModel =
//            ViewModelProvider(
//                this,
//                ViewModelProvider.NewInstanceFactory()
//            ).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.rb01.setOnClickListener(({
            binding.rg02.clearCheck()
        }))
        binding.rb02.setOnClickListener(({
            binding.rg02.clearCheck()
        }))
        binding.rb03.setOnClickListener(({
            binding.rg01.clearCheck()
        }))
        binding.rb04.setOnClickListener(({
            binding.rg01.clearCheck()
        }))

        binding.btnRegi.setOnClickListener(({
            if (chkRegi()) {
                setRegiRiderInfo()
            }
        }))
//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.noRider.observe(viewLifecycleOwner) {
//            textView.text = it
//
//        }
        return root
    }

    private fun setRegiRiderInfo() {
        var norider = binding.etxtNoRiderRegi.text.toString()
        norider = norider.replace(" ", "")
        var nmrider = binding.eTxtNmRiderRegi.text.toString()
        nmrider = nmrider.replace(" ", "")
        var hprider = binding.etxtHpRiderRegi.text.toString()
        hprider = hprider.replace(" ", "")
        var methrider = ""
        if (binding.rb01.isChecked) methrider = binding.rb01.text.toString()
        if (binding.rb02.isChecked) methrider = binding.rb02.text.toString()
        if (binding.rb03.isChecked) methrider = binding.rb03.text.toString()
        if (binding.rb04.isChecked) methrider = binding.rb04.text.toString()
        if (networkMng(context!!).checkNetworkState()) {
            val api = WoojeonApiService.create()
            api.insertRiderInfo(norider, nmrider, hprider, methrider).enqueue(object : Callback<DBDeliveryStart> {
                override fun onResponse(call: Call<DBDeliveryStart>, response: Response<DBDeliveryStart>) {
                    response.body()?.results?.let {
                        if (it == "YES") {
                            Toast.makeText(context, "등록완료 되었습니다.", Toast.LENGTH_LONG).show()
                            MainActivity.noRider = norider
                            MainActivity.nmRider = nmrider
                            MainActivity.hpRider = hprider
                            MainActivity.riding = methrider
                            findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_notifications)
                        } else if (it == "Duplicate") {
                            Toast.makeText(context, "중복된 자료가 있습니다.", Toast.LENGTH_LONG).show()
                            MainActivity.noRider = norider
                            MainActivity.confirm = "Y"
                            findNavController().navigate(R.id.action_navigation_dashboard_to_navigation_home)
                        } else {
                            Toast.makeText(context, "등록시 문제발생", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                override fun onFailure(call: Call<DBDeliveryStart>, t: Throwable) {
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

    private fun chkRegi():Boolean{
        if (binding.etxtNoRiderRegi.text.isNullOrEmpty()) {
            Toast.makeText(context, "라이더 번호를 입력해주세요", Toast.LENGTH_LONG).show()
            return false
        }
        if (binding.eTxtNmRiderRegi.text.isNullOrEmpty()) {
            Toast.makeText(context, "성함을 입력해주세요", Toast.LENGTH_LONG).show()
            return false
        }
        if (binding.etxtHpRiderRegi.text.isNullOrEmpty() || binding.etxtHpRiderRegi.text.length <= 0) {
            Toast.makeText(context, "핸드폰 번호를 입력해주세요", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}