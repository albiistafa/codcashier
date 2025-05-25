package dev.codcow.kasirku.features.menuManager.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.codcow.kasirku.core.data.model.menu.RequestMenu
import dev.codcow.kasirku.ui.theme.AppTheme
import java.io.File
import android.util.Log
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuatMenuScreen(
    viewModel: MenuManagerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
        viewModel.fetchSubCategories()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            // Kembali ke screen sebelumnya
            onNavigateBack()
        }
    }

    var menuName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSubCategoryId by remember { mutableStateOf<Int?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSubCategoryDropdown by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    // Fungsi untuk mengkonversi Uri ke File
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    "Buat Menu",
                    style = AppTheme.typography.labelBold,
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

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
                placeholder = { Text("") },
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
                value = price,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        price = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("") },
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
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Pilih Kategori") }
                )

                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier
                        .background(Color.White)
                        .exposedDropdownSize()
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = category.name,
                                    style = AppTheme.typography.paragraph2
                                )
                            },
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
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Pilih Foto",
                            style = AppTheme.typography.paragraph2,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                // Validasi input
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
                            // Konversi Uri ke File
                            val imageFile = uriToFile(imageUri!!)

                            if (imageFile != null) {
                                // Gunakan metode createMenuWithImage
                                Log.d("BuatMenuScreen", "Creating menu with image: $menuName, $cleanPrice, $selectedCategoryId, $selectedSubCategoryId, $imageFile")
                                viewModel.createMenuWithImage(
                                    name = menuName,
                                    price = cleanPrice,
                                    categoryId = selectedCategoryId!!,
                                    subCategoryId = selectedSubCategoryId,
                                    imageFile = imageFile
                                )
                            } else {
                                viewModel.setError("Gagal memproses gambar")
                            }
                        } else {
                            // Tidak ada gambar, gunakan metode createMenu biasa
                            val newMenu = RequestMenu(
                                name = menuName,
                                price = cleanPrice,
                                category_id = selectedCategoryId!!,
                                sub_category_id = selectedSubCategoryId,
                                photo = null // Tidak ada gambar
                            )

                            Log.d("BuatMenuScreen", "Creating menu without image: $newMenu")
                            viewModel.createMenu(newMenu)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.surface,
                disabledContainerColor = Color.Gray
            ),
            enabled = menuName.isNotEmpty() && price.isNotEmpty() && selectedCategoryId != null
        ) {
            Text("Buat Menu", color = Color.White, style = AppTheme.typography.paragraph1)
        }
    }

    // Show error if any
    if (error != null) {
        LaunchedEffect(error) {
            // Show snackbar or toast with error message
            // Implementasi sesuai kebutuhan
        }
    }

    // Show loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}