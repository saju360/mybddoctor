package com.lifeplus.healthcare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeplus.healthcare.ui.theme.*

import com.lifeplus.healthcare.util.DataConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterSheet(
    title: String,
    initialDistrict: String? = null,
    onDismiss: () -> Unit,
    onApply: (Map<String, String>) -> Unit
) {
    var district by remember { mutableStateOf(initialDistrict ?: "") }
    var upazila by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Filter",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // District Selection
            Text("District", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            
            val districts = DataConstants.districts
            var expandDistrict by remember { mutableStateOf(false) }
            
            DropdownField(
                label = "Select District",
                selected = district.ifBlank { "All Districts" },
                expanded = expandDistrict,
                onExpandedChange = { expandDistrict = it },
                items = listOf("All Districts") + districts,
                onItemSelected = { 
                    district = if (it == "All Districts") "" else it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Upazila Selection
            Text("Upazila / Area", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = upazila,
                onValueChange = { upazila = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Dhanmondi") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Surface2Light
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conditional Filters based on Feature
            when (title) {
                "Hospitals" -> {
                    FilterToggle("ICU Available", availability) { availability = it }
                    FilterToggle("24/7 Open", false) { }
                }
                "Doctors" -> {
                    FilterToggle("Telemedicine", availability) { availability = it }
                }
                "Donors", "Blood Banks" -> {
                    Text("Blood Group", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    BloodGroupSelector(selectedCategory) { selectedCategory = it }
                }
                "Ambulances" -> {
                    FilterToggle("Available Now", availability) { availability = it }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val filters = mutableMapOf<String, String>()
                    if (district.isNotBlank()) filters["district"] = district
                    if (upazila.isNotBlank()) filters["upazila"] = upazila
                    if (availability) filters["availability"] = "true"
                    if (selectedCategory != "All") filters["category"] = selectedCategory
                    onApply(filters)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Apply Filters", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = {
                    district = ""
                    upazila = ""
                    availability = false
                    selectedCategory = "All"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All", color = TextSecondary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FilterToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BloodGroupSelector(selected: String, onSelect: (String) -> Unit) {
    val groups = listOf("All") + DataConstants.bloodGroups
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            val isSelected = selected == group
            Surface(
                onClick = { onSelect(group) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Primary else Surface2Light,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = group,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}
