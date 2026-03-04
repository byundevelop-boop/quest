package com.kurly.android.quest.feature.main.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kurly.android.quest.core.model.SectionType
import com.kurly.android.quest.feature.main.R
import com.kurly.android.quest.feature.main.model.MainError
import com.kurly.android.quest.feature.main.model.ProductUiModel
import com.kurly.android.quest.feature.main.model.SectionUiModel
import com.kurly.android.quest.feature.main.presentation.MainViewModel
import com.kurly.android.quest.feature.main.ui.formatter.PriceFormatter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val blockingError = uiState.blockingError
    val listState = rememberLazyListState()

    val shouldLoadMore by remember(uiState, listState) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalCount = listState.layoutInfo.totalItemsCount
            totalCount > 0 &&
                lastVisibleIndex >= totalCount - 2 &&
                uiState.nextPage != null &&
                !uiState.isInitialLoading &&
                !uiState.isRefreshing &&
                !uiState.isLoadingNextPage
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPage()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
            when {
            uiState.isInitialLoading && uiState.sections.isEmpty() -> {
                MainSkeletonScreen(modifier = Modifier.align(Alignment.TopStart))
            }

            blockingError != null && uiState.sections.isEmpty() -> {
                ErrorState(
                    message = stringResource(id = blockingError.toMessageRes()),
                    onRetry = viewModel::retry,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    uiState.inlineError?.let { inlineError ->
                        item(key = "inline-error") {
                            InlineErrorBanner(
                                message = stringResource(id = inlineError.toMessageRes()),
                                onRetry = viewModel::retryInlineError,
                                onDismiss = viewModel::clearInlineError
                            )
                        }
                    }

                    itemsIndexed(
                        items = uiState.sections,
                        key = { _, section -> section.id }
                    ) { _, section ->
                        SectionContent(
                            section = section,
                            onFavoriteClick = viewModel::toggleFavorite
                        )
                    }

                    if (uiState.isLoadingNextPage) {
                        item(key = "next-page-loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.main_retry))
        }
    }
}

@Composable
private fun MainSkeletonScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(SKELETON_SECTION_COUNT) { index ->
            SkeletonSection(
                index = index,
                shimmerProgress = shimmerProgress
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = Color(0xFFE6E6E6),
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun SkeletonSection(
    index: Int,
    shimmerProgress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SkeletonTextLine(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .height(18.dp)
                .fillMaxWidth(0.45f),
            shimmerProgress = shimmerProgress
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (index % 3) {
            0 -> SkeletonHorizontalSection(shimmerProgress)
            1 -> SkeletonVerticalSection(shimmerProgress)
            else -> SkeletonGridSection(shimmerProgress)
        }
    }
}

@Composable
private fun SkeletonHorizontalSection(shimmerProgress: Float) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(3) {
            Column(
                modifier = Modifier.width(150.dp)
            ) {
                SkeletonImage(
                    modifier = Modifier
                        .width(150.dp)
                        .height(200.dp),
                    shimmerProgress = shimmerProgress
                )
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonTextLine(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(18.dp),
                    shimmerProgress = shimmerProgress
                )
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonTextLine(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(14.dp),
                    shimmerProgress = shimmerProgress
                )
            }
        }
    }
}

@Composable
private fun SkeletonVerticalSection(shimmerProgress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(2) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    SkeletonImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(6f / 4f),
                        shimmerProgress = shimmerProgress
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonTextLine(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(18.dp),
                        shimmerProgress = shimmerProgress
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonTextLine(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp),
                        shimmerProgress = shimmerProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonGridSection(shimmerProgress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    SkeletonImage(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp),
                        shimmerProgress = shimmerProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonImage(
    modifier: Modifier,
    shimmerProgress: Float
) {
    SkeletonShimmer(
        modifier = modifier,
        shimmerProgress = shimmerProgress
    )
}

@Composable
private fun SkeletonTextLine(
    modifier: Modifier,
    shimmerProgress: Float
) {
    SkeletonShimmer(
        modifier = modifier,
        shimmerProgress = shimmerProgress
    )
}

@Composable
private fun SkeletonShimmer(
    modifier: Modifier,
    shimmerProgress: Float
) {
    Spacer(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .shimmerBrush(shimmerProgress)
    )
}

private fun Modifier.shimmerBrush(progress: Float): Modifier = this.drawWithCache {
    val startX = -size.width + (size.width * 2f * progress)
    val shimmerWidth = size.width * 0.45f
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE5E5E5),
            Color(0xFFF5F5F5),
            Color(0xFFE5E5E5)
        ),
        start = Offset(startX, 0f),
        end = Offset(startX + shimmerWidth, 0f)
    )

    onDrawWithContent {
        drawContent()
        drawRect(brush = brush)
    }
}

@Composable
private fun InlineErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF222222)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRetry) {
                    Text(text = stringResource(id = R.string.main_retry))
                }
                Button(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.main_dismiss))
                }
            }
        }
    }
}

