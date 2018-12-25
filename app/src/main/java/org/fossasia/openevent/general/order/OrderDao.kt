package org.fossasia.openevent.general.order

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrders(orders: List<Order>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(order: Order)
}
