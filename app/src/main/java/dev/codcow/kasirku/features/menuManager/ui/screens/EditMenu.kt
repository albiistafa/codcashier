package dev.codcow.kasirku.features.menuManager.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.codcow.kasirku.core.data.model.menu.RequestMenu
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMenuScreen(
    navController: NavController,
    viewModel: MenuManagerViewModel = hiltViewModel(),
    menuId: Int
) {
    val menu by viewModel.selectedMenu.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var menuName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSubCategoryId by remember { mutableStateOf<Int?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSubCategoryDropdown by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load menu data when component is first created
    LaunchedEffect(menuId) {
        viewModel.getMenuById(menuId)
    }

    LaunchedEffect(menu) {
        menu?.let {
            menuName = it.name
            price = it.price
            val priceDouble = it.price.toDoubleOrNull()
            price = if (priceDouble != null && priceDouble == priceDouble.toLong().toDouble()) {
                priceDouble.toLong().toString()
            } else {
                it.price
            }
            selectedCategoryId = it.category_id
            selectedSubCategoryId = it.sub_category_id
            imageUri = it.photo?.let { photoUrl -> Uri.parse(photoUrl) }
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Handle success state
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.navigateUp()
        }
    }

    // Image picker
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "menu_image_${System.currentTimeMillis()}.jpg"
            val outputFile = File(context.cacheDir, fileName)

            inputStream?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Validasi ukuran file (maks 2MB)
            val maxFileSize = 1024 * 1024// 2MB dalam byte
            if (outputFile.length() > maxFileSize) {
                Log.e("BuatMenuScreen", "File terlalu besar: ${outputFile.length()} bytes")
                // hapus file yang terlalu besar dari cache
                outputFile.delete()
                return null
            }

            outputFile
        } catch (e: Exception) {
            Log.e("BuatMenuScreen", "Error converting Uri to File: ${e.message}")
            null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Menu",
                        style = AppTheme.typography.labelBold,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {

                Divider()

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f)
                ) {
                    // Nama Menu Field
                    Text(
                        text = "Nama Menu *",
                        style = AppTheme.typography.paragraph2,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = menuName,
                        onValueChange = { menuName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Harga Field
                    Text(
                        text = "Harga *",
                        style = AppTheme.typography.paragraph2,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = price.toString(),
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                price = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        leadingIcon = { Text("Rp", modifier = Modifier.padding(start = 8.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Kategori Dropdown
                    Text(
                        text = "Kategori *",
                        style = AppTheme.typography.paragraph2,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { showCategoryDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryId?.let { categoryId ->
                                categories.find { it.id == categoryId }?.name ?: ""
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = if (selectedCategoryId == category.id) {
                                            null
                                        } else {
                                            category.id
                                        }
                                        selectedSubCategoryId = null
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sub-Kategori Dropdown
                    Text(
                        text = "Sub-Kategori",
                        style = AppTheme.typography.paragraph2,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = showSubCategoryDropdown,
                        onExpandedChange = {
                            if (selectedCategoryId != null) {
                                showSubCategoryDropdown = it
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedSubCategoryId?.let { subCatId ->
                                subCategories.find { it.id == subCatId }?.name ?: ""
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = selectedCategoryId != null,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubCategoryDropdown)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray,
                                disabledBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                disabledTextColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("Pilih Sub-kategori") }
                        )

                        ExposedDropdownMenu(
                            expanded = showSubCategoryDropdown,
                            onDismissRequest = { showSubCategoryDropdown = false },
                            modifier = Modifier
                                .background(Color.White)
                                .exposedDropdownSize()
                        ) {
                            val filteredSubCategories = subCategories.filter {
                                it.category_id == selectedCategoryId
                            }

                            filteredSubCategories.forEach { subCategory ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = subCategory.name,
                                            style = AppTheme.typography.paragraph2
                                        )
                                    },
                                    onClick = {
                                        selectedSubCategoryId = if (selectedSubCategoryId == subCategory.id) {
                                            null
                                        } else {
                                            subCategory.id
                                        }
                                        showSubCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Photo Upload
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(imageUri)
                                        .build()
                                ),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Panorama,
                                    contentDescription = "Upload Image",
                                    tint = Color.Gray
                                )
                                Text("Pilih Foto", color = Color.Gray)
                            }
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        when {
                            menuName.isEmpty() -> {
                                viewModel.setError("Nama menu tidak boleh kosong")
                            }

                            price.isEmpty() -> {
                                viewModel.setError("Harga tidak boleh kosong")
                            }

                            selectedCategoryId == null -> {
                                viewModel.setError("Pilih kategori terlebih dahulu")
                            }

                            else -> {
                                val cleanPrice = price.replace(Regex("[^0-9]"), "")

                                // Cek apakah ada gambar yang dipilih
                                if (imageUri != null) {
                                    val isLocalImage = imageUri?.scheme == "content"
                                    val imageFile =
                                        if (isLocalImage) uriToFile(imageUri!!) else null

                                    if (isLocalImage && imageFile == null ) {
                                        viewModel.setError("Gagal memproses gambar")
                                    } else {
                                        val maxSizeInBytes = 1 * 1024 * 1024
                                        if (imageFile != null && imageFile.length() > maxSizeInBytes) {
                                            viewModel.setError("Ukuran gambar terlalu besar, maksimal 1MB")
                                        }
                                        Log.d(
                                            "EditMenuScreen",
                                            "Update menu with image: $menuName, $cleanPrice, $selectedCategoryId, $selectedSubCategoryId, $imageFile"
                                        )
                                        viewModel.updateMenu(
                                            id = menuId,
                                            name = menuName,
                                            price = cleanPrice,
                                            categoryId = selectedCategoryId!!,
                                            subCategoryId = selectedSubCategoryId,
                                            imageFile = imageFile // null jika tidak ada update gambar
                                        )
                                    }
                                } else {
                                    // Tidak ada gambar, gunakan metode createMenu biasa
                                    val newMenu = RequestMenu(
                                        name = menuName,
                                        price = cleanPrice,
                                        category_id = selectedCategoryId!!,
                                        sub_category_id = selectedSubCategoryId,
                                        photo = null
                                    )
                                    Log.d("BuatMenuScreen", "Creating menu without image: $newMenu")
                                    viewModel.createMenu(newMenu)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.surface
                    ),
                    enabled = menuName.isNotEmpty() && price.isNotEmpty() && selectedCategoryId != null
                ) {
                    Text("Simpan Perubahan", color = Color.White)
                }
            }


            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // Error handling
            if (error != null) {
                Snackbar { Text(error!!) }
            }

        }
    )
}


