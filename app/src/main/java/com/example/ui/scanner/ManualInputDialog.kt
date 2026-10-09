package com.example.ui.scanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ManualInputDialog(
    onDismiss: () -> Unit,
    onSubmit: (barcode: String) -> Unit
) {
    var barcodeText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "バーコード番号を手動入力",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "JANコード(8桁または13桁)や商品コードを入力してください",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = barcodeText,
                    onValueChange = {
                        barcodeText = it.filter { char -> char.isDigit() || char.isLetter() }
                        isError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_barcode_input"),
                    label = { Text("バーコード番号") },
                    placeholder = { Text("例: 4901005511170") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (barcodeText.isNotBlank()) {
                                onSubmit(barcodeText.trim())
                            } else {
                                isError = true
                            }
                        }
                    ),
                    isError = isError,
                    trailingIcon = {
                        if (barcodeText.isNotEmpty()) {
                            IconButton(onClick = { barcodeText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "クリア")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                if (isError) {
                    Text(
                        text = "バーコード番号を入力してください",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (barcodeText.isNotBlank()) {
                        onSubmit(barcodeText.trim())
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("submit_manual_barcode_button")
            ) {
                Text("検索する")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_manual_barcode_button")
            ) {
                Text("キャンセル")
            }
        }
    )
}
