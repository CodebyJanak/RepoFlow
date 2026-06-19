package com.repoflow.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.repoflow.core.theme.RepoFlowTheme
import com.repoflow.core.ui.components.PrimaryButton
import com.repoflow.core.ui.components.RepoFlowTopAppBar

@Composable
fun SettingsScreen(
    onNavigateToAccount: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        RepoFlowTopAppBar(
            title = "Settings",
            navigationIcon = null,
            onNavigationClick = null
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SectionHeader(title = "Account")
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    title = "GitHub Profile",
                    subtitle = "Not logged in",
                    onClick = onNavigateToAccount
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Switch Account",
                    subtitle = "Manage multiple accounts"
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Preferences")
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "Theme",
                    subtitle = "Dark"
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = "Dynamic Colors",
                    subtitle = "Material You enabled"
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Security")
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Fingerprint,
                    title = "Biometric Lock",
                    subtitle = "Require authentication to open app"
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Encrypted Storage",
                    subtitle = "Tokens stored securely"
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Connections")
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Computer,
                    title = "Remote Workspace",
                    subtitle = "No devices connected"
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Link,
                    title = "Linked Services",
                    subtitle = "GitHub, GitLab"
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Storage")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Storage,
                    title = "Cache & Data",
                    subtitle = "128 MB used"
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "About")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Version",
                    subtitle = "1.0.0"
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = "Report Issue",
                    subtitle = "Send feedback"
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Outlined.Build,
                    title = "Developer Options",
                    subtitle = "Debug settings"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    text = "Logout",
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
    Divider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun SettingsScreenPreview() {
    RepoFlowTheme(darkTheme = true) {
        SettingsScreen()
    }
}
