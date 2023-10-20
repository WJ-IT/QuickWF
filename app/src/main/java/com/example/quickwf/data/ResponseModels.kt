package com.example.quickwf.data

//검색화면: 라이더 검색
data class DBRiderList(val results : List<RiderList>)
data class RiderList(val nmRider:String?, val hpRider:String?, val riding:String?)

//
data class DBDeliveryList(val results : List<DeliveryList>)
data class DeliveryList(
        val noReq:String?,
        val nmHosp:String?,
        val dtAccept:String?,
        val addr:String?,
        val manager:String?,
        val noTel:String?,
        val amt:String?,
        val gbn:String?,
        val fgQuick:String?
        )

data class DBDeliveryStart(val results : String)

data class DBDeliveryInfo(val results : List<DeliveryInfo>)
data class DeliveryInfo (
        val noRider : String?,
        val hpRider : String?
        )
