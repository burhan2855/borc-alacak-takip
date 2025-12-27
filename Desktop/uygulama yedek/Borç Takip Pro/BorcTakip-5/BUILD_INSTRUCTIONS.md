# Build Instructions (Yeni APK Oluştur)

## Yapılan Değişiklikler
1. **Eski kısmi ödeme (Partial Payment) sistemini kaldırdık**
   - TransactionRepository'deki `applyPartialPayment()` metodunu sildik
   - TransactionDao'daki eski UPDATE query'sini sildik

2. **Yeni ödeme sistemi etkindir**
   - Borçlar için: "Kasadan Öde" ve "Bankadan Öde" butonları
   - Alacaklar için: "Kasadan Tahsilat" ve "Bankadan Tahsilat" butonları

## Yeni APK Oluşturmak İçin

### Seçenek 1: Android Studio Kullanarak
1. Android Studio'da projeyi aç
2. `Build` → `Clean Project`
3. `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
4. APK'nın yolu: `app/build/outputs/apk/debug/app-debug.apk`

### Seçenek 2: Terminal Kullanarak
```bash
./gradlew clean :app:assembleDebug
```

APK sonra `app/build/outputs/apk/debug/` klasöründe bulunacak.

## Cihaza Yüklemek
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Sorunun Kaynağı
Logcatda "=== PARTIAL PAYMENT START ===" yazması, eski APK çalışıyordu demek oluyor.
Yeni build'den sonra bu mesaj çıkmayacak ve ödeme işlemi düzgün çalışacak.
