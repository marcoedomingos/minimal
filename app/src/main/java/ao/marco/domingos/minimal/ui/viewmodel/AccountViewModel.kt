package ao.marco.domingos.minimal.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import ao.marco.domingos.minimal.config.AppDatabase
import ao.marco.domingos.minimal.entity.Account
import ao.marco.domingos.minimal.entity.Operation
import ao.marco.domingos.minimal.entity.OperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

sealed class AccountState {
    class Initial : AccountState();
    class Loading : AccountState();
    data class Success(val account: Account) : AccountState();
}

class AccountViewModel(val db: AppDatabase) : ViewModel() {
    private val _state = MutableStateFlow<AccountState>(AccountState.Initial())
    val state = _state.asStateFlow()
    val operationDao = db.operationDao()
    val accountDao = db.accountDao()
    var account: Account = Account()

    suspend fun getAccount() {
        withContext(Dispatchers.IO) {
            _state.value = AccountState.Loading()
            var dbAccount = accountDao.getAccount();
            if (dbAccount == null) {
                accountDao.insert(account);
                dbAccount = account
            }
            dbAccount.operations = operationDao.getOperations(dbAccount.id)
            account = dbAccount;
            _state.value = AccountState.Success(account = dbAccount);
        }
    }

    suspend fun addOperation(id: Int?, title: String, amount: Double, type: OperationType) {
        _state.value = AccountState.Loading()
        withTimeout(2.seconds) {
            withContext(Dispatchers.IO) {
                if (id != null) {
                    operationDao.update(
                        Operation(
                            id = id,
                            accountId = account.id,
                            title = title,
                            amount = amount,
                            type = type
                        )
                    )
                } else {
                    operationDao.insert(
                        Operation(
                            accountId = account.id,
                            title = title,
                            amount = amount,
                            type = type
                        )
                    )
                }
                var total = 0.0
                val allOperations = operationDao.getOperations(account.id)
                allOperations.forEach { operation ->
                    if (operation.type == OperationType.CREDIT) {
                        total += amount
                    } else {
                        total -= amount
                    }
                };
                accountDao.update(account)
                account = account.copy(total = total).apply {
                    this.operations = allOperations
                }
                _state.value = AccountState.Success(account = account);
            }
        }
    }

    suspend fun filterByDate(month: Int) {
        _state.value = AccountState.Loading()
        withContext(Dispatchers.IO) {
            val allOperations = operationDao.getOperations(account.id)
            val filteredOperations =
                allOperations.filter { it.creationDate.monthValue == month + 1 }
            var total = 0.0

            filteredOperations.forEach { operation ->
                if (operation.type == OperationType.CREDIT) {
                    total += operation.amount
                } else {
                    total -= operation.amount
                }
            }

            val filteredAccount = account.copy(total = total).apply {
                this.operations = filteredOperations
            }

            _state.value = AccountState.Success(account = filteredAccount)
        }
    }
}