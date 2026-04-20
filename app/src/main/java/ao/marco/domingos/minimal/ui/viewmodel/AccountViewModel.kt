package ao.marco.domingos.minimal.ui.viewmodel

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

    suspend fun addOperation(amount: Double, type: OperationType) {
        _state.value = AccountState.Loading()
        withTimeout(2.seconds){
            withContext(Dispatchers.IO) {
                operationDao.insert(Operation(accountId = account.id, amount = amount, type = type))
                if(type == OperationType.CREDIT) {
                    account.total += amount
                } else {
                    account.total -= amount
                }
                account.operations = operationDao.getOperations(account.id)
                accountDao.update(account);
                _state.value = AccountState.Success(account = account);
            }
        }
    }
}