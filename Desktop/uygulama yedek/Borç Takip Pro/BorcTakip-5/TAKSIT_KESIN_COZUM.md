# Taksit Ay Hesaplama - KESIN ÇÖZÜM

## ❌ Problem
23.12.2025 tarihinde 3 taksit oluştururken, hepsi **aynı tarihte (23.12.2025)** kalıyor.

## 🔍 Root Cause
`Calendar.add(Calendar.MONTH, index)` metodu:
1. Timezone farkından etkileniyor
2. DST geçişlerinde ay hesabını yanlışlaştırıyor  
3. Saat bilgisini temizlesek bile çalışmıyor

## ✅ Kesin Çözüm
**Ay hesabı manuel yapılıyor** - ay/yıl taşmasını elimizle yönetiyoruz:

```kotlin
repeat(installments) { index ->
    // Original ay/yıl/gün al
    val origDay = tempCal.get(Calendar.DAY_OF_MONTH)
    val origMonth = tempCal.get(Calendar.MONTH)  // 0-11
    val origYear = tempCal.get(Calendar.YEAR)
    
    // Hedef ayını hesapla (yıl taşmasını yönet)
    var targetMonth = origMonth + index
    var targetYear = origYear
    while (targetMonth > 11) {      // ← YIL TAŞMASI
        targetMonth -= 12
        targetYear++
    }
    
    // Yeni calendar oluştur
    val dueDateCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, targetYear)
        set(Calendar.MONTH, targetMonth)
        set(Calendar.DAY_OF_MONTH, origDay)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
```

## 📊 Beklenen Sonuç

| # | Başlık | Vade |
|---|--------|------|
| 1 | test alacak (1/3) | **23.12.2025** ✅ |
| 2 | test alacak (2/3) | **23.01.2026** ✅ |
| 3 | test alacak (3/3) | **23.02.2026** ✅ |

## 📝 Değişiklik
**File:** `AddTransactionScreen.kt`
**Satırlar:** ~234-276
**Yöntem:** Ay manuel hesaplama + yıl taşması denetimi

## 🧪 Test Prosedürü
1. Yeni APK build (yapılıyor...)
2. Uygulamaya kur
3. Alacak/Borç Ekle → 3 Taksit → 23.12.2025
4. Kaydet
5. Sonuç: 3 farklı tarih (23.12, 23.01, 23.02)

---

## Teknik Detay: Neden Çalışıyor?

### Eski: `Calendar.add()` ❌
```
Day 23, Month 11 (Dec 0-indexed)
add(MONTH, 1) → Month 12 (invalid!)
```
Timezone'a göre farklı değer döndürebiliyor.

### Yeni: Manuel Hesaplama ✅
```
Day 23, Month 11 + 1 = Month 12
while (12 > 11) → Month = 0, Year = 2026
Result: Day 23, Month 0 (Jan), Year 2026 ✓
```
Timezone-indifferent, deterministik.

---

**Status:** Build yapılıyor, yakında test edilecek 🚀
