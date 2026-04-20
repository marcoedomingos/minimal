package ao.marco.domingos.minimal.ui.pages

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ao.marco.domingos.minimal.config.AppDatabase
import ao.marco.domingos.minimal.entity.Account
import ao.marco.domingos.minimal.entity.OperationType
import ao.marco.domingos.minimal.ui.theme.Black
import ao.marco.domingos.minimal.ui.theme.Green
import ao.marco.domingos.minimal.ui.theme.Red
import ao.marco.domingos.minimal.ui.theme.White
import ao.marco.domingos.minimal.ui.viewmodel.AccountState
import ao.marco.domingos.minimal.ui.viewmodel.AccountViewModel
import kotlinx.coroutines.launch

@Composable
fun HomePage(paddingValues: PaddingValues, db: AppDatabase, viewModel: AccountViewModel) {
    val account = remember { mutableStateOf<Account>(Account()) }
    val isShowing = remember { mutableStateOf<Boolean>(false) }
    val isOpened = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(key1 = viewModel) {
        viewModel.getAccount()
        Log.d("Debug", state.value.toString())
        if (state.value is AccountState.Initial || state.value is AccountState.Loading)
            account.value = Account()
        if (state.value is AccountState.Success) {
            account.value = (state.value as AccountState.Success).account;
        }
    }

    if (state.value is AccountState.Success) {
        Log.d("Debug", state.value.toString())
        account.value = (state.value as AccountState.Success).account;
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(20.dp),
            content = {
                HeaderComponent(capital = account.value.total, isShowing = isShowing)
                Button(
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                    enabled = true,
                    colors = ButtonColors(
                        containerColor = Black,
                        contentColor = White,
                        disabledContentColor = White,
                        disabledContainerColor = Black,
                    ),
                    onClick = {
                        isOpened.value = true;
                    },
                    content = {
                        Text("Adicionar Custos")
                    })
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    content = {
                        Text("Valor")
                        Text("Data")
                    })
                account.value.operations.map { operation ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween, content = {
                            Text(
                                operation.amount.toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (operation.type == OperationType.CREDIT) Green else Red
                            )
                            Text(operation.creationDate.toString())
                        })
                    HorizontalDivider(
                        Modifier.fillMaxWidth()
                    )

                }
                if (account.value.operations.isEmpty()) Text(
                    "Sem Custos feitos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    modifier = Modifier.fillMaxHeight(0.5F)
                )
                AnimatedVisibility(
                    visible = isOpened.value, content = {
                        Dialog(properties = DialogProperties(), onDismissRequest = {
                            isOpened.value = false
                        }, content = {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Black, RoundedCornerShape(10.dp))
                                    .fillMaxWidth()
                                    .background(White)
                                    .align(Alignment.CenterHorizontally), content = {
                                    var value by remember { mutableStateOf("") }
                                    var selected by remember { mutableStateOf(OperationType.CREDIT) }
                                    var isDropped by remember { mutableStateOf(false) }
                                    Spacer(Modifier.height(5.dp))
                                    TextField(
                                        value = value,
                                        onValueChange = {
                                            value = it
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 5.dp)
                                            .border(
                                                1.dp, Black, RoundedCornerShape(10.dp)
                                            ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        placeholder = {
                                            Text("Insira o valor")
                                        })
                                    Spacer(Modifier.height(10.dp))
                                    TextField(
                                        value = selected.name,
                                        enabled = false,
                                        readOnly = true,
                                        onValueChange = {

                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 5.dp)
                                            .border(
                                                1.dp, Black, RoundedCornerShape(10.dp)
                                            )
                                            .clickable(onClick = {
                                                isDropped = true;
                                            }),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        label = {
                                            Text("Tipo de custo")
                                        })
                                    DropdownMenu(
                                        tonalElevation = 0.dp,
                                        modifier = Modifier
                                            .width(IntrinsicSize.Min)
                                            .clip(
                                                RoundedCornerShape(10.dp)
                                            ),
                                        expanded = isDropped,
                                        onDismissRequest = {
                                            isDropped = false
                                        },
                                        content = {
                                            OperationType.entries.forEach { type ->
                                                DropdownMenuItem(
                                                    onClick = {
                                                        selected = type
                                                        isDropped = false
                                                    },
                                                    text = { Text(type.name) },
                                                )
                                                HorizontalDivider(modifier = Modifier.height(2.dp))
                                            }
                                        })
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                                        enabled = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonColors(
                                            containerColor = Black,
                                            contentColor = White,
                                            disabledContentColor = White,
                                            disabledContainerColor = Black,
                                        ),
                                        onClick = {
                                            scope.launch {
                                                viewModel.addOperation(value.toDouble(), selected)
                                                isOpened.value = false
                                            }
                                        },
                                        content = {
                                            Text("Salvar")
                                        })
                                    Spacer(Modifier.height(10.dp))
                                })
                        })
                    })
            }
        )
    }
}

@Composable
fun HeaderComponent(capital: Double, isShowing: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Capital Total", fontSize = 16.sp, color = Black)
            Text(
                text = if (isShowing.value) "$capital kz" else "**** kz",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier.clickable { isShowing.value = !isShowing.value })
        }
    }
}