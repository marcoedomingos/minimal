package ao.marco.domingos.minimal.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Account (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var total: Double = 0.0
) {
    @Ignore
    var operations: List<Operation> = arrayListOf()
}