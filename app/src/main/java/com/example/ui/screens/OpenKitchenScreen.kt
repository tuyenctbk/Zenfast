package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BreakFastCategory
import com.example.data.model.BreakFastRecipe
import com.example.data.model.GlycemicAnalysis
import com.example.ui.components.GlycemicScoreCard
import com.example.ui.components.RecipeCard
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenKitchenScreen(
    recipes: List<BreakFastRecipe>,
    selectedCategory: BreakFastCategory,
    isLoadingRecipes: Boolean,
    barcodeInput: String,
    isAnalyzingBarcode: Boolean,
    glycemicAnalysis: GlycemicAnalysis?,
    barcodeAnalysisError: String?,
    recentScans: List<GlycemicAnalysis>,
    onSelectCategory: (BreakFastCategory) -> Unit,
    onStartCookMode: (BreakFastRecipe) -> Unit,
    onBarcodeInputChanged: (String) -> Unit,
    onAnalyzeBarcode: (String) -> Unit,
    onClearBarcodeAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedKitchenTab by remember { mutableIntStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Kitchen Sub-Tab Row: Recipes vs Scanner
        PrimaryTabRow(
            selectedTabIndex = selectedKitchenTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = EmeraldPrimary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedKitchenTab),
                    color = EmeraldPrimary,
                    width = 48.dp
                )
            }
        ) {
            Tab(
                selected = selectedKitchenTab == 0,
                onClick = { selectedKitchenTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SoupKitchen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.tab_recipes),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedKitchenTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("tab_break_fast_recipes")
            )

            Tab(
                selected = selectedKitchenTab == 1,
                onClick = { selectedKitchenTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.tab_scanner),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedKitchenTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("tab_glycemic_scanner")
            )
        }

        if (selectedKitchenTab == 0) {
            // TAB 1: BREAK-FAST RECIPES (TheMealDB)
            RecipesTabContent(
                recipes = recipes,
                selectedCategory = selectedCategory,
                isLoadingRecipes = isLoadingRecipes,
                onSelectCategory = onSelectCategory,
                onStartCookMode = onStartCookMode
            )
        } else {
            // TAB 2: GLYCEMIC & INSULIN SPIKE BARCODE SCANNER (OpenFoodFacts)
            ScannerTabContent(
                barcodeInput = barcodeInput,
                isAnalyzingBarcode = isAnalyzingBarcode,
                glycemicAnalysis = glycemicAnalysis,
                barcodeAnalysisError = barcodeAnalysisError,
                recentScans = recentScans,
                onBarcodeInputChanged = onBarcodeInputChanged,
                onAnalyzeBarcode = { code ->
                    keyboardController?.hide()
                    onAnalyzeBarcode(code)
                },
                onClearBarcodeAnalysis = onClearBarcodeAnalysis,
                onOpenKitchenRecipes = { selectedKitchenTab = 0 }
            )
        }
    }
}

@Composable
private fun RecipesTabContent(
    recipes: List<BreakFastRecipe>,
    selectedCategory: BreakFastCategory,
    isLoadingRecipes: Boolean,
    onSelectCategory: (BreakFastCategory) -> Unit,
    onStartCookMode: (BreakFastRecipe) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Educational Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = EmeraldPrimary.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.gentle_nutrition_title),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                        Text(
                            text = stringResource(id = R.string.gentle_nutrition_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(BreakFastCategory.entries) { cat ->
                    val isSelected = cat == selectedCategory
                    val categoryLabel = when (cat) {
                        BreakFastCategory.ALL -> stringResource(id = R.string.cat_all)
                        BreakFastCategory.HIGH_PROTEIN -> stringResource(id = R.string.cat_high_protein)
                        BreakFastCategory.GUT_FRIENDLY -> stringResource(id = R.string.cat_gut_friendly)
                        BreakFastCategory.KETO_GENTLE -> stringResource(id = R.string.cat_keto_gentle)
                        BreakFastCategory.LIGHT_STARTERS -> stringResource(id = R.string.cat_light_starters)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(cat) },
                        label = {
                            Text(
                                text = categoryLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("filter_chip_${cat.name}")
                    )
                }
            }
        }

        // Loading or Recipe List
        if (isLoadingRecipes) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            }
        } else if (recipes.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.no_recipes_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(recipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onStartCookMode = onStartCookMode,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ScannerTabContent(
    barcodeInput: String,
    isAnalyzingBarcode: Boolean,
    glycemicAnalysis: GlycemicAnalysis?,
    barcodeAnalysisError: String?,
    recentScans: List<GlycemicAnalysis>,
    onBarcodeInputChanged: (String) -> Unit,
    onAnalyzeBarcode: (String) -> Unit,
    onClearBarcodeAnalysis: () -> Unit,
    onOpenKitchenRecipes: () -> Unit
) {
    var isViewfinderActive by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Active Analysis Card Display
        if (glycemicAnalysis != null) {
            item {
                GlycemicScoreCard(
                    analysis = glycemicAnalysis,
                    onDismiss = onClearBarcodeAnalysis,
                    onOpenKitchenRecipes = onOpenKitchenRecipes,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Barcode Input Box with Search Button
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barcode_scanner_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.scanner_card_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(id = R.string.scanner_card_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = barcodeInput,
                            onValueChange = onBarcodeInputChanged,
                            placeholder = { Text(stringResource(id = R.string.barcode_input_placeholder)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { onAnalyzeBarcode(barcodeInput) }
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = EmeraldPrimary
                                )
                            },
                            trailingIcon = {
                                if (barcodeInput.isNotEmpty()) {
                                    IconButton(onClick = { onBarcodeInputChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(id = R.string.clear_input),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("barcode_text_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onAnalyzeBarcode(barcodeInput) },
                            enabled = !isAnalyzingBarcode,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("analyze_barcode_btn")
                        ) {
                            if (isAnalyzingBarcode) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.surface,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(id = R.string.analyze)
                                )
                            }
                        }
                    }

                    if (!barcodeAnalysisError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = barcodeAnalysisError,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scanner Viewfinder toggle
                    OutlinedButton(
                        onClick = { isViewfinderActive = !isViewfinderActive },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_camera_scanner_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                id = if (isViewfinderActive) R.string.hide_camera_viewfinder else R.string.open_camera_viewfinder
                            )
                        )
                    }

                    AnimatedVisibility(visible = isViewfinderActive) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            // Viewfinder frame
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.88f))
                                    .border(2.dp, EmeraldPrimary, RoundedCornerShape(16.dp))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.align_barcode_viewport),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Scans History Section (Real User Data Only)
        if (recentScans.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.recent_scans_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recentScans, key = { it.barcode }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onAnalyzeBarcode(item.barcode) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.brand} • ${item.barcode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${item.breakFastScore}/100",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(item.riskLevel.colorHex)
                        )
                    }
                }
            }
        }
    }
}
