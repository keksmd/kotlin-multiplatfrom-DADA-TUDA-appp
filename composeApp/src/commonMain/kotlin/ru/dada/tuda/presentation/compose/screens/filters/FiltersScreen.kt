package ru.dada.tuda.presentation.compose.screens.filters

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dadatuda.composeapp.generated.resources.Res
import dadatuda.composeapp.generated.resources.ic_close
import dadatuda.composeapp.generated.resources.ic_filled_star
import dadatuda.composeapp.generated.resources.ic_outlined_star
import dadatuda.composeapp.generated.resources.ic_revert_icon_thin
import dadatuda.composeapp.generated.resources.ic_theatre
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import ru.dada.tuda.domain.http.models.shortlist.ShortlistViewModel
import ru.dada.tuda.domain.util.formatDateFromMillis
import ru.dada.tuda.presentation.theme.BodyMediumText
import ru.dada.tuda.presentation.theme.CygreFontFamily
import ru.dada.tuda.presentation.theme.HeadlineMediumText
import ru.dada.tuda.presentation.theme.TitleLargeText

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun FiltersScreen(
    shortlistViewModel: ShortlistViewModel = koinInject(),
    onBack: () -> Unit,
    onApply: () -> Unit,
    bottomOverlapPadding: Dp = 0.dp
) {
    val currentCategories by shortlistViewModel.categoriesFlow.collectAsState()
    val currentStart by shortlistViewModel.startDateTimeFlow.collectAsState()
    val currentEnd by shortlistViewModel.endDateTimeFlow.collectAsState()
    val currentMin by shortlistViewModel.minPriceFlow.collectAsState()
    val currentMax by shortlistViewModel.maxPriceFlow.collectAsState()
    val searchState by shortlistViewModel.searchFlow.collectAsState()
    val isStarred by shortlistViewModel.isStarredFlow.collectAsState()

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = currentStart?.toLongOrNull(),
        initialSelectedEndDateMillis = currentEnd?.toLongOrNull()
    )

    // Convert string prices to floats for slider
    val minPrice = currentMin?.toFloatOrNull() ?: 0f
    val maxPrice = currentMax?.toFloatOrNull() ?: 10000f

    // Derived flag for reset button visibility
    val hasFilters by remember {
        derivedStateOf {
            currentCategories.isNotEmpty() || currentStart != null || currentEnd != null || minPrice > 0f || maxPrice < 10000f || searchState.isNotBlank() || isStarred
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Sticky Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(Res.drawable.ic_revert_icon_thin),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            HeadlineMediumText(
                text = "Фильтры", maxLines = 1, color = Color.Black, modifier = Modifier.weight(1f)
            )
            Icon(
                painterResource(Res.drawable.ic_close),
                contentDescription = "Закрыть",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBack() }
                    .padding(4.dp),
                tint = Color.Black)
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchState,
                singleLine = true,
                onValueChange = shortlistViewModel::updateSearch,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                leadingIcon = {
                    Icon(
                        Icons.Default.Search, null, tint = Color(0xFF8F8E94)
                    )
                },
                placeholder = {
                    Text("Поиск", color = Color(0xFF8F8E94))
                }
            )

            // Shortlist title with star button
            TitleLargeText(text = "Избранное", color = Color.Black)

            AnimatedContent(
                isStarred,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "StarFilterAnimation"
            ) {
                Image(
                    painterResource(if (it) Res.drawable.ic_filled_star else Res.drawable.ic_outlined_star),
                    contentDescription = if (it) "Показать все" else "Показать избранные",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            shortlistViewModel.updateStarred()
                        }
                        .padding(4.dp),
                )
            }

            // Categories section with FlowRow
            TitleLargeText(text = "Категории", color = Color.Black)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy((-8).dp),
            ) {
                DefaultCategories.forEach { category ->
                    val selectedColor by animateColorAsState(
                        if (currentCategories.contains(category)) Color.Black else Color(
                            0xFF8F8E94
                        )
                    )
                    FilterChip(
                        selected = currentCategories.contains(category), onClick = {
                            if (currentCategories.contains(category))
                                shortlistViewModel.updateCategories(currentCategories - category)
                            else
                                shortlistViewModel.updateCategories(currentCategories + category)
                        },
                        shape = RoundedCornerShape(35),

                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            selectedContainerColor = Color(0xFFF2FF87),
//                        con = Color(0xFF8F8E94)
                        ),
                        border = BorderStroke(
                            1.dp, Color(0xFF8F8E94)
                        ),
                        label = {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = selectedColor
                            )
                        }, leadingIcon = {
                            Icon(
                                painterResource(Res.drawable.ic_theatre), null, tint = selectedColor
                            )
                        }
                    )
                }
            }

            // Price range slider
            TitleLargeText(
                text = "Цена",
                color = Color.Black
            )
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${minPrice.toInt()} ₽", color = Color.Black
                )
                Text(
                    text = "${maxPrice.toInt()} ₽", color = Color.Black
                )
            }
            RangeSlider(
                value = minPrice..maxPrice,
                onValueChange = { range ->
                    shortlistViewModel.updateMinPrice(
                        if (range.start > 0f) range.start.toInt().toString() else null
                    )
                    shortlistViewModel.updateMaxPrice(
                        if (range.endInclusive < 10000f) range.endInclusive.toInt()
                            .toString() else null
                    )
                },
                valueRange = 0f..10000f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF8F8E94),
                    activeTrackColor = Color(0xFF8F8E94),
                    inactiveTrackColor = Color(0xFF8F8E94).copy(alpha = 0.24f)
                )
            )

            // Date section with DatePickers
            TitleLargeText(text = "Дата", color = Color.Black)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Card(
                    {
                        showDatePicker = true
                    },
                    Modifier.weight(1f),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(Color(0xFFEFEFEF))
                ) {
                    androidx.compose.material3.TextField(
                        prefix = {
                            BodyMediumText(
                                "от",
                                color = Color(0xFF8F8E94)
                            )
                        },
                        value = currentStart?.let { it.toLong().formatDateFromMillis() } ?: "",
                        onValueChange = { },
                        enabled = false,
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            disabledIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = Color.Black
                        )
                    )
                }

                // End date picker
                androidx.compose.material3.Card(
                    {
                        showDatePicker = true
                    },
                    Modifier.weight(1f),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(Color(0xFFEFEFEF))
                ) {
                    androidx.compose.material3.TextField(
                        prefix = {
                            BodyMediumText(
                                "до",
                                color = Color(0xFF8F8E94)
                            )
                        },
                        value = currentEnd?.let { it.toLong().formatDateFromMillis() } ?: "",
                        onValueChange = { },
                        enabled = false,
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            disabledIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = Color.Black
                        )
                    )
                }
            }

            // Add some bottom padding to ensure content isn't hidden behind sticky buttons
            Spacer(modifier = Modifier.padding(bottom = 80.dp))
        }

        // Sticky buttons at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = bottomOverlapPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated reset button
            AnimatedVisibility(
                visible = hasFilters,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.weight(1f)
            ) {
                Button(
                    onClick = {
                        shortlistViewModel.clearFilters()
                    },
                    shape = RoundedCornerShape(32),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF))
                ) {
                    Text("Сбросить", color = Color(0xFF8F8E94))
                }
            }

            // Apply button
            Button(
                onClick = {
                    shortlistViewModel.applyFilters()
                    onApply()
                },
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFF2FF87)),
                shape = RoundedCornerShape(32),
                modifier = Modifier.weight(2f)
            ) {
                Text("Показать мероприятия", color = Color(0xFF191919))
            }
        }
    }

    // Date range picker dialog
    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    showDatePicker = false
                    dateRangePickerState.selectedStartDateMillis?.let {
                        shortlistViewModel.updateStartDateTime(it.toString())
                    }
                    dateRangePickerState.selectedEndDateMillis?.let {
                        shortlistViewModel.updateEndDateTime(it.toString())
                    }
                }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
        }) {
            DateRangePicker(state = dateRangePickerState)
        }
    }
}

val DefaultCategories = listOf(
    "Красота и здоровье",
    "Активный отдых",
    "Образование и развитие",
    "Фотография",
    "Клубы и вечеринки",
    "Другое",
    "Культура и творчество",
    "Развлечения и досуг",
    "Еда и гастрономия"
)