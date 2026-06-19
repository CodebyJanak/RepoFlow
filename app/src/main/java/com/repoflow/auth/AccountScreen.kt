package com.repoflow.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bio
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.PrimaryButton
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val accountState by viewModel.accountState.collectAsState()
    val logoutComplete by viewModel.logoutComplete.collectAsState()

    LaunchedEffect(logoutComplete) {
        if (logoutComplete) {
            onLoggedOut()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RepoFlowTopAppBar(
            title = "Account",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val user = accountState.user

            if (user != null) {
                Spacer(modifier = Modifier.height(16.dp))

                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user.name ?: user.login,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "@${user.login}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!user.bio.isNullOrBlank()) {
                    DetailItem(
                        icon = Icons.Outlined.Bio,
                        label = "Bio",
                        value = user.bio
                    )
                }

                if (!user.email.isNullOrBlank()) {
                    DetailItem(
                        icon = Icons.Outlined.Email,
                        label = "Email",
                        value = user.email
                    )
                }

                DetailItem(
                    icon = Icons.Outlined.Folder,
                    label = "Public Repos",
                    value = user.publicRepos.toString()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = if (accountState.isLoggingOut) "Logging out..." else "Logout",
                    onClick = { viewModel.logout() },
                    enabled = !accountState.isLoggingOut
                )
            } else if (accountState.isLoading) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Not logged in",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun AccountScreenPreview() {
    RepoFlowTheme(darkTheme = true) {
        AccountScreen(onBack = {}, onLoggedOut = {})
    }
}
