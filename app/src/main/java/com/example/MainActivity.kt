package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AddEditLogDialog
import com.example.ui.screens.DailyLogScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SummaryReportScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WorkLogViewModel

enum class NavScreen(val title: String) {
    DAILY("နေ့စဉ် အလုပ်မှတ်တမ်း"),
    SUMMARY("စာရင်းချုပ် (Total & OT)"),
    SETTINGS("သတ်မှတ်ချက် (Settings)")
}

class MainActivity : ComponentActivity() {
    private val viewModel: WorkLogViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var currentScreen by remember { mutableStateOf(NavScreen.DAILY) }

                val isAddEditOpen by viewModel.isAddEditOpen.collectAsStateWithLifecycle()
                val editingLog by viewModel.editingLog.collectAsStateWithLifecycle()
                val defaultDayWage by viewModel.defaultDayWage.collectAsStateWithLifecycle()
                val defaultOtRate by viewModel.defaultOtRate.collectAsStateWithLifecycle()
                val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

                // BackHandler for secondary screens
                if (currentScreen != NavScreen.DAILY) {
                    BackHandler {
                        currentScreen = NavScreen.DAILY
                    }
                }

                // Show Snackbar on user message
                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearUserMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentScreen.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = Color.White
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == NavScreen.DAILY,
                                onClick = { currentScreen = NavScreen.DAILY },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "မှတ်တမ်း"
                                    )
                                },
                                label = { Text("မှတ်တမ်း") },
                                modifier = Modifier.testTag("nav_daily")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.SUMMARY,
                                onClick = { currentScreen = NavScreen.SUMMARY },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = "စာရင်းချုပ်"
                                    )
                                },
                                label = { Text("စာရင်းချုပ်") },
                                modifier = Modifier.testTag("nav_summary")
                            )

                            NavigationBarItem(
                                selected = currentScreen == NavScreen.SETTINGS,
                                onClick = { currentScreen = NavScreen.SETTINGS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "ဆက်တင်"
                                    )
                                },
                                label = { Text("ဆက်တင်") },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentScreen == NavScreen.DAILY) {
                            FloatingActionButton(
                                onClick = { viewModel.openAddLog() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                modifier = Modifier.testTag("fab_add_log")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "မှတ်တမ်းအသစ်ထည့်မည်"
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            NavScreen.DAILY -> {
                                DailyLogScreen(viewModel = viewModel)
                            }
                            NavScreen.SUMMARY -> {
                                SummaryReportScreen(
                                    viewModel = viewModel,
                                    onShowMessage = { msg ->
                                        viewModel.showUserMessage(msg)
                                    }
                                )
                            }
                            NavScreen.SETTINGS -> {
                                SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // Add / Edit Work Log Dialog
                    if (isAddEditOpen && editingLog != null) {
                        AddEditLogDialog(
                            initialLog = editingLog!!,
                            defaultDayWage = defaultDayWage,
                            defaultOtRate = defaultOtRate,
                            onDismiss = { viewModel.closeAddEdit() },
                            onSave = { updatedLog -> viewModel.saveLog(updatedLog) }
                        )
                    }
                }
            }
        }
    }
}
