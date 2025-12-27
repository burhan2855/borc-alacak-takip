# Logcat Filtreleme Talimatları

## Android Studio Logcat'te Filtreleme:

1. **Logcat penceresini açın** (Alt+6)
2. **Filtre kutusuna** şunu yazın:
   ```
   tag:DB_DUMP
   ```
3. Veya dropdown'dan "Show only selected application" seçin

## Komut Satırından (Terminal):

```powershell
# Önce logcat'i temizle
adb logcat -c

# Kısmi ödeme yapın (uygulama içinde)

# Sadece DB_DUMP loglarını göster
adb logcat -s DB_DUMP
```

## Beklenen Loglar (Başarılı Kısmi Ödeme):

```
DB_DUMP: === PARTIAL PAYMENT START ===
DB_DUMP: Transaction ID: X
DB_DUMP: Current amount: 15000.0
DB_DUMP: Payment amount: 5000.0
DB_DUMP: Payment source: Kasa
DB_DUMP: Transaction status: Ödenmedi
DB_DUMP: Creating cash flow transaction: Ödeme: [Borç Adı]
DB_DUMP: Transaction inserted to Room with ID: Y
DB_DUMP: Attempting Firestore sync...
DB_DUMP: Firestore insert failed (continuing with local data): ...
DB_DUMP: Cash flow transaction created with ID: Y
DB_DUMP: Applying partial payment to transaction ID: X
DB_DUMP: applyPartialPayment: transactionId=X, paymentAmount=5000.0
DB_DUMP: Original transaction before update: Transaction(...)
DB_DUMP: Rows affected by applyPartialPayment: 1
DB_DUMP: Updated transaction after DB update: Transaction(amount=10000.0, ...)
DB_DUMP: === PARTIAL PAYMENT COMPLETED SUCCESSFULLY ===
```

## Hata Durumları:

### 1. ID=0 Hatası:
```
DB_DUMP: ERROR: Transaction ID is 0, cannot process partial payment
```
**Çözüm:** Transaction'ı Room'dan değil, UI'dan geçin (ID'li nesne)

### 2. Insert Başarısız:
```
DB_DUMP: CRITICAL: Failed to insert cash flow transaction: ...
```
**Çözüm:** Room veritabanı problemi - `.\gradlew.bat :app:clean` sonra rebuild

### 3. Güncelleme Başarısız:
```
DB_DUMP: ERROR: No rows affected - transaction may not exist
```
**Çözüm:** Transaction ID veritabanında yok - veri senkronizasyon sorunu

## Manuel Test Adımları:

1. ✅ Uygulamayı aç
2. ✅ Borçlar ekranına git
3. ✅ Bir borç seç (örn: 15.000 TL)
4. ✅ Kısmi ödeme butonuna tıkla
5. ✅ Kasa seç
6. ✅ 5.000 gir
7. ✅ Onayla
8. ✅ **Kontrol et:**
   - Borç tutarı 15.000 → 10.000 oldu mu?
   - Kasa bakiyesi 5.000 azaldı mı?
   - "Ödeme: [Borç Adı]" yeni işlem oluştu mu?

## Sorun Devam Ediyorsa:

Lütfen şu bilgileri paylaşın:
1. **Kısmi ödeme sonrası ne oldu?** (Borç/Kasa tutarları değişti mi?)
2. **Logcat'te DB_DUMP satırları var mı?** (yukarıdaki filtreyle)
3. **Hata mesajı gördünüz mü?** (Toast/Dialog)

---

**NOT:** Gösterdiğiniz Firestore/GoogleApiManager uyarıları **normaldir** ve artık işlemi **engellemez**. Local veritabanı mükemmel çalışmalı!
