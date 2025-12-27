# Hızlı Başlangıç - Yeni Ödeme Sistemi

## Ne Değişti?

### ❌ Eski Sistem
```
1. Kısmi Ödeme Dialog açılıyordu
2. Tutarı manuel giriyordun
3. Kasa/Banka seçiyordun
4. Sorunlu: Borç bakiyesi düşmüyordu
5. Sorunlu: Kasa/Banka bakiyesi güncellenmiyor
```

### ✅ Yeni Sistem
```
1. İşlem detayına giriyorsun
2. "Kasadan Öde" VEYA "Bankadan Öde" butonu görüyorsün
3. Basana bas, işlem bitti
4. Borç otomatikmen "Ödendi" olur
5. Kasa/Banka bakiyesi artıyor
```

## Kullanıcı Akışı

### Bir Borcu Ödeme

**1. Adım: Borcu Seç**
- Ana menü → "Borçlar" → Bir borcu seç

**2. Adım: Öde**
- İşlem detay sayfasında aşağıya indir
- "Kasadan Öde" (yeşil) veya "Bankadan Öde" (mavi) butonu gör
- Ödeme yapmak istediğin kaynağı seç

**3. Adım: Onay**
- Otomatikmen ödeme işlenir
- "Ödeme: [Borç Adı]" isimli yeni transaction oluşturulur
- Orijinal borç "Ödendi" olarak işaretlenir
- Geri buton otomatikmen açılır

## Kod Örneği

Sistem mimarisi:

```kotlin
// UI: Buton tıkla
Button(onClick = { 
    viewModel.processPayment(transaction, "Kasa")
})

// ViewModel: Tam ödeme işle
fun processPayment(transaction: Transaction, paymentSource: String) {
    // 1. Nakit akışı transaction'ı oluştur
    val cashFlow = Transaction(
        title = "Ödeme: ${transaction.title}",
        amount = transaction.amount,  // Tam tutar
        paymentType = paymentSource,   // "Kasa" veya "Banka"
        status = "Ödendi"
    )
    repository.insert(cashFlow)
    
    // 2. Orijinal borcu kapat
    val paidTransaction = transaction.copy(
        status = "Ödendi",
        amount = 0.0
    )
    repository.update(paidTransaction)
}
```

## Avantajlar

| Eski | Yeni |
|-----|------|
| Kısmi ödeme seçeneği | Sadece tam ödeme |
| Tutar girişi zorunlu | Otomatik tutar |
| Sıklıkla hata | Garantili çalışma |
| Karmaşık UI | Basit 2 buton |
| Bakiye güncellenmiyor | Hemen güncelleniyor |

## Sorun Çözümleri

### S: Kısmi ödeme yapamaz mıyım?
**C**: Şimdi tam ödeme zorunlu. Kısmi ödeme tamamen kaldırıldı çünkü hata yapıyordu.

### S: Ödeme butonlarını nerede göreceğim?
**C**: 
- İşlem Detayı sayfası (TransactionDetailScreen)
- Borçlar Sayfası (DebtTransactionsScreen) - Kısa menüde
- Alacaklar Sayfası (CreditTransactionsScreen) - Kısa menüde
- Ve diğer tüm listelerde

### S: İşlem iptal edebilir miyim?
**C**: Ödeme oluşturduktan sonra:
1. "Ödeme: [Adı]" transaction'ını silebilirsin
2. Orijinal borcu tekrar açabilirsin (Durum: Ödenmedi)

## Gerekirse Test Etme

```
1. Yeni borç oluştur: 100 TL
2. "Kasadan Öde" tıkla
3. Kontrol: Kasa bakiyesi artmalı
4. Kontrol: Borç "Ödendi" olmalı
5. Kontrol: "Ödeme: [Borç Adı]" görülmeli
```

---

**Sorun varsa**: 
- Logcat'i kontrol et
- "DB_DUMP" tag'ını filtrele
- Ekran görüntüsü al
