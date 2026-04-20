package ao.marco.domingos.minimal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ao.marco.domingos.minimal.entity.Operation

@Dao
interface OperationDao {
    @Query("SELECT * FROM operation WHERE accountId = :id")
    fun getOperations(id:Int): List<Operation>

    @Insert
    suspend fun insert(operation: Operation)
}