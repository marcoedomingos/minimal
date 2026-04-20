package ao.marco.domingos.minimal.config

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ao.marco.domingos.minimal.dao.AccountDao
import ao.marco.domingos.minimal.dao.OperationDao
import ao.marco.domingos.minimal.entity.Account
import ao.marco.domingos.minimal.entity.Operation

@Database(entities = [Account::class, Operation::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun accountDao(): AccountDao
    abstract fun operationDao(): OperationDao
}
