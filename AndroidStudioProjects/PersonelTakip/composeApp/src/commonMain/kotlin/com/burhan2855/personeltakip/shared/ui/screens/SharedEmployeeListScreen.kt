package com.burhan2855.personeltakip.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burhan2855.personeltakip.shared.data.Employee
import com.burhan2855.personeltakip.shared.logic.IEmployeeService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedEmployeeListScreen(
    employeeService: IEmployeeService,
    onAddEmployee: () -> Unit,
    onEmployeeClick: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val employees by employeeService.getAllEmployees().collectAsState(initial = emptyList())
    
    LaunchedEffect(employees.size) {
        println("SharedEmployeeListScreen: Personel listesi yenilendi. Yeni sayı: ${employees.size}")
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personel Listesi") },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = { /* TODO: Filtreleme */ }) {
                        Icon(Icons.Default.Search, null)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Daha Fazla")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Yedek Al (İndir)") },
                            onClick = {
                                showMenu = false
                                employeeService.exportBackup()
                            },
                            leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Yedek Yükle") },
                            onClick = {
                                showMenu = false
                                employeeService.importBackup { success ->
                                    // Flow updates automatically
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, null) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
        }
    ) { padding ->
        if (showAddDialog) {
            SharedEmployeeDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { employee ->
                    scope.launch {
                        employeeService.insertEmployee(employee)
                    }
                    showAddDialog = false
                    onAddEmployee()
                }
            )
        }
        if (employees.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.People, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Henüz personel eklenmedi", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(employees) { employee ->
                    SharedEmployeeItem(
                        employee = employee,
                        onClick = { onEmployeeClick(employee.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SharedEmployeeItem(employee: Employee, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil Resmi Yer Tutucu (Web'de resim yükleme daha sonra eklenecek)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${employee.firstName} ${employee.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = employee.position,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
