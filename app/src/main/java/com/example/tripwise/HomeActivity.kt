package com.example.tripwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.ui.theme.TripWiseTheme
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripWiseTheme {
                HomeScreen()
            }
        }
    }
}

/* ---------- Data Models & Sample Data ---------- */

enum class Category { Attractions, Restaurants, Hotels }

data class Place(
    val id: String,
    val name: String,
    val category: Category,
    val distanceMeters: Int,
    val rating: Double,
    val address: String,
    val isFavorite: Boolean = false
)

private fun samplePlaces(): List<Place> = listOf(
    Place("1", "Riverside Museum", Category.Attractions, 450, 4.6, "12 River St, City"),
    Place("2", "Skyline Viewpoint", Category.Attractions, 900, 4.8, "Hilltop Rd, City"),
    Place("3", "Blue Harbor Hotel", Category.Hotels, 1200, 4.3, "45 Ocean Ave, City"),
    Place("4", "Bella Italia", Category.Restaurants, 300, 4.5, "22 Market Ln, City"),
    Place("5", "City Art Gallery", Category.Attractions, 1500, 4.7, "Museum Sq, City"),
    Place("6", "Maple Inn", Category.Hotels, 850, 4.1, "78 Park Rd, City"),
)

/* ---------- UI ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var favorites by remember { mutableStateOf(setOf<String>()) }
    var places by remember { mutableStateOf(samplePlaces()) }

    // Derived filtered list
    val filtered = remember(query, selectedCategory, favorites, places) {
        places
            .filter { p ->
                (selectedCategory == null || p.category == selectedCategory) &&
                        (query.isBlank() || p.name.contains(query, ignoreCase = true))
            }
            .map { p -> if (p.id in favorites) p.copy(isFavorite = true) else p.copy(isFavorite = false) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TripWise", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* open drawer if added */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* profile */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        "Find nearby gems",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    SearchBar(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Search attractions, restaurants, hotels…",
                        onSearch = { /* optional: trigger search */ }
                    )
                    Spacer(Modifier.height(12.dp))
                    CategoryChips(
                        selected = selectedCategory,
                        onSelect = { selectedCategory = if (selectedCategory == it) null else it }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            MapCTACard(
                onOpenMap = { /* navigate to Map screen */ }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Nearby",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            androidx.compose.foundation.lazy.LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered.size) { idx ->
                    val place = filtered[idx]
                    PlaceCard(
                        place = place,
                        onToggleFavorite = {
                            scope.launch {
                                favorites = if (place.id in favorites)
                                    favorites - place.id
                                else favorites + place.id
                            }
                        },
                        onDetails = { /* navigate to detail */ }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search…",
    onSearch: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}

@Composable
private fun CategoryChips(
    selected: Category?,
    onSelect: (Category) -> Unit
) {
    val categories = listOf(Category.Attractions, Category.Restaurants, Category.Hotels)
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp)
    ) {
        Spacer(Modifier.width(12.dp))
        categories.forEach { cat ->
            val isSelected = selected == cat
            AssistChip(
                onClick = { onSelect(cat) },
                label = { Text(cat.name) },
                leadingIcon = {
                    when (cat) {
                        Category.Attractions -> Icon(Icons.Default.Place, contentDescription = null)
                        Category.Restaurants -> Icon(Icons.Default.Star, contentDescription = null)
                        Category.Hotels -> Icon(Icons.Default.LocationOn, contentDescription = null)
                    }
                },
                modifier = Modifier.padding(horizontal = 6.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    labelColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(Modifier.width(12.dp))
    }
}

@Composable
private fun MapCTACard(onOpenMap: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Map, contentDescription = null)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("View on Map", fontWeight = FontWeight.SemiBold)
                Text(
                    "See all nearby spots and directions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onOpenMap, shape = RoundedCornerShape(12.dp)) {
                Text("Open")
            }
        }
    }
}

@Composable
private fun PlaceCard(
    place: Place,
    onToggleFavorite: () -> Unit,
    onDetails: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onDetails() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Leading icon/avatar placeholder
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (place.category) {
                        Category.Attractions -> Icons.Default.Place
                        Category.Restaurants -> Icons.Default.Star
                        Category.Hotels -> Icons.Default.LocationOn
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${place.rating} • ${(place.distanceMeters / 1000.0).let { "%.1f".format(it) }} km away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorite) {
                if (place.isFavorite) {
                    Icon(Icons.Default.Favorite, contentDescription = "Unfavorite", tint = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                }
            }
        }
    }
}

/* ---------- Preview ---------- */

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    TripWiseTheme { HomeScreen() }
}
