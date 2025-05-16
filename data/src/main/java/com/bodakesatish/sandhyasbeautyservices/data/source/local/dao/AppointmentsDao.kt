package com.bodakesatish.sandhyasbeautyservices.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentCustomer
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.AppointmentsEntity
import com.bodakesatish.sandhyasbeautyservices.data.source.local.entity.CustomerEntity
import com.bodakesatish.sandhyasbeautyservices.domain.repository.AppointmentStatus
import com.bodakesatish.sandhyasbeautyservices.domain.repository.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentsDao {

    // Example: A flexible query to get appointments with multiple filters
    // For simplicity, this query assumes appointmentDate is a Long (timestamp)
    // You might need to join with a Customer table if customerName is not denormalized
    @Query("""
           SELECT * FROM ${AppointmentsEntity.TABLE_NAME}
           WHERE (:status IS NULL OR ${AppointmentsEntity.Columns.APPOINTMENT_STATUS} = :status)
           AND (:paymentStatus IS NULL OR ${AppointmentsEntity.Columns.PAYMENT_STATUS} = :paymentStatus)
           AND (:customerId IS NULL OR ${AppointmentsEntity.Columns.CUSTOMER_ID} = :customerId)
           AND (${AppointmentsEntity.Columns.APPOINTMENT_DATE} >= :startDate AND ${AppointmentsEntity.Columns.APPOINTMENT_DATE} <= :endDate)
           ORDER BY ${AppointmentsEntity.Columns.APPOINTMENT_DATE} DESC
       """)
    fun getAppointmentsFiltered(
        startDate: Long,
        endDate: Long,
        status: AppointmentStatus?,      // Nullable: if null, this filter is ignored
        paymentStatus: PaymentStatus?,  // Nullable: if null, this filter is ignored
        customerId: Int?                // Nullable: if null, this filter is ignored
        // Add sorting parameters here later if needed
    ): Flow<List<AppointmentCustomer>> // Consider returning AppointmentWithCustomer if you have a relation

//    // If you want to filter by customer name directly (assuming customerName is in Appointment table)
//    @Query("""
//           SELECT * FROM ${AppointmentsEntity.TABLE_NAME}
//           WHERE (:status IS NULL OR ${AppointmentsEntity.Columns.APPOINTMENT_STATUS} = :status)
//           AND (:paymentStatus IS NULL OR ${AppointmentsEntity.Columns.PAYMENT_STATUS} = :paymentStatus)
//           AND (:customerNameQuery IS NULL OR customerName LIKE '%' || :customerNameQuery || '%')
//           AND (${AppointmentsEntity.Columns.APPOINTMENT_DATE} >= :startDate AND ${AppointmentsEntity.Columns.APPOINTMENT_DATE} <= :endDate)
//           ORDER BY ${AppointmentsEntity.Columns.APPOINTMENT_DATE} DESC
//       """)

    @Transaction
    @Query("""
    SELECT * FROM ${AppointmentsEntity.TABLE_NAME}
    WHERE 
        (${AppointmentsEntity.Columns.CUSTOMER_ID} IN (
            SELECT ${CustomerEntity.Columns.ID} FROM ${CustomerEntity.TABLE_NAME}
            WHERE (:customerNameQuery IS NULL OR 
                   (${CustomerEntity.Columns.FIRST_NAME} LIKE '%' || :customerNameQuery || '%' OR
                    ${CustomerEntity.Columns.LAST_NAME} LIKE '%' || :customerNameQuery || '%' OR
                    (${CustomerEntity.Columns.FIRST_NAME} || ' ' || ${CustomerEntity.Columns.LAST_NAME}) LIKE '%' || :customerNameQuery || '%'))
        ) OR :customerNameQuery IS NULL) -- This OR :customerNameQuery IS NULL handles the case where the filter is not applied
        AND (:status IS NULL OR ${AppointmentsEntity.Columns.APPOINTMENT_STATUS} = :status)
        AND (:paymentStatus IS NULL OR ${AppointmentsEntity.Columns.PAYMENT_STATUS} = :paymentStatus)
        AND (${AppointmentsEntity.Columns.APPOINTMENT_DATE} >= :startDate AND ${AppointmentsEntity.Columns.APPOINTMENT_DATE} <= :endDate)
    ORDER BY ${AppointmentsEntity.Columns.APPOINTMENT_DATE} DESC
""")
    fun getAppointmentsFilteredWithCustomerName(
        startDate: Long,
        endDate: Long,
        status: AppointmentStatus?,
        paymentStatus: PaymentStatus?,
        customerNameQuery: String? // For searching customer name
    ): Flow<List<AppointmentCustomer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(appointment: AppointmentsEntity): Long

    @Update
    suspend fun update(appointment: AppointmentsEntity): Int

    @Query("DELETE FROM ${AppointmentsEntity.TABLE_NAME} WHERE ${AppointmentsEntity.Columns.ID} = :appointmentId")
    fun delete(appointmentId: Int)

    @Query("SELECT * FROM ${AppointmentsEntity.TABLE_NAME}")
    fun getAppointmentList(): Flow<List<AppointmentsEntity>>

    @Query("SELECT * FROM ${AppointmentsEntity.TABLE_NAME} WHERE ${AppointmentsEntity.Columns.ID} = :appointmentId")
    fun getAppointmentById(appointmentId: Int): Flow<AppointmentsEntity?>

    @Query("SELECT * FROM ${AppointmentsEntity.TABLE_NAME}")
    fun getAppointmentCustomerList(): Flow<List<AppointmentCustomer>>

    /**
     * Fetches appointments with flexible filtering and dynamic sorting.
     *
     * This query joins Appointments with Customers to allow filtering by customer name
     * and sorting by columns from both tables.
     *
     * Note on sortByColumnName: This parameter is directly inserted into the ORDER BY clause.
     * Ensure that the values passed for sortByColumnName are sanitized and correspond to
     * actual column names (or aliases defined in the SELECT statement if complex) to prevent SQL injection
     * if the source of this string is not controlled. In our case, it comes from an enum mapping, which is safe.
     */
    @Transaction // Good practice for queries involving relations
    @Query("""
        SELECT apt.* FROM ${AppointmentsEntity.TABLE_NAME} apt
        LEFT JOIN ${CustomerEntity.TABLE_NAME} cust ON apt.${AppointmentsEntity.Columns.CUSTOMER_ID} = cust.${CustomerEntity.Columns.ID}
        WHERE
            (:status IS NULL OR apt.${AppointmentsEntity.Columns.APPOINTMENT_STATUS} = :status)
            AND (:paymentStatus IS NULL OR apt.${AppointmentsEntity.Columns.PAYMENT_STATUS} = :paymentStatus)
            AND (apt.${AppointmentsEntity.Columns.APPOINTMENT_DATE} >= :startDate AND apt.${AppointmentsEntity.Columns.APPOINTMENT_DATE} <= :endDate)
            AND (
                :customerNameQuery IS NULL OR :customerNameQuery = '' OR
                cust.${CustomerEntity.Columns.FIRST_NAME} LIKE '%' || :customerNameQuery || '%' OR
                cust.${CustomerEntity.Columns.LAST_NAME} LIKE '%' || :customerNameQuery || '%' OR
                (cust.${CustomerEntity.Columns.FIRST_NAME} || ' ' || cust.${CustomerEntity.Columns.LAST_NAME}) LIKE '%' || :customerNameQuery || '%'
            )
        ORDER BY
            CASE WHEN :sortByColumnName = '${AppointmentsEntity.Columns.APPOINTMENT_DATE}' AND :sortOrderSql = 'ASC' THEN apt.${AppointmentsEntity.Columns.APPOINTMENT_DATE} END ASC,
            CASE WHEN :sortByColumnName = '${AppointmentsEntity.Columns.APPOINTMENT_DATE}' AND :sortOrderSql = 'DESC' THEN apt.${AppointmentsEntity.Columns.APPOINTMENT_DATE} END DESC,
            CASE WHEN :sortByColumnName = '${CustomerEntity.Columns.FIRST_NAME}' AND :sortOrderSql = 'ASC' THEN cust.${CustomerEntity.Columns.FIRST_NAME} END ASC,
            CASE WHEN :sortByColumnName = '${CustomerEntity.Columns.FIRST_NAME}' AND :sortOrderSql = 'DESC' THEN cust.${CustomerEntity.Columns.FIRST_NAME} END DESC,
            CASE WHEN :sortByColumnName = '${AppointmentsEntity.Columns.APPOINTMENT_STATUS}' AND :sortOrderSql = 'ASC' THEN apt.${AppointmentsEntity.Columns.APPOINTMENT_STATUS} END ASC,
            CASE WHEN :sortByColumnName = '${AppointmentsEntity.Columns.APPOINTMENT_STATUS}' AND :sortOrderSql = 'DESC' THEN apt.${AppointmentsEntity.Columns.APPOINTMENT_STATUS} END DESC
            -- Add more CASE statements for other sortable columns as needed.
            -- Fallback sort order if no match or if sortByColumnName is not one of the handled ones
            , apt.${AppointmentsEntity.Columns.APPOINTMENT_DATE} DESC
    """)
    fun getFilteredAppointments(
        startDate: Long,
        endDate: Long,
        status: String?, // Use String if enums are converted in Repository, or use enums with TypeConverters
        paymentStatus: String?, // Same as above
        customerNameQuery: String?,
        sortByColumnName: String, // e.g., "appointmentDate", "customerFirstName", "status"
        sortOrderSql: String      // "ASC" or "DESC"
    ): Flow<List<AppointmentCustomer>> // Returns the POJO that Room populates with Appointment and related Customer

    // The old getAppointmentsFiltered can be removed or kept if it serves a different specific purpose
    // The old getAppointmentsFilteredWithCustomerName can also be removed as its functionality is now
    // covered by the more generic getFilteredAppointments.

}