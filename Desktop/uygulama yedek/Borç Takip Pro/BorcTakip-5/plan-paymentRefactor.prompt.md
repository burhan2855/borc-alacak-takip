# Ödeme Sistemi Yeniden Tasarımı

## Sorun
- Kısmi ödeme sistemi tam ödemeyi işlemiyor
- Kasa/Banka bakiyesi güncellenmiyordu
- Karmaşık logic ve Firestore senkronizasyon sorunları

## Çözüm
Kısmi ödeme sistemini kaldırıp, daha basit ve güvenilir bir sistem kuracağız:

1. **PaymentDialog.kt**: Kasadan/Bankadan tam ödeme butonları
2. **TransactionDetailScreen.kt**: Ödemeleri işleme ve nakit akışı oluşturma
3. **MainViewModel.kt**: `processPayment()` metodu ile tam ödeme işleme
4. **UI Components**: Kısmi ödeme dialog'u kaldır

## Değişiklikler

### 1. PaymentDialog.kt - Güncelleme
- Kasa/Banka seçimi (önceki sistem korunur ama sadece tam ödeme)
- Ödeme tutarı otomatik olarak transaction amount'u kullan
- Seç ve Öde sistemi

### 2. TransactionDetailScreen.kt - İyileştirme
- "Kasadan Öde" ve "Bankadan Öde" butonları ekle
- Hızlı ödeme işlemi

### 3. MainViewModel.kt - Yeni metod
- `processPayment()`: Tam ödeme ve nakit akışı oluştur
- Basit ve hatasız işlem

### 4. Kısmi Ödeme Dialog Kaldır
- PartialPaymentDialog.kt kullanmayı durdur
- İlgili state'ler kaldır

## Test Planı
1. Kasadan tam ödeme yap → Kasa bakiyesi düş, borç silinsin
2. Bankadan tam ödeme yap → Banka bakiyesi düş, borç silinsin
3. Ödeme tutarları doğru gösterilsin
