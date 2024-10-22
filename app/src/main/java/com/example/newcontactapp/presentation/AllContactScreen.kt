package com.example.newcontactapp.presentation

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
                            modifier = Modifier.fillMaxWidth(),
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
                        Text("Contacts", color = surfaceColor, fontWeight = FontWeight.Bold)
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
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.AddEditScreen.route) },
                containerColor = secondaryColor,
                shape = CircleShape,
                modifier = Modifier.shadow(4.dp, CircleShape)
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
                .clip(RoundedCornerShape(16.dp))
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
                .scale(scale),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contact.number,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor
                    )
                    Text(
                        text = contact.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onCallClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Phone, contentDescription = "Call", tint = iconPrimaryColor)
                    }
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(secondaryColor.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = iconSecondaryColor)
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconDeleteColor.copy(alpha = 0.1f), CircleShape)
                    ) {
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
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                color = if (bitmap != null) Color.Transparent else primaryVariant,
                shape = CircleShape
            )
            .border(2.dp, primaryColor, CircleShape)
            .shadow(4.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = name.first().toString(),
                color = surfaceColor,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}