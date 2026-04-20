package ao.marco.domingos.minimal.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.Date

enum class OperationType {
    CREDIT, DEBIT
}

@Entity
data class Operation (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int = 0,
    val amount: Double = 0.0,
    val type: OperationType,
    val creationDate: LocalDate = LocalDate.now()
)
