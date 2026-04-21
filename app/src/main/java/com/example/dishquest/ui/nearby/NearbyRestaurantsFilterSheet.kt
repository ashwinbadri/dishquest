package com.example.dishquest.ui.nearby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FoodOrange = Color(0xFFFF6B35)
private val DeepOrange = Color(0xFFD84910)
private val OrangeLight = Color(0xFFFFEDE3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: FilterState,
    sheetState: SheetState,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    // Local draft — only committed when Apply is tapped
    var draft by remember(currentFilter) { mutableStateOf(currentFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                if (!draft.isDefault) {
                    OutlinedButton(
                        onClick = { draft = FilterState() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FoodOrange)
                    ) {
                        Text("Reset all", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Search Radius ─────────────────────────────────────────────
            SectionLabel("Search Radius")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterState.RADIUS_OPTIONS.forEach { mi ->
                    FilterChip(
                        selected = draft.radiusMiles == mi,
                        onClick = { draft = draft.copy(radiusMiles = mi) },
                        label = { Text("${mi}mi", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FoodOrange,
                            selectedLabelColor = Color.White,
                            containerColor = OrangeLight,
                            labelColor = DeepOrange
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = draft.radiusMiles == mi,
                            selectedBorderColor = FoodOrange,
                            borderColor = FoodOrange.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Open Now ──────────────────────────────────────────────────
            SectionLabel("Availability")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open now", fontSize = 15.sp, color = Color(0xFF333333))
                Switch(
                    checked = draft.openNow,
                    onCheckedChange = { draft = draft.copy(openNow = it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FoodOrange
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Dish Confirmation ─────────────────────────────────────────
            SectionLabel("Dish Availability")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mentioned in reviews", fontSize = 15.sp, color = Color(0xFF333333))
                    Text(
                        "Only show restaurants where dish\nappears in Google reviews",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = draft.dishConfirmedOnly,
                    onCheckedChange = { draft = draft.copy(dishConfirmedOnly = it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FoodOrange
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Minimum Rating ────────────────────────────────────────────
            SectionLabel("Minimum Rating")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterState.RATING_OPTIONS.forEach { rating ->
                    val label = if (rating == 0f) "Any" else "$rating ★"
                    FilterChip(
                        selected = draft.minRating == rating,
                        onClick = { draft = draft.copy(minRating = rating) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FoodOrange,
                            selectedLabelColor = Color.White,
                            containerColor = OrangeLight,
                            labelColor = DeepOrange
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = draft.minRating == rating,
                            selectedBorderColor = FoodOrange,
                            borderColor = FoodOrange.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Sort By ───────────────────────────────────────────────────
            SectionLabel("Sort By")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortBy.entries.forEach { sort ->
                    FilterChip(
                        selected = draft.sortBy == sort,
                        onClick = { draft = draft.copy(sortBy = sort) },
                        label = { Text(sort.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FoodOrange,
                            selectedLabelColor = Color.White,
                            containerColor = OrangeLight,
                            labelColor = DeepOrange
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = draft.sortBy == sort,
                            selectedBorderColor = FoodOrange,
                            borderColor = FoodOrange.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Apply button ──────────────────────────────────────────────
            Button(
                onClick = { onApply(draft) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FoodOrange)
            ) {
                Text(
                    text = if (draft.activeCount > 0) "Apply ${draft.activeCount} filter${if (draft.activeCount > 1) "s" else ""}"
                           else "Apply",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF888888),
        letterSpacing = 0.5.sp
    )
}
