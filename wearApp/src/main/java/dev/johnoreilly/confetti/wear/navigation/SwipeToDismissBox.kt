package dev.johnoreilly.confetti.wear.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissKeys
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

/**
 * Displays the [ChildStack] in [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 *
 * @param stack a [ChildStack] to be displayed.
 * @param onDismissed called when the swipe to dismiss gesture has completed, allows popping the stack.
 * See [StackNavigator#pop][com.arkivanov.decompose.router.stack.pop].
 * @param modifier a [Modifier] to be applied to the underlying
 * [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 * @param content a Composable slot displaying a [Child][Child.Created].
 */
@Composable
fun <C : Any, T : Any> SwipeToDismissBox(
    stack: Value<ChildStack<C, T>>,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) {
    val state = stack.subscribeAsState()

    SwipeToDismissBox(
        stack = state.value,
        onDismissed = onDismissed,
        modifier = modifier,
        content = content,
    )
}

/**
 * Displays the [ChildStack] in [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 *
 * @param stack a [ChildStack] to be displayed.
 * @param onDismissed called when the swipe to dismiss gesture has completed, allows popping the stack.
 * See [StackNavigator#pop][com.arkivanov.decompose.router.stack.pop].
 * @param modifier a [Modifier] to be applied to the underlying
 * [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 * @param content a Composable slot displaying a [Child][Child.Created].
 */
@Composable
fun <C : Any, T : Any> SwipeToDismissBox(
    stack: ChildStack<C, T>,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (child: Child.Created<C, T>) -> Unit,
) {
    val active: Child.Created<C, T> = stack.active
    val background: Child.Created<C, T>? = stack.backStack.lastOrNull()
    val holder = rememberSaveableStateHolder()

    RetainStates(holder, stack.getConfigurations())

    SwipeToDismissBox(
        onDismissed = onDismissed,
        modifier = modifier,
        backgroundScrimColor = workaroundBackgroundScrimColor(stackSize = stack.items.size),
        backgroundKey = background?.configuration ?: SwipeToDismissKeys.Background,
        contentKey = active.configuration,
        hasBackground = background != null,
    ) { isBackground ->
        val child = background?.takeIf { isBackground } ?: active
        holder.SaveableStateProvider(child.configuration.key()) {
            content(child)
        }
    }
}

// Workaround for https://issuetracker.google.com/issues/280392104
@Composable
private fun workaroundBackgroundScrimColor(stackSize: Int): Color {
    return MaterialTheme.colors.background.copy(alpha = 1F - ((stackSize % 2) * 0.01F))
}

private fun ChildStack<*, *>.getConfigurations(): Set<String> =
    items.mapTo(HashSet()) { it.configuration.key() }

private fun Any.key(): String = "${this::class.simpleName}_${hashCode().toString(radix = 36)}"

@Composable
private fun RetainStates(holder: SaveableStateHolder, currentKeys: Set<String>) {
    val keys = remember(holder) { Keys(currentKeys) }

    DisposableEffect(holder, currentKeys) {
        keys.set.forEach {
            if (it !in currentKeys) {
                holder.removeState(it)
            }
        }

        keys.set = currentKeys

        onDispose {}
    }
}

private class Keys(var set: Set<String>)
