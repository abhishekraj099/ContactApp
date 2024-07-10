package com.example.newcontactapp.presentation

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.newcontactapp.data.database.Contact
import com.example.newcontactapp.navigation.Routes
import com.example.newcontactapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllContactScreen(
    viewModel: ContactViewModel,
    state: ContactState,
    navController: NavController
) {
    val context = LocalContext.current
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.onSearchQueryChange(it)
                            },
                            placeholder = {
                                Text(
                                    "Search contacts",
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            textStyle = TextStyle(color = Color.White),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("Contacts",
                            color = surfaceColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) {
                            searchQuery = ""
                            viewModel.onSearchQueryChange("")
                        }
                    }) {
                        Icon(
                            if (isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = surfaceColor
                        )
                    }
                    IconButton(onClick = { viewModel.changeSorting() }) {
                        Icon(
                            Icons.Rounded.SortByAlpha,
                            contentDescription = "Sort",
                            tint = surfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = surfaceColor
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.AddEditScreen.route) },
                containerColor = secondaryColor,
                modifier = Modifier.shadow(8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Contact", tint = surfaceColor)
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(state.contacts) { contact ->
                ContactCard(
                    contact = contact,
                    onEditClick = {
                        state.id.value = contact.id
                        state.number.value = contact.number
                        state.email.value = contact.email
                        state.name.value = contact.name
                        state.dateOfCreation.value = contact.dateOfCreation
                        navController.navigate(Routes.AddEditScreen.route)
                    },
                    onDeleteClick = {
                        state.id.value = contact.id
                        state.number.value = contact.number
                        state.email.value = contact.email
                        state.name.value = contact.name
                        state.dateOfCreation.value = contact.dateOfCreation
                        viewModel.deleteContact()
                    },
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${contact.number}")
                        }
                        ContextCompat.startActivity(context, intent, null)
                    }
                )
                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: Contact,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCallClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val dragState = rememberDraggableState { delta ->
        offsetX += delta
    }

    val density = LocalDensity.current
    val cardWidthPx = with(density) { 400.dp.toPx() }
    val swipeThreshold = cardWidthPx * 0.4f

    val swipeProgress = (animatedOffsetX / swipeThreshold).coerceIn(-1f, 1f)
    val scale by animateFloatAsState(targetValue = 1f - (swipeProgress.absoluteValue * 0.05f))
    val phoneIconScale by animateFloatAsState(targetValue = swipeProgress.absoluteValue)

    var isCallInitiated by remember { mutableStateOf(false) }

    LaunchedEffect(isCallInitiated) {
        if (isCallInitiated) {
            delay(300)
            onCallClick()
            isCallInitiated = false
            offsetX = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(primaryColor),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Rounded.Phone,
                contentDescription = "Call",
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .scale(phoneIconScale)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX.absoluteValue >= swipeThreshold) {
                            offsetX = if (offsetX > 0) cardWidthPx else -cardWidthPx
                            isCallInitiated = true
                        } else {
                            offsetX = 0f
                        }
                    }
                )
                .scale(scale)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactImage(contact.image, contact.name)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 8.dp)
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textPrimaryColor
                    )
                    Text(
                        text = contact.number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor
                    )
                    Text(
                        text = contact.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondaryColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onCallClick) {
                        Icon(Icons.Rounded.Phone, contentDescription = "Call", tint = iconPrimaryColor)
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = iconSecondaryColor)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = iconDeleteColor)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactImage(image: ByteArray?, name: String) {
    val bitmap = image?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    val defaultColor = name.first().uppercaseChar().hashCode() and 0x00FFFFFF or 0xFF000000.toInt()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color(defaultColor))
            .shadow(4.dp, CircleShape)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Contact Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.first().toString(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}