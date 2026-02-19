import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * CustomTextField
 * Standart veri giriş kutusu.
 * @param value İçindeki metin (State).
 * @param onValueChange Kullanıcı bir harf yazdığında tetiklenir.
 * @param label Kutunun üstündeki başlık (Örn: "Adınız").
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
    )
}