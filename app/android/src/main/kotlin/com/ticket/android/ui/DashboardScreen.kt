package com.ticket.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticket.common.viewmodel.dashboardViewModel

@Composable
fun DashboardScreen(navController: NavController) {
    val viewModel: dashboardViewModel = remember { dashboardViewModel() }
    var refresh by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(imageVector = Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = {}
                )
                BottomNavigationItem(
                    icon = { Icon(imageVector = Icons.Filled.Ticket, contentDescription = "Tickets") },
                    label = { Text("Tickets") },
                    onClick = {
                        navController.navigate("ticket-list")
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Usuarios") },
                    label = { Text("Usuarios") },
                    onClick = {
                        navController.navigate("user-management")
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Start,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bienvenido al Sistema",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Avatar(
                            imageVector = Icons.Filled.Person,
                            size = 32.dp,
                            contentDescription = "Usuario"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Gestión de Soporte TI",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { /* Ver tickets */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver todos los tickets")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        elevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val stats = viewModel.ticketStats
                                Text(
                                    text = "Total: ${stats.total}",
                                    style = MaterialTheme.typography.h5,
                                    color = MaterialTheme.colors.primary
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "Abiertos: ${stats.abiertos}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colors.error
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Resueltos: ${stats.resueltos}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colors.success
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "Urgentes: ${stats.urgentes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colors.warning
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Estadísticas recientes",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(16.dp, top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (refresh) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterRight))
            }

            Button(
                onClick = {
                    refresh = true
                    viewModel.loadStats()
                    refresh = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar estadísticas")
            }
        }
    }
}