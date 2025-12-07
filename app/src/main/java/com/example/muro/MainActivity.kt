package com.example.muro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.*

// --- THEMES ---
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkAccent = Color(0xFFFFD700)
val DarkText = Color(0xFFF1F5F9)

val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF8FAFC)
val LightAccent = Color(0xFFFF5722)
val LightText = Color(0xFF1E293B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuroUltimateApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuroUltimateApp() {
    // --- APP STATE ---
    var isDarkTheme by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("Prateek") }

    // Initial Facts
    val globalPosts = remember { mutableStateListOf(
        "Did you know? In India, the horn is a communication device! 🎺",
        "Fact: ISRO spent less on the Mars mission than the movie Gravity. 🚀",
        "Funny: We don't need GPS, we have 'Bhaiya, aage se right lena'. 😂",
        "India has a floating post office in Dal Lake, Srinagar!",
        "The game 'Snakes and Ladders' originated in India as Moksha Patam."
    ) }

    // --- ANIMATED COLORS ---
    val bgCol by animateColorAsState(if (isDarkTheme) DarkBackground else LightBackground, tween(500))
    val surfCol by animateColorAsState(if (isDarkTheme) DarkSurface else LightSurface, tween(500))
    val accCol by animateColorAsState(if (isDarkTheme) DarkAccent else LightAccent, tween(500))
    val txtCol by animateColorAsState(if (isDarkTheme) DarkText else LightText, tween(500))

    // --- SPLASH SCREEN LOGIC ---
    LaunchedEffect(Unit) {
        delay(2500)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen(bgCol, txtCol, accCol)
    } else {
        // --- MAIN APP STRUCTURE ---
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme(background = DarkBackground, surface = DarkSurface, primary = DarkAccent)
            else lightColorScheme(background = LightBackground, surface = LightSurface, primary = LightAccent)
        ) {
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(bgCol, surfCol, txtCol, accCol) {
                        scope.launch { drawerState.close() }
                    }
                }
            ) {
                Scaffold(
                    containerColor = bgCol,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("MURO", fontWeight = FontWeight.Black, letterSpacing = 3.sp, color = txtCol) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Menu", tint = txtCol)
                                }
                            },
                            actions = {
                                IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                    Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle", tint = accCol)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bgCol)
                        )
                    },
                    bottomBar = { AppBottomBar(navController, surfCol, accCol) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "feed",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("feed") { FeedScreen(surfCol, txtCol, accCol, globalPosts, userName) }
                        composable("nexus") { NexusScreen(surfCol, txtCol, accCol) }
                        composable("profile") { ProfileScreen(surfCol, txtCol, accCol, globalPosts, userName) { newName -> userName = newName } }
                    }
                }
            }
        }
    }
}

// --- SCREEN 0: SPLASH SCREEN ---
@Composable
fun SplashScreen(bg: Color, text: Color, accent: Color) {
    Box(
        modifier = Modifier.fillMaxSize().background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AutoStories, contentDescription = null, tint = accent, modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(20.dp))
            Text("MURO", fontSize = 40.sp, fontWeight = FontWeight.Black, color = text, letterSpacing = 8.sp)
            Text("The Fact Check App", fontSize = 18.sp, color = text.copy(alpha = 0.7f), fontStyle = FontStyle.Italic)
            Spacer(modifier = Modifier.height(100.dp))
            CircularProgressIndicator(color = accent)
        }
    }
}

