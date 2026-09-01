package com.eduarduhh.decantes.ui.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private const val AUTENTICADORES = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

@Composable
fun AppComBloqueio(
    activity: FragmentActivity,
    content: @Composable () -> Unit
) {
    var desbloqueado by remember { mutableStateOf(false) }

    fun autenticar() {
        val podeAutenticar = BiometricManager.from(activity)
            .canAuthenticate(AUTENTICADORES) == BiometricManager.BIOMETRIC_SUCCESS

        if (!podeAutenticar) {
            desbloqueado = true
            return
        }

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    desbloqueado = true
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Decantes")
            .setSubtitle("Desbloqueie para continuar")
            .setAllowedAuthenticators(AUTENTICADORES)
            .build()
        prompt.authenticate(promptInfo)
    }

    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> desbloqueado = false
                Lifecycle.Event.ON_RESUME -> if (!desbloqueado) autenticar()
                else -> Unit
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    if (desbloqueado) {
        content()
    } else {
        TelaBloqueada(onTentarNovamente = ::autenticar)
    }
}

@Composable
private fun TelaBloqueada(onTentarNovamente: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("App bloqueado", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onTentarNovamente) {
                Text("Tentar novamente")
            }
        }
    }
}
