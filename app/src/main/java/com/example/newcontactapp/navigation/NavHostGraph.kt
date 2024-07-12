package com.example.newcontactapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.newcontactapp.presentation.AllContactScreen
import com.example.newcontactapp.presentation.ContactState
import com.example.newcontactapp.presentation.ContactViewModel
import com.example.newcontactapp.presentation.AddEditScreen

@Composable
fun NavHostGraph(
    navController: NavHostController,
    state: ContactState,
    viewModel: ContactViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AllContactScreen.route
    ) {
        composable(Routes.AllContactScreen.route) {
            AllContactScreen(viewModel = viewModel, state = state, navController = navController)
        }

        composable(Routes.AddEditScreen.route) {
            AddEditScreen(state = state, navController = navController) {
                viewModel.saveContact()
            }
        }
    }
}