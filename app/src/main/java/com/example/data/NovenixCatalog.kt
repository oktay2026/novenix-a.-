package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.ui.graphics.vector.ImageVector

enum class SolutionCategory(val label: String) {
    ALL("Tümü"),
    HARDWARE("Donanım & PCB"),
    SMART_SYSTEMS("Akıllı Sistemler"),
    DEFENSE("Savunma & Endüstri"),
    SOFTWARE("Yazılım & IoT")
}

data class NovenixSolution(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val highlights: List<String>,
    val category: SolutionCategory,
    val icon: ImageVector,
    val webUrl: String = NovenixCatalog.HOME_URL
)

object NovenixCatalog {
    const val HOME_URL = "http://novenix.tech/"
    const val PHONE_DISPLAY = "+90 (533) 949 23 90"
    const val PHONE_URI = "tel:+905339492390"
    const val EMAIL = "info@novenix.tech"
    const val ADDRESS = "Ferhatpaşa Mahallesi, 16. Sokak No: 82, Daire: 2, Ataşehir / İstanbul, Türkiye"
    const val MAP_QUERY = "Ferhatpaşa Mahallesi, 16. Sokak No: 82, Ataşehir, İstanbul"

    val inquiryTopics = listOf(
        "PCB & Elektronik Ar-Ge",
        "NovXLockers Akıllı Dolaplar",
        "Solderprof Üretim Çözümleri",
        "Savunma & Havacılık Sistemleri",
        "Gömülü & IoT Yazılım",
        "Genel Bilgi"
    )

    val solutions = listOf(
        NovenixSolution(
            id = "pcb_rnd",
            title = "Ar-Ge ve Elektronik Kart Tasarımı",
            subtitle = "Şematik, Çok Katmanlı PCB & SMT/THT Dizgi",
            description = "Fikirden seri üretime kadar uçtan uca elektronik donanım mühendisliği, yüksek hızlı PCB tasarımı, komponent tedariği ve elektromekanik prototipleme.",
            highlights = listOf("Çok katmanlı yüksek hızlı PCB", "Komponent tedariği & SMT/THT dizgi", "3D mekanik tasarım & prototipleme"),
            category = SolutionCategory.HARDWARE,
            icon = Icons.Rounded.DeveloperBoard,
            webUrl = HOME_URL
        ),
        NovenixSolution(
            id = "novxlockers",
            title = "NovXLockers",
            subtitle = "Akıllı Kargo & Teslimat Dolap Sistemleri",
            description = "Apartmanlar, kampüsler ve lojistik ağları için modüler akıllı kargo otomatları, teslimat dolapları ve endüstriyel kontrol kartı mimarisi.",
            highlights = listOf("Akıllı teslimat & kargo otomatları", "Özel kontrol kartı sistemleri", "Bulut & mobil yönetim entegrasyonu"),
            category = SolutionCategory.SMART_SYSTEMS,
            icon = Icons.Rounded.Inventory2,
            webUrl = HOME_URL
        ),
        NovenixSolution(
            id = "solderprof",
            title = "Solderprof",
            subtitle = "THT Lehimleme Robotu & SMT Çözümleri",
            description = "Elektronik üretim hatlarında tekrarlanabilir kalite ve hız sağlayan THT lehimleme robotu ve profesyonel üretim ekipmanları.",
            highlights = listOf("Hassas THT lehimleme otomasyonu", "SMT komponent üretim çözümleri", "Endüstriyel hat verimliliği"),
            category = SolutionCategory.HARDWARE,
            icon = Icons.Rounded.PrecisionManufacturing,
            webUrl = HOME_URL
        ),
        NovenixSolution(
            id = "defense_aerospace",
            title = "Savunma, Havacılık & Endüstri",
            subtitle = "GPU, Data Logger & Aviyonik Sistemler",
            description = "Zorlu saha koşullarına dayanıklı Yer Güç Üniteleri (GPU), gelişmiş veri günlükleyiciler (Data Logger) ve güvenilir görev elektroniği.",
            highlights = listOf("Yer Güç Üniteleri (GPU)", "Yüksek güvenilirlikli Data Logger", "Aviyonik & saha standartları"),
            category = SolutionCategory.DEFENSE,
            icon = Icons.Rounded.FlightTakeoff,
            webUrl = HOME_URL
        ),
        NovenixSolution(
            id = "biomedical",
            title = "Biyomedikal & Sağlık Teknolojileri",
            subtitle = "Akıllı İlaç & Sarf Malzeme Takibi",
            description = "Hastaneler ve klinikler için yetkilendirilmiş erişimli akıllı ilaç ve tıbbi sarf malzeme takip dolapları ile güvenli stok yönetimi.",
            highlights = listOf("Akıllı ilaç takip sistemleri", "Sarf malzeme envanter kontrolü", "Anlık izleme ve raporlama"),
            category = SolutionCategory.SMART_SYSTEMS,
            icon = Icons.Rounded.MedicalServices,
            webUrl = HOME_URL
        ),
        NovenixSolution(
            id = "embedded_iot",
            title = "Gömülü Sistemler, IoT & Yazılım",
            subtitle = "Firmware, Mobil, Masaüstü & Bulut",
            description = "Donanım ile tam uyumlu gerçek zamanlı gömülü yazılımlar, IoT haberleşme protokolleri, mobil uygulamalar ve sunucu tabanlı bulut platformları.",
            highlights = listOf("Gerçek zamanlı gömülü firmware", "IoT haberleşme entegrasyonları", "Mobil, masaüstü & bulut yazılımı"),
            category = SolutionCategory.SOFTWARE,
            icon = Icons.Rounded.Memory,
            webUrl = HOME_URL
        )
    )
}
