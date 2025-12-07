package com.example.muro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// --- YOUR CUSTOM PALETTE COLORS ---
val MuroBlack = Color(0xFF110E15)       // Main Background
val MuroSurface = Color(0xFF211720)     // Card/Bar Background
val MuroAccentRed = Color(0xFF430E18)   // Dark Red Accent
val MuroAccentPurple = Color(0xFF411D2B)// Lighter Purple Accent
val MuroText = Color(0xFFE0E0E0)        // White/Light Grey (for readability on dark)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuroApp()
        }
    }
}

@Composable
fun MuroApp() {
    // Applying the dark theme colors
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = MuroBlack,
            surface = MuroSurface,
            primary = MuroAccentRed,
            onBackground = MuroText,
            onSurface = MuroText
        )
    ) {
        val navController = rememberNavController()

        Scaffold(
            // Bottom Bar for navigation
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = MuroBlack
        ) { innerPadding ->
            // NavHost handles switching 3 main tabs
            NavHost(
                navController = navController,
                startDestination = "feed",
                modifier = Modifier.padding(innerPadding).background(MuroBlack)
            ) {
                composable("feed") { FeedScreen() }
                composable("desc") { DescriptionScreen() }
                composable("profile") { ProfileScreen() }
            }
        }
    }
}

// --- TAB 1: FEED TAB ---
@Composable
fun FeedScreen() {
    // List of random text posts
    val posts = listOf(
        "Welcome to Muro Dark Mode.",
        "System Status: Online.",
        "Palette applied: #110E15 / #430E18",
        "Android Dev Team Recruitment Task.",
        "Jetpack Compose makes UI modern.",
        "Design is not just what it looks like.",
        "Keep the code clean."
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Muro Feed",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MuroText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(posts) { post ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MuroSurface),
                    // Adding a border with your accent color for contrast
                    modifier = Modifier.fillMaxWidth().border(1.dp, MuroAccentRed, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = post,
                        modifier = Modifier.padding(20.dp),
                        color = MuroText
                    )
                }
            }
        }
    }
}

// --- TAB 2: DESCRIPTION TAB (API) ---
@Composable
fun DescriptionScreen() {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("Initializing stream...") }
    val scope = rememberCoroutineScope()

    // Fetch image from public API
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                // Using Dog API as the source
                val jsonString = URL("https://dog.ceo/api/breeds/image/random").readText()
                val jsonObject = JSONObject(jsonString)
                val url = jsonObject.getString("message")

                // Extracting a "Description" (Breed name) from the API data
                val breed = url.split("/breeds/")[1].split("/")[0].replace("-", " ").capitalize()

                withContext(Dispatchers.Main) {
                    imageUrl = url
                    description = "Detected: $breed"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    description = "Connection Failed. Check Internet."
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Visuals", fontSize = 24.sp, color = MuroAccentPurple, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        // Card with gradient border using your palette
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .size(300.dp)
                .border(2.dp, Brush.horizontalGradient(listOf(MuroAccentRed, MuroAccentPurple)), RoundedCornerShape(16.dp))
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "API Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MuroAccentRed) // Loading indicator
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = description, fontSize = 20.sp, color = MuroText)
    }
}

// --- TAB 3: PROFILE TAB ---
@Composable
fun ProfileScreen() {
    // Editable user info
    var name by remember { mutableStateOf("") }
    var regNo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Identity", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MuroAccentRed)
        Spacer(modifier = Modifier.height(30.dp))

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", color = MuroAccentPurple) },
            textStyle = LocalTextStyle.current.copy(color = MuroText),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MuroAccentRed,
                unfocusedBorderColor = MuroSurface,
                focusedLabelColor = MuroAccentRed,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MuroAccentRed
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Registration ID Field
        OutlinedTextField(
            value = regNo,
            onValueChange = { regNo = it },
            label = { Text("Registration ID", color = MuroAccentPurple) },
            textStyle = LocalTextStyle.current.copy(color = MuroText),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MuroAccentRed,
                unfocusedBorderColor = MuroSurface,
                focusedLabelColor = MuroAccentRed,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MuroAccentRed
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preview Card (Visual feedback)
        if (name.isNotEmpty() || regNo.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MuroSurface),
                modifier = Modifier.fillMaxWidth().border(1.dp, MuroAccentRed, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PROFILE PREVIEW", color = MuroAccentRed, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name: $name", color = MuroText)
                    Text("ID: $regNo", color = MuroText)
                }
            }
        }
    }
}

// --- BOTTOM NAVIGATION COMPONENT ---
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Feed", "feed", Icons.Default.Home),
        BottomNavItem("Explore", "desc", Icons.Default.Image),
        BottomNavItem("Profile", "profile", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = MuroBlack) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MuroText,
                    selectedTextColor = MuroAccentRed,
                    indicatorColor = MuroAccentRed, // Highlight color for active tab
                    unselectedIconColor = MuroSurface,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)