@Composable
private fun SectionContent(
    section: SectionUiModel,
    onFavoriteClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (section.type) {
            SectionType.HORIZONTAL -> HorizontalSection(section, onFavoriteClick)
            SectionType.GRID -> GridSection(section, onFavoriteClick)
            SectionType.VERTICAL -> VerticalSection(section, onFavoriteClick)
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            color = Color(0xFFE6E6E6),
            thickness = 1.dp
        )
    }
}

@Composable
private fun HorizontalSection(
    section: SectionUiModel,
    onFavoriteClick: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = section.products,
            key = { index, item -> "h-${section.id}-${item.id}-$index" }
        ) { _, product ->
            CompactProductCard(
                product = product,
                isGrid = false,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
private fun GridSection(
    section: SectionUiModel,
    onFavoriteClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        section.products
            .take(GRID_MAX_ITEM_COUNT)
            .chunked(GRID_COLUMN_COUNT)
            .forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            CompactProductCard(
                                product = item,
                                isGrid = true,
                                onFavoriteClick = onFavoriteClick
                            )
                        }
                    }

                    repeat(GRID_COLUMN_COUNT - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
    }
}

@Composable
private fun VerticalSection(
    section: SectionUiModel,
    onFavoriteClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        section.products.forEachIndexed { index, product ->
            key("v-${section.id}-${product.id}-$index") {
                VerticalProductCard(
                    product = product,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
    }
}

@Composable
private fun CompactProductCard(
    product: ProductUiModel,
    isGrid: Boolean,
    onFavoriteClick: (Long) -> Unit
) {
    Column(
        modifier = (if (isGrid) Modifier.fillMaxWidth() else Modifier.width(150.dp))
            .alpha(if (product.isSoldOut) 0.5f else 1f)
    ) {
        Box(
            modifier = if (isGrid) {
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            } else {
                Modifier
                    .width(150.dp)
                    .height(200.dp)
            }
        ) {
            AsyncImage(
                model = product.image,
                contentDescription = stringResource(id = R.string.main_product_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            FavoriteButton(
                isFavorite = product.isFavorite,
                onClick = { onFavoriteClick(product.id) },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        Text(
            text = product.name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
            color = Color(0xFF222222),
            modifier = Modifier.padding(top = 8.dp)
        )

        if (product.hasDiscount) {
            val discounted = product.discountedPrice ?: product.originalPrice
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = PriceFormatter.discountRate(product.discountRatePercent),
                    color = Color(0xFFFA622F),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isGrid) 20.sp else 24.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = PriceFormatter.price(discounted),
                    color = Color(0xFF222222),
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isGrid) 20.sp else 24.sp
                )
            }

            Text(
                text = PriceFormatter.price(product.originalPrice),
                color = Color(0xFF888888),
                style = TextStyle(textDecoration = TextDecoration.LineThrough),
                fontSize = if (isGrid) 16.sp else 20.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        } else {
            Text(
                text = PriceFormatter.price(product.originalPrice),
                color = Color(0xFF222222),
                fontWeight = FontWeight.Bold,
                fontSize = if (isGrid) 20.sp else 24.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun VerticalProductCard(
    product: ProductUiModel,
    onFavoriteClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .alpha(if (product.isSoldOut) 0.5f else 1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = product.image,
                    contentDescription = stringResource(id = R.string.main_product_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(6f / 4f)
                )

                FavoriteButton(
                    isFavorite = product.isFavorite,
                    onClick = { onFavoriteClick(product.id) },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Text(
                text = product.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp,
                color = Color(0xFF222222),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (product.hasDiscount) {
                    val discounted = product.discountedPrice ?: product.originalPrice
                    Text(
                        text = PriceFormatter.discountRate(product.discountRatePercent),
                        color = Color(0xFFFA622F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = PriceFormatter.price(discounted),
                        color = Color(0xFF222222),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = PriceFormatter.price(product.originalPrice),
                        color = Color(0xFF888888),
                        style = TextStyle(textDecoration = TextDecoration.LineThrough),
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = PriceFormatter.price(product.originalPrice),
                        color = Color(0xFF222222),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = if (isFavorite) R.drawable.ic_btn_heart_on else R.drawable.ic_btn_heart_off
    val contentDesc = if (isFavorite) {
        stringResource(id = R.string.main_favorite_on)
    } else {
        stringResource(id = R.string.main_favorite_off)
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.size(FAVORITE_TOUCH_TARGET_SIZE)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDesc,
            modifier = Modifier.size(FAVORITE_ICON_SIZE)
        )
    }
}

private const val GRID_COLUMN_COUNT = 3
private const val GRID_ROW_COUNT = 2
private const val GRID_MAX_ITEM_COUNT = GRID_COLUMN_COUNT * GRID_ROW_COUNT
private const val SKELETON_SECTION_COUNT = 3
private val FAVORITE_TOUCH_TARGET_SIZE = 48.dp
private val FAVORITE_ICON_SIZE = 24.dp

private fun MainError.toMessageRes(): Int {
    return when (this) {
        MainError.LOAD_FAILED -> R.string.main_error_default
        MainError.FAVORITE_SAVE_FAILED -> R.string.main_error_favorite_save
    }
}
