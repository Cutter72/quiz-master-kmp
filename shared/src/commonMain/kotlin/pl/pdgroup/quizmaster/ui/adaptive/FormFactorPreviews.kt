package pl.pdgroup.quizmaster.ui.adaptive

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Phone Landscape", widthDp = 840, heightDp = 390, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showBackground = true)
annotation class FormFactorPreviews
