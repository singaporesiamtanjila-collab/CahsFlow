package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CncDesignEntity
import com.example.security.BiometricLockScreen
import com.example.ui.CncViewModel
import com.example.ui.components.AnimatedCncCutter
import com.example.ui.screens.AddEditDesignSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DailyWorkScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CncBackground
import com.example.ui.theme.CncBorder
import com.example.ui.theme.CncBorderBright
import com.example.ui.theme.CncLaserCyan
import com.example.ui.theme.CncPrimary
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CncViewModel = viewModel()
                CncAppRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CncAppRoot(viewModel: CncViewModel) {
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingDesign by remember { mutableStateOf<CncDesignEntity?>(null) }

    // If app is locked, render the Biometric / PIN lock screen
    if (isAppLocked) {
        BiometricLockScreen(
            securityManager = viewModel.securityManager,
            onUnlocked = { viewModel.unlockApp() }
        )
        return
    }

    BackHandler(enabled = currentTab != 0) {
        viewModel.currentTab.value = 0
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CncBackground)
            .statusBarsPadding(),
        topBar = {
            CncTopBar(
                onLockClick = { viewModel.lockApp() }
            )
        },
        bottomBar = {
            CncBottomNavigation(
                selectedTab = currentTab,
                onSelectTab = { viewModel.currentTab.value = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DailyWorkScreen(
                    viewModel = viewModel,
                    onAddNewDesign = {
                        editingDesign = null
                        showAddEditSheet = true
                    },
                    onEditDesign = { design ->
                        editingDesign = design
                        showAddEditSheet = true
                    }
                )
                1 -> AnalyticsScreen(viewModel = viewModel)
                2 -> CatalogScreen(
                    viewModel = viewModel,
                    onEditDesign = { design ->
                        editingDesign = design
                        showAddEditSheet = true
                    }
                )
                3 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    if (showAddEditSheet) {
        AddEditDesignSheet(
            initialDesign = editingDesign,
            defaultDate = selectedDate,
            currencySymbol = currencySymbol,
            onDismiss = {
                showAddEditSheet = false
                editingDesign = null
            },
            onSave = { saved ->
                viewModel.saveDesign(saved)
                showAddEditSheet = false
                editingDesign = null
            }
        )
    }
}

@Composable
fun CncTopBar(
    onLockClick: () -> Unit
) {
    Surface(
        color = CncSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cnc_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedCncCutter(size = 30.dp)

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CNC CRAFT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CncTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(CncLaserCyan, CircleShape)
                        )
                    }

                    Text(
                        text = "Delay Work & Income Hub",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = CncTextSecondary
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = CncSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CncBorderBright),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onLockClick)
                    .testTag("quick_lock_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock App",
                        tint = CncPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CncBottomNavigation(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    NavigationBar(
        containerColor = CncSurface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(1.dp, CncBorder)
            .testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onSelectTab(0) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Default.Today else Icons.Outlined.Today,
                    contentDescription = "Daily Work"
                )
            },
            label = {
                Text(
                    text = "Daily",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CncPrimary,
                selectedTextColor = CncPrimary,
                indicatorColor = CncLaserCyan.copy(alpha = 0.15f),
                unselectedIconColor = CncTextSecondary,
                unselectedTextColor = CncTextSecondary
            ),
            modifier = Modifier.testTag("tab_daily")
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onSelectTab(1) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 1) Icons.Default.AutoGraph else Icons.Outlined.AutoGraph,
                    contentDescription = "Analytics"
                )
            },
            label = {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CncPrimary,
                selectedTextColor = CncPrimary,
                indicatorColor = CncLaserCyan.copy(alpha = 0.15f),
                unselectedIconColor = CncTextSecondary,
                unselectedTextColor = CncTextSecondary
            ),
            modifier = Modifier.testTag("tab_analytics")
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onSelectTab(2) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 2) Icons.Default.Folder else Icons.Outlined.Folder,
                    contentDescription = "Catalog"
                )
            },
            label = {
                Text(
                    text = "Catalog",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CncPrimary,
                selectedTextColor = CncPrimary,
                indicatorColor = CncLaserCyan.copy(alpha = 0.15f),
                unselectedIconColor = CncTextSecondary,
                unselectedTextColor = CncTextSecondary
            ),
            modifier = Modifier.testTag("tab_catalog")
        )

        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onSelectTab(3) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 3) Icons.Default.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = {
                Text(
                    text = "Backup & Vault",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CncPrimary,
                selectedTextColor = CncPrimary,
                indicatorColor = CncLaserCyan.copy(alpha = 0.15f),
                unselectedIconColor = CncTextSecondary,
                unselectedTextColor = CncTextSecondary
            ),
            modifier = Modifier.testTag("tab_settings")
        )
    }
}
