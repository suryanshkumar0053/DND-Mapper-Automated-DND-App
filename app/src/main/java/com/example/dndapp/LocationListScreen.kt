package com.example.dndapp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dndapp.ui.theme.MainViewModel
@Composable
fun LocationListScreen(
    navController: NavController,
    viewModel: MainViewModel,
    locationUtils: LocationUtils
) {

    val locations by viewModel.geofences.collectAsState() // FIXED
    var editingLocation by remember { mutableStateOf<GeofenceLocation?>(null) }

    if (editingLocation != null) {
        EditNameDialog(
            location = editingLocation!!,
            onDismiss = { editingLocation = null },
            onSave = { newName ->
                viewModel.updateGeofenceName(editingLocation!!.id, newName)
                editingLocation = null
            }
        )
    }

    // Ensure Box wraps everything to allow proper positioning of elements
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp, top = 35.dp) // Increased bottom padding to make room for the button
                .background(Color(0xFFD6DCF4)), // Applying the extremely light pink color
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(8.dp)) // Subtle shadow
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center // Center both text & icon
            ) {
                Text(
                    text = "Saved DND",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF1565C0) // A refined shade of blue
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(8.dp)) // Space between text and icon

                Icon(
                    imageVector = Icons.Default.Place, // Built-in location icon
                    contentDescription = "Location Icon",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF1565C0) // Match text color
                )
            }

            LazyColumn {
                items(locations) { location ->
                    LocationListItem(
                        location = location,
                        onEdit = { editingLocation = it },
                        onDelete = { viewModel.removeGeofence(location.id,locationUtils) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD6DCF4)) // Match screen background
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 5.dp
                )
        ) {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.LocationSelectionScreen) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = "Add Location") },
                modifier = Modifier
                    .align(Alignment.Center) // Ensures button is centrally aligned
                    .wrapContentWidth(Alignment.CenterHorizontally),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                containerColor = Color(0xFF1976D2), // Use a deeper blue for contrast
                contentColor = Color.White // White text & icon for visibility
            )
        }

    }
}

@Composable
fun LocationListItem(
    location: GeofenceLocation,
    onEdit: (GeofenceLocation) -> Unit,
    onDelete: (GeofenceLocation) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth() // Ensures the border covers the full screen width
            .padding(horizontal = 6.dp, vertical = 4.dp) // Adds margin between items
            .border(
                border = BorderStroke(3.dp, Color(0XFF018786)),
                shape = RoundedCornerShape(20)
            )
            .padding(6.dp) // Adds internal padding so content doesn’t touch border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp), // Ensures full width inside Box
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = " ${location.name}",
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 4.dp), // Adds left padding to text
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            )

            Row(modifier = Modifier.padding(4.dp)) {
                IconButton(onClick = { onEdit(location) }) {
                    Icon(imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF2E7D32))
                }
                IconButton(onClick = { onDelete(location) }) {
                    Icon(imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336))

                }
            }
        }
    }

}


@Composable
fun EditNameDialog(
    location: GeofenceLocation,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newName by remember { mutableStateOf(location.name) }
    val isSaveEnabled = newName.isNotBlank() && newName != location.name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Location Name") },
        text = {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Location Name") },
                singleLine = true,
                maxLines = 1
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Padding from edges
                horizontalArrangement = Arrangement.SpaceBetween // Spacing between buttons
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp)) // Space between buttons

                TextButton(
                    onClick = { onSave(newName.trim()) },
                    enabled = isSaveEnabled
                ) {
                    Text("Save")
                }
            }

}
    )
}