// --- SCREEN 1: FEED ---
@Composable
fun FeedScreen(surface: Color, text: Color, accent: Color, posts: List<String>, userName: String) {
    var weatherInfo by remember { mutableStateOf("Loading Weather...") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val json = URL("https://api.open-meteo.com/v1/forecast?latitude=23.25&longitude=77.41&current_weather=true").readText()
                val current = JSONObject(json).getJSONObject("current_weather")
                weatherInfo = "${current.getString("temperature")}°C | ${current.getString("windspeed")} km/h"
            } catch (e: Exception) { weatherInfo = "Weather Unavailable" }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when (hour) { in 0..11 -> "Good Morning"; in 12..16 -> "Good Afternoon"; else -> "Good Evening" }
            Column {
                Text("$greeting, $userName!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = text)
                Text("Here is your daily dose of facts.", fontSize = 16.sp, color = Color.Gray)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surface),
                modifier = Modifier.fillMaxWidth().border(1.dp, accent.copy(alpha=0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("VIT BHOPAL CAMPUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(weatherInfo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = text)
                    }
                    Icon(Icons.Default.WbSunny, null, tint = accent, modifier = Modifier.size(40.dp))
                }
            }
        }
        item { Text("TRENDING FACTS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accent) }
        items(posts.reversed()) { post ->
            var liked by remember { mutableStateOf(false) }
            Card(colors = CardDefaults.cardColors(containerColor = surface), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(post, color = text, fontSize = 16.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { liked = !liked }) {
                            Icon(if(liked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if(liked) Color.Red else Color.Gray)
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))
                Text("Crafted by Prateek Das", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = text)
                Text("ID: 25BCE10599", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Code & Soul: Kotlin", fontSize = 10.sp, color = accent)
            }
        }
    }
}

// --- DRAWER (SIDE MENU - UPDATED) ---
@Composable
fun DrawerContent(bg: Color, surface: Color, text: Color, accent: Color, onClose: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    ModalDrawerSheet(drawerContainerColor = bg) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("MURO v15.5.1", fontSize = 24.sp, fontWeight = FontWeight.Black, color = accent)
            Spacer(modifier = Modifier.height(30.dp))

            // Menu Items
            val menuItems = listOf("App FAQs", "Theme Settings")
            menuItems.forEach { item ->
                TextButton(onClick = { onClose() }) {
                    Text(item, fontSize = 18.sp, color = text)
                }
                Divider(color = Color.Gray.copy(0.1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language Selector
            Text("Language / भाषा", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { Toast.makeText(context, "English Selected", Toast.LENGTH_SHORT).show() }) {
                    Text("English", color = text)
                }
                OutlinedButton(onClick = { Toast.makeText(context, "Hindi Selected (Beta)", Toast.LENGTH_SHORT).show() }) {
                    Text("Hindi", color = text)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- CONNECT WITH DEVELOPER ---
            Card(colors = CardDefaults.cardColors(containerColor = surface), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CONNECT WITH DEVELOPER", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accent)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Prateek Das", color = text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Full Stack Android Developer", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Social Links
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = { uriHandler.openUri("https://www.linkedin.com/in/prateek-das-a45215252/") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B5)), // LinkedIn Blue
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LinkedIn", color = Color.White)
                        }

                        Button(
                            onClick = { uriHandler.openUri("https://github.com/dev-pd-1525/") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292e)), // GitHub Black
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GitHub", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: NEXUS ---
@Composable
fun NexusScreen(surface: Color, text: Color, accent: Color) {
    var fact by remember { mutableStateOf("Connecting...") }
    val scope = rememberCoroutineScope()
    fun fetch() { scope.launch(Dispatchers.IO) { try { fact = JSONObject(URL("https://uselessfacts.jsph.pl/random.json?language=en").readText()).getString("text") } catch (e: Exception) { fact = "Offline" } } }
    LaunchedEffect(Unit) { fetch() }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Lightbulb, null, tint = accent, modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = surface), modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, accent, RoundedCornerShape(12.dp))) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) { Text(fact, color = text, fontSize = 18.sp, textAlign = TextAlign.Center) }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = { fetch() }, colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("NEW FACT", color = Color.White) }
    }
}

// --- TAB 3: PROFILE ---
@Composable
fun ProfileScreen(surface: Color, text: Color, accent: Color, posts: MutableList<String>, name: String, onName: (String) -> Unit) {
    var loggedIn by remember { mutableStateOf(false) }
    var txt by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!loggedIn) {
            Spacer(modifier = Modifier.height(40.dp))
            Text("MURO LOGIN", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = onName, label = { Text("Your Name") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = text))
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { loggedIn = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("ENTER", color = Color.White) }
        } else {
            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = text)
            Text("Verified Fact Checker", fontSize = 14.sp, color = Color.Gray)
            Divider(Modifier.padding(vertical = 20.dp))
            OutlinedTextField(value = txt, onValueChange = { txt = it }, label = { Text("Type a fact...") }, modifier = Modifier.fillMaxWidth().height(100.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = text))
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { if(txt.isNotEmpty()) { posts.add("User: $txt"); txt="" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("PUBLISH", color = Color.White) }
        }
    }
}

// --- BOTTOM BAR ---
@Composable
fun AppBottomBar(navController: NavController, surface: Color, accent: Color) {
    val items = listOf(Triple("Feed", "feed", Icons.Default.Home), Triple("Nexus", "nexus", Icons.Default.Explore), Triple("Profile", "profile", Icons.Default.Person))
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar(containerColor = surface) {
        items.forEach { (l, r, i) -> NavigationBarItem(icon = { Icon(i, l) }, label = { Text(l) }, selected = currentRoute == r, onClick = { navController.navigate(r) }, colors = NavigationBarItemDefaults.colors(indicatorColor = accent)) }
    }
}