package org.fossasia.openevent.general.order

import android.service.carrier.CarrierIdentifier
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrders(orders: List<Order>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(order: Order)

    @Query("SELECT * FROM `order`")
    fun getOrdersLocal():Single<List<Order>>

    @Query("SELECT * FROM `order` WHERE identifier = :identifier")
    fun getOrder(identifier: String):Single<Order>
}
