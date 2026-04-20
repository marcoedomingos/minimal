package ao.marco.domingos.minimal.config

import androidx.room.TypeConverter
import ao.marco.domingos.minimal.entity.OperationType
import java.time.LocalDate
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromOperationType(value: String?): OperationType? {
        return value?.let { OperationType.valueOf(it) }
    }

    @TypeConverter
    fun operationTypeToString(type: OperationType?): String? {
        return type?.name
    }
}
