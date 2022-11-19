package com.walfud.cc.android


import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.walfud.cc.android.common.MyViewModelFactory
import com.walfud.cc.android.page.*
import com.walfud.cc.android.ui.theme.CrossCopyAndroidTheme

@Composable
fun App() {
    CrossCopyAndroidTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = ROUTE_CONTENT_PAGE,
        ) {
            composable(ROUTE_CONTENT_PAGE) {
                val vm = viewModel<ContentViewModel>(factory = MyViewModelFactory(navController))
                vm.snackbarHostState = remember { SnackbarHostState() }
                ContentPage(vm)
            }

            composable(ROUTE_TOKEN_PAGE) {
                val vm = viewModel<TokenViewModel>(factory = MyViewModelFactory(navController))
                vm.snackbarHostState = remember { SnackbarHostState() }
                TokenPage(vm)
            }
        }
    }
}