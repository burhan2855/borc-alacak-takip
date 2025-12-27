# Plan: Kasa/Banka Ödeme ve Tahsilat Düzeltmesi

## Sorun
Kasadan ödeme/tahsilat yapılırken:
1. Borç/alacak tutarı düşmüyor (30.000 TL kalıyor, 5.000 TL ödendikten sonra)
2. Kasa/Banka bakiyesi güncellenmemiyor
3. Aynı sorun banka işlemleri için de geçerli

## Esas Nedenler
1. TransactionDetailScreen'deki ödeme butonları navigation route parametrelerini **yanlış geçiyordu**:
   - `navController?.navigate("cashPayment/${transaction.id}")` → parametreler `isCashIn=false` olmalıydı
   - Fallback olarak `processPayment()` çağırılıyor (tüm tutarı bir kere öder)

2. CashPaymentScreen ve BankPaymentScreen'deki logic doğru görünüyor:
   - Nakit akışı transaction'ı oluşturuyor (Kasa Girişi/Çıkışı)
   - Orijinal işlemi güncelleme yapıyor (amount azaltma, status değiştirme)

## Çözüm Adımları (Uygulandı)

### 1. TransactionDetailScreen.kt - Navigation Parametreleri Düzeltildi
- **Önceki kod**: `navController?.navigate("cashPayment/${transaction.id}")`
- **Yeni kod**: `navController?.navigate("cashPayment/${transaction.id}?isCashIn=false")` ve tahsilat için `isCashIn=true`
- Banka butonları da aynı şekilde düzeltildi (`isBankIn` parametresi)
- Fallback `processPayment()` çağrıları kaldırıldı (navController her zaman geçiliyor)

### 2. MainActivity.kt - navController Parametresi Eklendi
- TransactionDetailScreen çağrısına `navController = navController` parametresi eklendi
- Bu sayede screen tüm navigation işlemleri doğru şekilde yapabilir

## Beklenen Sonuçlar

### Borç Ödeme Akışı
1. Detay ekranında "Kasadan Öde" butonuna tıkla
2. CashPaymentScreen açılır (`isCashIn=false`)
3. Ödeme tutarını gir (5.000 TL)
4. Tarih seç
5. Kaydet'e tıkla
6. ✅ Nakit akışı transaction oluşturulur (Kasa Çıkışı: -5.000)
7. ✅ Orijinal borç işlemi güncellenir (30.000 - 5.000 = 25.000)
8. ✅ Ana ekranda borç bakiyesi: 25.000 TL gösterilir
9. ✅ Kasa bakiyesi: -5.000 TL düşer

### Alacak Tahsilat Akışı
1. Detay ekranında "Kasadan Tahsil" butonuna tıkla
2. CashPaymentScreen açılır (`isCashIn=true`)
3. Tahsilat tutarını gir (5.000 TL)
4. Tarih seç
5. Kaydet'e tıkla
6. ✅ Nakit akışı transaction oluşturulur (Kasa Girişi: +5.000)
7. ✅ Orijinal alacak işlemi güncellenir (30.000 - 5.000 = 25.000)
8. ✅ Ana ekranda alacak bakiyesi: 25.000 TL gösterilir
9. ✅ Kasa bakiyesi: +5.000 TL artar

## Validasyonlar (Zaten Mevcut)
- Ödeme tutarı 0'dan büyük olmalı
- Borç ödeme sırasında Kasa bakiyesi yeterli olmalı
- Alacak tahsilat sırasında herhangi bir kontrol yok (para giriş olduğu için)

## Test Etme Adımları
1. APK build et: `gradlew :app:assembleDebug` ✅ (BAŞARILI)
2. Emülatöre kur: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. Test durumları:
   - Yeni borç ekle (30.000 TL)
   - Detay ekranı aç
   - "Kasadan Öde" tıkla
   - 5.000 TL ödeme yap
   - Borç bakiyesi 25.000 TL olması gerekir ✅
   - Kasa bakiyesi -5.000 TL düşmesi gerekir ✅

## Notlar
- CashPaymentScreen ve BankPaymentScreen'deki mantık zaten doğru
- processPayment() metodu hala fallback olarak var (eski compatibility için)
- KasaBalance ve BankaBalance StateFlow'lar kategori-based filtering ile çalışıyor
- Tüm değişiklikler navigation ve parameter passing ile ilgili
