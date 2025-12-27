# ✅ Hata Çözümü - Kasa/Banka Bakiyesi Problemi

## Sorun
- Kasa ve Banka giriş/çıkış işlemleri, Borç ve Alacak bakiyelerine yansıyordu
- Borç ve Alacak işlemleri, Kasa ve Banka bakiyelerine yansıyordu

## Nedeni
Bakiye hesaplamalarında `paymentType` ve `isDebt` alanları kullanılıyordu, bu da işlemleri yanlış sınıflandırıyordu.

Eski kod:
```kotlin
// ❌ YANLIŞ
val kasaBalance = transactions
    .filter { it.paymentType == "Kasa" }  // Tüm Kasa işlemleri dahil
    .sumOf { if (it.isDebt) -it.amount else it.amount }  // Yanlış hesap

val debtTotal = transactions
    .filter { it.isDebt }  // Kasa/Banka işlemleri de dahil!
    .sumOf { it.amount }
```

## Çözüm
Category alanıyla doğru filtreleme:

```kotlin
// ✅ DOĞRU
val kasaBalance = transactions
    .filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }
    .sumOf { if (it.category == "Kasa Girişi") it.amount else -it.amount }

val debtTotal = transactions
    .filter { 
        it.isDebt && 
        it.category != "Kasa Girişi" && it.category != "Kasa Çıkışı" &&
        it.category != "Banka Girişi" && it.category != "Banka Çıkışı"
    }
    .sumOf { it.amount }
```

## Yapılan Değişiklikler

### 1. MainViewModel.kt
- ✅ `kasaBalance` hesaplamasını düzeltildi
- ✅ `bankaBalance` hesaplamasını düzeltildi

### 2. DebtTrackerApp.kt  
- ✅ `cashTransactions` filtrelemesi düzeltildi
- ✅ `bankTransactions` filtrelemesi düzeltildi
- ✅ `debtTransactions` filtrelemesi düzeltildi
- ✅ `creditTransactions` filtrelemesi düzeltildi
- ✅ `cashTotal` hesaplaması düzeltildi
- ✅ `bankTotal` hesaplaması düzeltildi

### 3. ReportScreen.kt
- ✅ Zaten doğru yapılmış (değişiklik yapmaya gerek yok)

## Test Sonuçları Beklenen
1. **Kasa Girişi/Çıkışı işlemi** → Sadece Kasa bakiyesini etkiler
2. **Banka Girişi/Çıkışı işlemi** → Sadece Banka bakiyesini etkiler
3. **Borç işlemi** → Sadece Borç bakiyesini etkiler
4. **Alacak işlemi** → Sadece Alacak bakiyesini etkiler

## Yeni Build ve Test
```bash
./gradlew clean :app:assembleDebug
adb uninstall com.burhan2855.borctakip
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Ardından:
1. Ana ekranda Kasa, Banka, Borç, Alacak bakiyelerine bakılacak
2. Her işlem türü sadece kendi bakiyesini etkilemeliydi
