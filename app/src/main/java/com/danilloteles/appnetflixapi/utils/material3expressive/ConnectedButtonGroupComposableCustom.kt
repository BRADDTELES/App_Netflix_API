@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.danilloteles.appnetflixapi.utils.material3expressive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.ui.theme.GRAY
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE

/**
 * Um grupo de botões de filtro customizado para a tela "Minha Lista".
 *
 * @param selectedIndex O índice do botão atualmente selecionado (0 para Minha Lista, 1 para Populares, 2 para Melhor Avaliados, 3 para A-Z).
 * @param onIndexChange Uma função que será chamada com o novo índice sempre que um botão for selecionado.
 * @param modifier O modificador a ser aplicado ao componente.
 */
@Composable
fun ConnectedButtonGroupComposableCustom(
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("", "", "", "")
    val unCheckedIcons =
        listOf(Icons.Outlined.Favorite, Icons.Outlined.ThumbUp,Icons.Outlined.Star, Icons.Default.SortByAlpha)
    val checkedIcons = listOf(Icons.Filled.Favorite, Icons.Filled.ThumbUp,Icons.Filled.Star, Icons.Default.SortByAlpha)

    Row(
        modifier = modifier.padding(start = 30.dp, end = 30.dp, top = 16.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { onIndexChange(index) },
                modifier = Modifier
                    .weight(1f) // Peso igual para todos os botões
                    .semantics { role = Role.RadioButton },
                shapes =
                when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = VERMELHO,
                    checkedContentColor = WHITE,
                    containerColor = WHITE,
                    contentColor = GRAY
                )
            ) {
                Icon(
                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = label,
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
}

@Preview
@Composable
private fun ConnectedButtonGroupComposableCustomPreview() {
    // O Preview precisa de seu próprio estado para ser interativo
    var selectedIndex by remember { mutableIntStateOf(0) }
    ConnectedButtonGroupComposableCustom(
        selectedIndex = selectedIndex,
        onIndexChange = { newIndex -> selectedIndex = newIndex }
    )
}
