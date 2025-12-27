# ✅ BORÇ ÖDEME BAKIYE KONTROLÜ EKLENDI

## 📋 Yapılan Değişiklikler

### 1. CashPaymentScreen.kt
**Dosya:** `app/src/main/java/com/burhan2855/borctakip/ui/payment/CashPaymentScreen.kt`

✅ **Kasadan ödeme yapılırken kasa bakiyesi kontrolü eklendi:**

```kotlin
// 1. ViewModel'den kasa bakiyesi al
val kasaBalance by viewModel.kasaBalance.collectAsState()

// 2. Kaydet butonunda kontrol et
Button(
    onClick = {
        val amount = paymentAmount.toDoubleOrNull()
        when {
            amount == null || paymentAmount.isEmpty() -> {
                amountError = "Geçerli bir tutar girin"
            }
            amount <= 0 -> {
                amountError = "Tutar 0'dan büyük olmalıdır"
            }
            amount > kasaBalance -> {
                amountError = "Kasa bakiyesi yetersiz (Mevcut: ₺${String.format("%.2f", kasaBalance)})"
            }
            else -> {
                // İşlem yapıl...
            }
        }
    }
)
```

**Sonuç:**
- Kasa bakiyesinden fazla ödemeye izin verilmez
- Hata mesajında mevcut bakiye gösterilir
- Kaydet butonu devre dışı kalır

### 2. BankPaymentScreen.kt
**Dosya:** `app/src/main/java/com/burhan2855\borctakip/ui/payment/BankPaymentScreen.kt`

✅ **Bankadan ödeme yapılırken banka bakiyesi kontrolü eklendi:**

```kotlin
// 1. ViewModel'den banka bakiyesi al
val bankaBalance by viewModel.bankaBalance.collectAsState()

// 2. Kaydet butonunda kontrol et
Button(
    onClick = {
        val amount = paymentAmount.toDoubleOrNull()
        when {
            // ...existing code...
            amount > bankaBalance -> {
                amountError = "Banka bakiyesi yetersiz (Mevcut: ₺${String.format("%.2f", bankaBalance)})"
            }
            // ...existing code...
        }
    }
)
```

**Sonuç:**
- Banka bakiyesinden fazla ödemeye izin verilmez
- Hata mesajında mevcut bakiye gösterilir
- Kaydet butonu devre dışı kalır

## 🎯 Beklenen Davranış

### Senaryo 1: Borç Ödeme (Kasa, Yeterli Bakiye)
1. Borç listesinden "Kasadan Öde" tıkla
2. Ödeme tutarı gir (mevcut bakiyeden az)
3. Kaydet → ✅ İşlem yapılır
4. Kasa bakiyesi azalır, Borç bakiyesi azalır

### Senaryo 2: Borç Ödeme (Kasa, Yetersiz Bakiye) ⚠️
1. Borç listesinden "Kasadan Öde" tıkla
2. Ödeme tutarı gir (mevcut bakiyeden fazla, örn: 5000 tutar ama bakiye 2000)
3. **Error:** "Kasa bakiyesi yetersiz (Mevcut: ₺2000.00)"
4. Kaydet butonu devre dışı
5. İşlem iptal ✅

### Senaryo 3: Alacak Tahsilat (Kasa, Herhangi Bir Bakiye)
1. Alacak listesinden "Kasadan Tahsilat" tıkla
2. Tahsilat tutarı gir
3. Kaydet → ✅ İşlem yapılır (kontrol yok, giriş işlemi)
4. Kasa bakiyesi artar, Alacak bakiyesi azalır

### Senaryo 4: Bankadan Ödeme (Yetersiz Bakiye) ⚠️
1. Borç listesinden "Bankadan Öde" tıkla
2. Ödeme tutarı gir (mevcut bakiyeden fazla)
3. **Error:** "Banka bakiyesi yetersiz (Mevcut: ₺X.XX)"
4. Kaydet butonu devre dışı
5. İşlem iptal ✅

## 📊 Kontrol Matrisi

| İşlem | Tip | Bakiye Kontrol | Açıklama |
|---|---|---|---|
| Kasa Girişi | Giriş | ❌ Yok | Girişe kontrol gerek yok |
| Kasa Çıkışı (Ekle) | Çıkış | ✅ Var | Bakiye kontrol edilir |
| Borç Öde (Kasa) | Çıkış | ✅ Var | Kasa bakiyesi kontrol edilir |
| Alacak Tahsilat (Kasa) | Giriş | ❌ Yok | Girişe kontrol gerek yok |
| Banka Girişi | Giriş | ❌ Yok | Girişe kontrol gerek yok |
| Banka Çıkışı (Ekle) | Çıkış | ✅ Var | Bakiye kontrol edilir |
| Borç Öde (Banka) | Çıkış | ✅ Var | Banka bakiyesi kontrol edilir |
| Alacak Tahsilat (Banka) | Giriş | ❌ Yok | Girişe kontrol gerek yok |

## ✨ Sonuç

**Tüm çıkış işlemleri (borç ödeme, banka/kasa çıkışı) artık bakiye kontrolü yapıyor:**
- Bakiye yok → ❌ İşlem engellenir + Uyarı
- Bakiye yetersiz → ❌ İşlem engellenir + Uyarı (Mevcut bakiye gösterilir)
- Bakiye yeterli → ✅ İşlem yapılır

## 🚀 Build ve Test
```bash
./gradlew clean :app:assembleDebug
adb uninstall com.burhan2855.borctakip
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Test adımları:
1. Kasa bakiyesini 0'a düşür (Kasa Çıkışı)
2. Borç ekle (5000)
3. "Kasadan Öde" → **Error gösterilmeli** ✅
4. Kasa Girişi ekle (3000)
5. "Kasadan Öde" (2000) → **Yapılmalı** ✅
6. "Kasadan Öde" (2000) → **Error** (1000 kaldı) ✅
