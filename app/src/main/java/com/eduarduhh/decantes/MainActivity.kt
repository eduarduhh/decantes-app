package com.eduarduhh.decantes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.eduarduhh.decantes.data.repository.DecantesRepository
import com.eduarduhh.decantes.ui.lock.AppComBloqueio
import com.eduarduhh.decantes.ui.nav.DecantesNavGraph
import com.eduarduhh.decantes.ui.theme.DecantesTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DecantesRepository.getInstance(applicationContext)

        setContent {
            DecantesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppComBloqueio(activity = this) {
                        DecantesNavGraph(repository = repository)
                    }
                }
            }
        }
    }
}
