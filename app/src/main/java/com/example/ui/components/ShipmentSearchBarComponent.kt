package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class ShipmentSearchScope(val label: String, val shortLabel: String, val icon: ImageVector, val tagId: String) {
    ALL("All (Tracking ID & Client)", "All", Icons.Default.Search, "filter_all_chip"),
    TRACKING_ID("Tracking ID", "Tracking ID", Icons.Default.QrCodeScanner, "filter_tracking_chip"),
    CLIENT_NAME("Client Name", "Client", Icons.Default.Business, "filter_client_chip")
}

/**
 * Search bar component for the top of the shipment list.
 * Allows logistics managers to filter shipments by tracking ID or client name.
 */
@Composable
fun ShipmentSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    searchScope: ShipmentSearchScope,
    onSearchScopeChange: (ShipmentSearchScope) -> Unit,
    totalShipmentsCount: Int,
    filteredShipmentsCount: Int,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shipment_search_bar")
    ) {
        // Main Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shipment_search_input"),
            placeholder = {
                Text(
                    text = when (searchScope) {
                        ShipmentSearchScope.ALL -> "Search by Tracking ID or Client Name..."
                        ShipmentSearchScope.TRACKING_ID -> "Search by Tracking ID (e.g. ECGMIX, EXP-2026)..."
                        ShipmentSearchScope.CLIENT_NAME -> "Search by Client Name (e.g. Outlaw Dip)..."
                    },
                    fontSize = 13.sp,
                    maxLines = 1
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = LightBorder,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Scope Filter Chips Row: All, Tracking ID, Client Name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ShipmentSearchScope.entries.forEach { scope ->
                val isSelected = searchScope == scope
                FilterChip(
                    selected = isSelected,
                    onClick = { onSearchScopeChange(scope) },
                    leadingIcon = {
                        Icon(
                            imageVector = scope.icon,
                            contentDescription = null,
                            tint = if (isSelected) PrimaryBlue else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = {
                        Text(
                            text = scope.shortLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag(scope.tagId)
                )
            }

            // Results count badge when query is present
            if (query.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlueContainer
                ) {
                    Text(
                        text = "$filteredShipmentsCount of $totalShipmentsCount matches",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Friendly empty state for search with specific guidance on tracking ID and client name.
 */
@Composable
fun ShipmentSearchEmptyState(
    query: String,
    scope: ShipmentSearchScope,
    onResetSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = StatusAmberContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = TertiaryAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No shipments found matching '$query'",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (scope) {
                    ShipmentSearchScope.TRACKING_ID ->
                        "No shipments match Tracking ID '$query'. Check the shipment ID (e.g. ECGMIX_092826_016), reference, B/L, or container number."
                    ShipmentSearchScope.CLIENT_NAME ->
                        "No shipments found for client '$query'. Check the client/customer name or switch filter to 'All'."
                    ShipmentSearchScope.ALL ->
                        "Try searching by full or partial Tracking ID (e.g. ECGMIX_092826_016) or Client Name (e.g. OUTLAW DIP COMPANY)."
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onResetSearch,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_reset_shipment_search")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Search Filter", fontSize = 12.sp)
            }
        }
    }
}
