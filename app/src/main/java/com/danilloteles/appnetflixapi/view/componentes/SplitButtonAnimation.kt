package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SplitButtonShapes
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplitButtonAnimation(
    onClick: () -> Unit
){
    var isTrailingChecked by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isTrailingChecked) 180f else 0f,
        label = "Icon Rotation"
    )

    // 1. Definir as formas e a borda usando as APIs corretas
    val leadingShape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
    val trailingShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
    // A forma do botão secundário quando está "checado" (animado)
    val trailingCheckedShape = RoundedCornerShape(20.dp)
    val border = BorderStroke(width = 2.dp, color = WHITE)

    // 2. Criar os objetos de 'shapes' para os botões
    val leadingButtonShapes = SplitButtonShapes(
        shape = leadingShape,
        pressedShape = leadingShape, // Mantém a mesma forma ao pressionar
        checkedShape = null // Botão principal não tem estado "checado"
    )
    val trailingButtonShapes = SplitButtonShapes(
        shape = trailingShape,
        pressedShape = trailingShape,
        checkedShape = trailingCheckedShape // Forma para o estado animado
    )

    SplitButtonLayout(
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TRANSPARENT,
                    contentColor = WHITE
                ),
                // 3. Aplicar a forma e a borda através dos parâmetros corretos
                shapes = leadingButtonShapes,
                border = border
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Botão de editar"
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = "Editar")
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                checked = isTrailingChecked,
                onCheckedChange = { isTrailingChecked = it },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TRANSPARENT,
                    contentColor = WHITE
                ),
                // 4. Aplicar aqui também para consistência
                shapes = trailingButtonShapes,
                border = border
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Ativar animação",
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }
    )
}

@Preview
@Composable
fun SplitButtonAnimationPreview() {
    Box(
        modifier = Modifier
            .size(width = 300.dp, height = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        SplitButtonAnimation(
            onClick = {}
        )
    }
}