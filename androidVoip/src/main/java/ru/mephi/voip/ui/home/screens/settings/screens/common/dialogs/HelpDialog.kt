package ru.mephi.voip.ui.home.screens.settings.screens.common.dialogs

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import ru.mephi.voip.utils.launchDialer
import ru.mephi.voip.utils.launchMailClientIntent

@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss() },
        icon = { Icon(Icons.Outlined.Group, contentDescription = null) },
        title = {
            Text(text = "Получение доступа")
        },
        text = {
            val annotatedString = buildAnnotatedString {
                withStyle(style = MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                    append("Для получения доступа к ip-телефонии НИЯУ МИФИ вам необходимо оставить заявку\n")
                    append("Почта: ")

                    pushStringAnnotation(tag = "email", annotation = "voip@mephi.ru")
                    withStyle(
                        style = MaterialTheme.typography.bodyLarge.toSpanStyle()
                            .copy(color = MaterialTheme.colorScheme.secondary)
                    ) {
                        append("voip@mephi.ru")
                    }
                    pop()

                    append("\nТелефон: ")

                    pushStringAnnotation(tag = "phone", annotation = "+74957885699")
                    withStyle(
                        style = MaterialTheme.typography.bodyLarge.toSpanStyle()
                            .copy(color = MaterialTheme.colorScheme.secondary)
                    ) {
                        append("+74957885699, доб. 7777")
                    }
                    pop()
                }
            }
            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "email",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        context.launchMailClientIntent(it.item)
                    }

                    annotatedString.getStringAnnotations(
                        tag = "phone",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        context.launchDialer(it.item)
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Понятно")
            }
        }
    )
}
