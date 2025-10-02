package com.bodakesatish.sandhyasbeautyservices.compose.topappbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bodakesatish.sandhyasbeautyservices.compose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTopAppBar() {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.categories_title)) },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}