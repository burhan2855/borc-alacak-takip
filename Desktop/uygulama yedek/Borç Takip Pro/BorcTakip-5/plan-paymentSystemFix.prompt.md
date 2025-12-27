# BorçTakip Ödeme Sistemi Düzeltme Planı

## Sorun Özeti
BorçTakip Android uygulamasında ödeme yaparken borç bakiyesi düşüyor ancak kasa/banka bakiyesi değişmiyor. 
Örnek: 30.000₺ borç, 5.000₺ kasadan ödeme yapıldığında:
- ✅ Borç: 30.000 → 25.000 (doğru)
- ❌ Kasa: değişmiyor (yanlış - 5.000₺ düşmesi gerekir)

## Kök Nedenleri

### 1. İşlem Mantığı Hatası
**Dosya:** `CashPaymentScreen.kt` ve `BankPaymentScreen.kt` (satırlar ~165-190)

**Sorun:**
```kotlin
val cashFlowTransaction = Transaction(
    isDebt = !isCashIn,  // ❌ YANLIŞtarih - bu yapı yanlış
    category = if (isCashIn) "Kasa Girişi" else "Kasa Çıkışı"
)
```

**Neden:** `isCashIn` boolean değerine göre transaction'lar oluşturuluyor ama:
- Borç ödeme (çıkış) için `isDebt = true` ayarlanıyor → kasa bakiyesine katılmıyor
- Alacak tahsilat (giriş) için `isDebt = false` ayarlanıyor → yine tutarsız

### 2. Bakiye Hesaplama Mantığı
**Dosya:** `MainViewModel.kt` (satırlar ~45-52)

Mevcut mantık:
```kotlin
val kasaBalance: StateFlow<Double> = allTransactions.map { transactions ->
    transactions
        .filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }
        .sumOf { if (it.category == "Kasa Girişi") it.amount else -it.amount }
}
```

**Problem:** Borç ödeme işlemleri "Kasa Çıkışı" kategorisine sahip ama asıl borç transaction'ı güncellenmeyerek iki kat işlem kaydediliyor.

## Çözüm Planı

### Adım 1: Transaction Entity Yapısını Netleştir
**Dosya:** `data/Transaction.kt`

Transaction türlerini kategorize et:
- **Borç/Alacak İşlemleri**: isDebt=true/false, paymentType=null
- **Kasa/Banka İşlemleri**: isDebt=false, paymentType="Kasa"/"Banka"

### Adım 2: MainViewModel'daki Bakiye Hesaplama
**Dosya:** `ui/MainViewModel.kt` (satırlar ~45-52)

Sabit ve doğru hesapla:
```kotlin
val kasaBalance: StateFlow<Double> = allTransactions.map { transactions ->
    transactions
        .filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }
        .sumOf { 
            when {
                it.category == "Kasa Girişi" -> it.amount
                it.category == "Kasa Çıkışı" -> -it.amount
                else -> 0.0
            }
        }
}
```

### Adım 3: Ödeme Ekranlarını Düzelt
**Dosyalar:** 
- `ui/payment/CashPaymentScreen.kt` (satırlar ~155-190)
- `ui/payment/BankPaymentScreen.kt` (satırlar ~155-190)

**Mantık:**
```kotlin
if (isCashIn) {
    // Alacak tahsilat → Kasa/Banka giriş işlemi
    val cashFlowTransaction = Transaction(
        title = "Tahsilat: ${transaction.title}",
        amount = amount,
        date = selectedDate,
        isDebt = false,
        category = "Kasa Girişi", // VEYA "Banka Girişi"
        paymentType = "Kasa", // VEYA "Banka"
        status = "Ödendi"
    )
} else {
    // Borç ödeme → Kasa/Banka çıkış işlemi
    val cashFlowTransaction = Transaction(
        title = "Ödeme: ${transaction.title}",
        amount = amount,
        date = selectedDate,
        isDebt = false, // 🔑 FİX: false olmalı çünkü bu kasa/banka operasyonu
        category = "Kasa Çıkışı", // VEYA "Banka Çıkışı"
        paymentType = "Kasa", // VEYA "Banka"
        status = "Ödendi"
    )
}
```

### Adım 4: Bakiye Kontrolü Ekle
**Dosyalar:** `CashPaymentScreen.kt` ve `BankPaymentScreen.kt` (Kaydet butonu koşulları)

```kotlin
Button(onClick = {
    val amount = paymentAmount.toDoubleOrNull()
    when {
        amount == null || paymentAmount.isEmpty() -> {
            amountError = "Geçerli bir tutar girin"
        }
        amount <= 0 -> {
            amountError = "Tutar 0'dan büyük olmalıdır"
        }
        !isCashIn && amount > kasaBalance -> {
            // Borç ödeme için kasa bakiyesi kontrol et
            amountError = "Kasa bakiyesi yetersiz (Mevcut: ₺${String.format("%.2f", kasaBalance)})"
        }
        else -> {
            // İşlemi kaydet
        }
    }
})
```

### Adım 5: TransactionDetailScreen Güncellemeleri
**Dosya:** `ui/transactions/TransactionDetailScreen.kt`

Ödeme butonlarının gösterilme koşullarını güncelle:
```kotlin
// Ödeme butonlarını sadece borç/alacak işlemleri için göster
if ((transaction.isDebt || !transaction.isDebt) && 
    transaction.category != "Kasa Girişi" && 
    transaction.category != "Kasa Çıkışı" && 
    transaction.category != "Banka Girişi" && 
    transaction.category != "Banka Çıkışı") {
    // Ödeme butonlarını göster
}
```

## Beklenilen Sonuçlar

Ödeme yapıldığında:
- ✅ Orijinal borç transaction'ı güncellenir (30.000 → 25.000)
- ✅ Kasa/Banka çıkış transaction'ı oluşturulur (-5.000)
- ✅ Kasa bakiyesi düşer (Kasa Girişi - Kasa Çıkışı hesaplaması)
- ✅ Ana ekranda her iki miktar da doğru gösterilir

## Test Senaryoları

1. **Borç Ödeme (Kasadan)**: 30.000₺ borç, 5.000₺ ödeme
   - Beklenen: Borç 25.000, Kasa -5.000

2. **Alacak Tahsilat (Kasadan)**: 20.000₺ alacak, 5.000₺ tahsilat
   - Beklenen: Alacak 15.000, Kasa +5.000

3. **Yetersiz Bakiye**: Kasa 3.000₺, 5.000₺ ödeme deneme
   - Beklenen: Hata mesajı

## Dosyalar Değişecek

1. ✏️ `CashPaymentScreen.kt` - İşlem mantığı
2. ✏️ `BankPaymentScreen.kt` - İşlem mantığı
3. ✏️ `MainViewModel.kt` - Bakiye hesaplama (varsa düzeltme)
4. ✏️ `TransactionDetailScreen.kt` - Buton koşulları (varsa)
5. 🗑️ `PartialPaymentDialog.kt` - Kaldırılabilir (tıkla kullanılmıyorsa)

---

**Hazırladı:** Code Assistant  
**Tarih:** 2025-12-19  
**Durum:** Plan Hazır - Uygulamaya Başlanabilir
