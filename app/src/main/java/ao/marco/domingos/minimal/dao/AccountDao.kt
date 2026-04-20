package ao.marco.domingos.minimal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ao.marco.domingos.minimal.entity.Account

@Dao
interface AccountDao {
    @Query("SELECT * FROM account LIMIT 1")
    suspend fun getAccount(): Account?

    @Insert
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)
}